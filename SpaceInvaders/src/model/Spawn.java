package model;

import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Hashtable;

import events.DeathEvent;
import events.SpawnEvent;
import server.GameManager;

/**
 * This class wraps up a few simple data fields and methods
 * retaled to a spawn (or de-spawn)
 * @author Derek Batts
 *
 */
public class Spawn implements Serializable{
	
	private static final long serialVersionUID = 2479661271009259133L;

	Hashtable<Integer, GameObject> objects;
	
	// The rectangle defining the boundary
	private Rectangle boundary;
	// Is the spawn active
	boolean isActive;
	
	/**
	 * A spawn is constructed around a rectangle and is set active by default
	 * @param bounds The Rectangle that defines us.
	 * @param objects The master table of game objects.
	 */
	public Spawn(Rectangle bounds, Hashtable<Integer, GameObject> objects){
		this.boundary = bounds;
		isActive = true;
		this.objects = objects;
	}
	
	// Getter methods
	
	public int getX(){
		return boundary.x;
	}
	
	public int getY(){
		return boundary.y;
	}
	
	public Rectangle getBounds(){
		return boundary;
	}
	
	public boolean isActive(){
		return this.isActive;
	}
	
	// Setter methods
	
	public void setActive(boolean bool){
		this.isActive = bool;
	}
	
	public void moveSpawn(int x, int y){
		boundary.x = x;
		boundary.y = y;
	}
	
	public void setBounds(Rectangle bounds){
		this.boundary = bounds;
	}
	
	/**
	 * This method handles a given spawn event.
	 * @param e The event to handle.
	 */
	public void handleSpawnEvent(SpawnEvent e){
		// Check if the object the event is about is in our table
		if(!objects.containsKey(new Integer(e.guid)))
			return;
		GameObject g = objects.get(new Integer(e.guid));
		// Check if its a death event
		if(e instanceof DeathEvent){
			if(g.spawner == null){
				for(GameObject collided : g.collidedWith.keySet()){
					collided.collidedWith.remove(g);
					if(g.equals(collided.colliderLeft))
						collided.collidedFromLeft = false;
					else if(g.equals(collided.colliderRight))
						collided.collidedFromRight = false;
					else if(g.equals(collided.colliderTop))
						collided.collidedFromTop = false;
					else if(g.equals(collided.colliderBottom))
						collided.collidedFromBottom = false;
				}
				if(g instanceof Box)
					GameManager.aliens.removeFromCluster((Box) g);
				objects.remove(g.getGUID());
			}
			else if(g instanceof Player)
				((Player) g).deathsLeft--;
			else
				g.setVisible(false);
		}
		if(g.spawner != null)
			g.spawn();
	}
}
