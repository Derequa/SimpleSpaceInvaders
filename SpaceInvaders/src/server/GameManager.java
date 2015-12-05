package server;

import java.awt.Rectangle;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.Semaphore;

import javax.script.ScriptException;
import javax.swing.ImageIcon;

import scripts.*;
import events.*;
import model.*;
import processing.core.PApplet;
import processing.core.PImage;
import processing.event.KeyEvent;
import time.TimeLine;

/**
 * This class displays and updates the game world. It also starts up server-client communication
 * and sends out world state to all clients on each step.
 * @author Derek Batts
 *
 */
public class GameManager extends PApplet {
	
	// Important thing
	private static final long serialVersionUID = 1227957981784275051L;
	// Global time-line
	public static TimeLine globalTime = new TimeLine(0, 2, Integer.MAX_VALUE);
	// A list of all the Servers handling clients
	protected static LinkedList<Server> servers = new LinkedList<Server>();
	// A list of all the clients connected
	protected static LinkedList<Socket> clients = new LinkedList<Socket>();
	// A table mapping players to the server thread/client controlling them
	protected static Hashtable<Player, Server> playerServerMap = new Hashtable<Player, Server>();
	// A list of scripts to run on each loop iteration
	protected static LinkedList<String> runtimeScripts = new LinkedList<String>();
	// A sprite library
	public static ArrayList<PImage> imageLib = new ArrayList<PImage>();
	// A thread to listen for incoming connections
	protected ConnectionManager listener = new ConnectionManager(this);
	private Thread listenerThread = new Thread(listener);
	
	// A random object for general use
	public static Random rand = new Random();
	// A semaphore to lock the list of players
	public static Semaphore stateLock = new Semaphore(1);
	// The event manager for the server
	public static EventManager eventManager = new EventManager();
	// A debugging flag
	public static final boolean debug = true;
	// Player 1 for playing on the server in debug mode
	public Player p1 = null;
	// A table of all our objects and their GUIDs
	public static Hashtable<Integer, GameObject> objects = new Hashtable<Integer, GameObject>();
	
	// A spawn for players (and boxes)
	protected static Spawn playerSpawn;
	// A collision detector
	protected static Collider collisionDetector = new Collider(objects);
	// A motion updater
	public static Mover motionUpdater = new Mover(objects);
	// A handler "system" for player input
	protected static HumanIO hidHandler = new HumanIO(objects);
	// A thingy for handling bullets
	protected static Shooter shooter = null;
	// Its time to oil up
	PImage bg1;
	PImage bg2;
	
	// Window size stuff
	private static final int WIDTH = 500;
	private static final int HEIGHT = 500;
	
	// Object marker
	public static int guidMaker = 0;
	public static int playerCounter = 0;
	// A place to print (deprecated)
	@Deprecated
	protected static OutputStream console = System.out;
	// A rectangle defining the window
	StaticRectangle window;
	MovingPlatform s = new MovingPlatform(guidMaker++, new Rectangle(389, 380, 80, 45), this, null, null);
	public static AlienCluster aliens;
	
	public void settings(){
		size(WIDTH, HEIGHT);
	}
	
	/**
	 * This method sets up the game
	 */
	public void setup(){
		// Start listening for new connections
		listenerThread.start();
		bg1 = loadImage("resources/images/spi_bg0.jpg");
		bg2 = loadImage("resources/images/spi_bg1.png");
		bg1.resize(WIDTH, HEIGHT);
		bg2.resize(WIDTH, HEIGHT);
		// Lock the state in startup
		try {
			stateLock.acquire();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		shooter = new Shooter(this);
		// Create spawn and death zones
		Rectangle spawnBounds = new Rectangle(217, 420, 30, 30);
		playerSpawn = new Spawn(spawnBounds, objects);
		// Setup the window object
		window = new StaticRectangle(guidMaker++, new Rectangle(0, 0, WIDTH, HEIGHT), this);
		objects.put(s.getGUID(), s);
		loadImages();
		// Register systems with the event manager
		eventManager.registerCollisionEvents(collisionDetector);
		eventManager.registerSpawnEvent(playerSpawn);
		eventManager.registerMovementEvent(motionUpdater);
		eventManager.registerHIDEvent(hidHandler);
		eventManager.registerNewPlayerEvent(this);
		eventManager.registerPlayerQuitEvent(this);
		eventManager.registerMissleEvent(shooter);
		eventManager.registerDeathEvent(playerSpawn);
		eventManager.registerGameOverEvent(this);
		
		
		// Create all the walls
		StaticRectangle[] walls = { new StaticRectangle(guidMaker++, new Rectangle(0, 0, WIDTH, 1), this),
									new StaticRectangle(guidMaker++, new Rectangle(0, HEIGHT, WIDTH, 1), this),
									new StaticRectangle(guidMaker++, new Rectangle(WIDTH, 0, 1, HEIGHT), this),
									new StaticRectangle(guidMaker++, new Rectangle(0, 0, 1, HEIGHT), this)};
		// Add walls to the table of objects
		for(int i = 0 ; i < 4 ; i++)
			objects.put(walls[i].getGUID(), walls[i]);
		
		aliens = new AlienCluster(10, 5, this);
		
		// Make a player in debug mode
		if(debug){
			p1 = createPlayer(guidMaker++);
			objects.put(p1.getGUID(), p1);
		}
		
		doScripts();
		s.setSprite(loadImage("resources/images/mdew.png"));
		
		// Unlock the state
		stateLock.release();
		
		
	}
	
	/**
	 * This method loads all the images we need into the game manager.
	 */
	private void loadImages() {
		imageLib.add(0, loadImage("resources/images/alien0.png"));
		imageLib.add(1, loadImage("resources/images/alien1.png"));
		imageLib.add(2, loadImage("resources/images/alien2.png"));
		imageLib.add(3, loadImage("resources/images/lsr.png"));
		imageLib.add(4, loadImage("resources/images/lsr2.png"));
	}

	/**
	 * This method draws each frame on each step of the game loop
	 */
	public void draw(){
		//Set the background
		background(25);
		image(bg1,0, 0);
		image(bg2, 0, 0);

		// Lock the server state
		try {
			stateLock.acquire();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		// Runtime scripts
		for(String s : runtimeScripts){
			ScriptManager.loadScript(s);
			ScriptManager.executeScript();
		}
		
		// Step the global time up
		globalTime.step();
		
		// Manually update the aliens
		motionUpdater.update(aliens);
		// Check if all the aliens are gone
		if(aliens.numLeft <= 0){
			// Re-make the cluster
			aliens = new AlienCluster(10, 5, this);
			// Remove all the missiles currently in the game
			for(Iterator<GameObject> it = objects.values().iterator() ; it.hasNext() ;){
				GameObject g = it.next();
				if(g instanceof Missle)
					it.remove();
			}
		}
		// Check if the cluster can fire a missile
		if(aliens.cooldown > 0)
			aliens.cooldown--;
		else{
			Box b = aliens.getShooter();
			eventManager.raiseMissleEvent(new MissleEvent(globalTime.getTime(), 2, b.getGUID().intValue()));
			aliens.cooldown = 120;
		}
		
		objectLoop: for(GameObject g : objects.values()){
			// Handle input and modify the player's fields
			if(g instanceof Player){
				hidHandler.handleInput((Player) g);
				if(((Player) g).cooldown > 0)
					((Player) g).cooldown--;
				if(((Player) g).score > 13000){
					((Player) g).score -= 13000;
					((Player) g).deathsLeft++;
				}
				if(((Player) g).deathsLeft < 0)
					resetPlayer((Player) g);
			}
			
			// Move and update
			g.move();
			
			// Re-spawn if out of bounds
			if(!collisionDetector.collides(g, window)){
				if(g.isSpawnable()){
					SpawnEvent e = new SpawnEvent(globalTime.getTime(), 2, g.getGUID().intValue());
					eventManager.raiseSpawnEvent(e);
				}
				else if(!g.isStatic())
					eventManager.raiseDeathEvent(new DeathEvent(globalTime.getTime(), 0, g.getGUID()));
				
			}
			
			// Check collisions with every other object
			collider: for(GameObject g2 : objects.values()){
				
				// Skip ourself
				if(g.equals(g2))
					continue;
				// Detect collisions
				if(collisionDetector.collides(g, g2)){
					if(((g instanceof Box) && (g2 instanceof MovingPlatform)) || ((g2 instanceof Box) && (g instanceof MovingPlatform))){
						//Game over reset game
						eventManager.raiseGameOverEvent(new GameOverEvent(globalTime.getTime(), 0));
						break objectLoop;
					}
					// Players do not collide with each other
					if((g instanceof Player) && (g2 instanceof Player))
						continue collider;
					// If a missile and a box or player are colliding...
					else if(((g instanceof Missle) && (g2 instanceof Box)) || ((g2 instanceof Missle) && (g instanceof Box))){
						// Determine what is happening
						Missle m = null;
						Player p = null;
						Box b = null;
						
						if(g instanceof Missle)
							m = (Missle) g;
						else m = (Missle) g2;
						
						if(g instanceof Player)
							p = (Player) g;
						else if(g2 instanceof Player)
							p = (Player) g2;
						else if((g instanceof Box) && (p == null))
							b = (Box) g;
						else if((g2 instanceof Box) && (p == null))
							b = (Box) g2;
						
						// Check if a player and a non-player-fired missile are colliding
						if((p != null) && !m.playerFired){
							eventManager.raiseDeathEvent(new DeathEvent(globalTime.getTime(), 2, p.getGUID()));
							eventManager.raiseDeathEvent(new DeathEvent(globalTime.getTime(), 2, m.getGUID()));
						}
						// Check if a box and a player-fired missile are colliding
						else if((b != null) && m.playerFired){
							((Player) m.whoFired).score += b.worth;
							eventManager.raiseDeathEvent(new DeathEvent(globalTime.getTime(), 2, b.getGUID()));
							eventManager.raiseDeathEvent(new DeathEvent(globalTime.getTime(), 2, m.getGUID()));
						}
						
					}
					// Check if a missile and a moving platform are colliding
					else if(((g instanceof Missle) && (g2 instanceof MovingPlatform)) || ((g2 instanceof Missle) && (g instanceof MovingPlatform))){
						Missle m = null;
						
						if(g instanceof Missle)
							m = (Missle) g;
						else m = (Missle) g2;
						// Kill the missile
						eventManager.raiseDeathEvent(new DeathEvent(globalTime.getTime(), 2, m.getGUID()));
					}
					CollisionEvent c = new CollisionEvent(globalTime.getTime(), 2, g.getGUID().intValue(), g2.getGUID().intValue());
					eventManager.raiseCollisionEvent(c);
				}
				// If we didn't collide remember that we didn't
				else
					collisionDetector.handleNoCollide(g, g2);
			}
		}
		
		// Handle Events
		eventManager.handleAllEvents();
		textSize(14);
		int count = 0;
		fill(230, 230, 230);
		// Generate all the text for this frame
		LinkedList<String> strings = new LinkedList<String>();
		for(GameObject g : objects.values()){
			if(g instanceof Player){
				Player p = (Player) g;
				String toDraw = "Player " + p.playerID + ": Score: " + p.score + " Deaths Left: " + p.deathsLeft;
				strings.add(toDraw);
				text(toDraw, 10, 15 + (count * 17));
				count++;
			}
		}
		
		
		// Send the world state
		UpdatePacket u = makePacket(strings);
		for(Server s : servers){
			// Synchronize of each server's lock
			synchronized(s.waitingThing){
				// Set packet and notify
				s.packet = u;
				s.waitingThing.notify();
			}
		}
		
		
		// Unlock the server state
		stateLock.release();
		
		// Draw each
		for(GameObject g : objects.values())
			g.draw();
	}
	
	/**
	 * This method creates and returns a new player in the game
	 * @return the player created
	 */
	public Player createPlayer(int guid){
		// Make the player
		Player p = new Player(guid, new Rectangle(45, 30), playerSpawn, null, motionUpdater, collisionDetector, this);
		PImage img;
		img = loadImage("resources/images/alien3.png");
		p.setSprite(img);
		// Set fields
		p.aSetY(Moveable.GRAVITY);
		p.setVisible(true);
		p.playerID = playerCounter++;
		int r = 0;
		int g = 0;
		int b = 0;
		while((r + g + b) < 50){
			r = rand.nextInt(256);
			g = rand.nextInt(256);
			b = rand.nextInt(256);
		}
		p.setColor(r, g, b);
		p.spawn();
		
		return p;
	}
	
	/**
	 * This method handles a new player connecting to the server.
	 * @param e The event describing the new player.
	 */
	public void handleNewPlayer(NewPlayerEvent e){
		Player p = createPlayer(e.guid);
		objects.put(p.getGUID(), p);
	}
	
	/**
	 * This method handles a player leaving the server.
	 * @param e The event describing the quiter.
	 */
	public void handlePlayerQuit(PlayerQuitEvent e){
		objects.remove(new Integer(e.guid));
	}
	
	/**
	 * This method responds to keyboard input on the server.
	 */
	@Override
	public void keyPressed(KeyEvent k){
		// Raise an event about the key press if we are in debug mode
		if(debug){
			HIDEvent e = new HIDEvent(globalTime.getTime(), 0, k.getKey(), true, p1.getGUID().intValue());
			eventManager.raiseHIDEvent(e);
		}
	}
	
	/**
	 * This method responds to keyboard input on the server.
	 */
	@Override
	public void keyReleased(KeyEvent k){
		// Raise an event about the key release if we are in debug mode
		if(debug){
			HIDEvent e = new HIDEvent(globalTime.getTime(), 0, k.getKey(), false, p1.getGUID().intValue());
			eventManager.raiseHIDEvent(e);
		}
	}
	
	/**
	 * This method makes a packet to send to the clients based on the game state.
	 * @param strings 
	 * @return The packet summarizing the game state.
	 */
	@SuppressWarnings("deprecation")
	private UpdatePacket makePacket(LinkedList<String> strings){
		// Count the number of visible objects
		int numRects = 0;
		int numSprited = 0;
		for(GameObject g : objects.values())
			if(g.visible()){
				if(g.hasSprite())
					numSprited++;
				else 
					numRects++;
			}
		// Prepare a packet
		UpdatePacket update = new UpdatePacket(numRects, numSprited, strings.size(), globalTime.getTime());
		
		// Store x, y, width, height, color data, and sprite for each visible object
		int i = 0;
		int j = 0;
		for(GameObject g : objects.values()){
			if(g.visible()){
				// Add an image
				if(g.hasSprite()){
					update.sprites[j] = new ImageIcon(g.getSprite().getImage());
					update.spriteCoords[j][0] = g.getShape().x;
					update.spriteCoords[j][1] = g.getShape().y;
					j++;
				}
				// Add the rectangle data and color
				else{
					for(int k = 0 ; k < 3 ; k++)
						update.rectColors[i][k] =  g.getColor()[k];;
					Rectangle r = g.getShape();
					update.rectVals[i][0] = r.x;
					update.rectVals[i][1] = r.y;
					update.rectVals[i][2] = r.width;
					update.rectVals[i][3] = r.height;
					i++;
				}
			}
		}
		// Add all the current strings
		int k = 0;
		for(String s : strings)
			update.strings[k++] = s;
		
		return update;
	}
	
	/**
	 * This method resets a player's score, lives, and cooldown to their default values.
	 * Then it respawnes the player.
	 * @param p The player to reset.
	 */
	private void resetPlayer(Player p){
		p.collidedFromBottom = false;
		p.collidedFromLeft = false;
		p.collidedFromRight =false;
		p.collidedFromTop = false;
		p.colliderBottom = null;
		p.colliderLeft = null;
		p.colliderRight = null;
		p.colliderTop = null;
		p.collidedWith.clear();
		p.deathsLeft = Player.INIT_DEATHS;
		p.cooldown = Player.COOLDOWN;
		p.score = 0;
		eventManager.raiseSpawnEvent(new SpawnEvent(globalTime.getTime(), 0, p.getGUID().intValue()));
	}
	
	/**
	 * This method reads in some scripts and runs a few.
	 * Scripts named "startup_script_n" where n is an integer (assuming all are named
	 * and stored in increasing order) will be loaded and ran in the order they are found.
	 * Scripts named "runtime_script_n" where n is an integer (again scripts named in increasing
	 * order starting at 0) will be found and stored for the game to run on each loop iteration.
	 */
	private void doScripts(){
		// Print that we are reading scripts
		System.out.println("Running startup scripts...");
		
		// Set up some arguments for scripts
		ScriptManager.bindArgument("game_manager", this);
		ScriptManager.bindArgument("event_manager", eventManager);
		ScriptManager.bindArgument("s", s);
		ScriptManager.bindArgument("timeline", globalTime);
		
		// Initialize a counter
		int i = -1;
		try{
			// Read scripts and run them until we hit a file not found exception
			for(i = 0 ; i < Integer.MAX_VALUE ; i++){
				FileReader reader = new FileReader("resources/scripts/startup_script_" + i + ".js");
				ScriptManager.loadScript(reader);
				ScriptManager.executeScript();
			}
			
		} catch (FileNotFoundException e) {
			// Print we've read and processed all the scripts we could find
			System.out.println("Done running startup scripts!");
		} catch (ScriptException e) {
			// Print an error if one occured running a script
			System.out.println("Error running startup script #" + i + "!");
			e.printStackTrace();
		}
		
		// Print we are reading runtime scripts
		System.out.println("Reading runtime scripts...");
		try{
			// Look for and store the filenames of all the runtime scripts we find
			for(i = 0 ; i < Integer.MAX_VALUE ; i++){
				FileReader reader = new FileReader("resources/scripts/runtime_script_" + i + ".js");
				System.out.println("Found: runtime_script_" + i + ".js");
				runtimeScripts.add("resources/scripts/runtime_script_" + i + ".js");
				reader.close();
			}
			
		} catch (FileNotFoundException e) {
			// Notify the console that we are done reading scripts in
			System.out.println("Done reading runtime scripts!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	/**
	 * This method handles reseting the game on gameover.
	 * @param e The event signaling game over.
	 */
	public void handleGameOver(GameOverEvent e) {
		for(Box b : aliens.removeAll()){
			if(b != null)
				objects.remove(b.getGUID());
		}
		for(Iterator<GameObject> it = objects.values().iterator() ; it.hasNext() ;){
			GameObject g = it.next();
			if(g instanceof Player)
				resetPlayer((Player) g);
			else if(g instanceof Missle)
				it.remove();
		}
		aliens = new AlienCluster(10, 5, this);
	}
}
