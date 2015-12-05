package client;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;

import processing.core.PApplet;
import processing.core.PImage;
import server.UpdatePacket;
import time.TimeLine;

/**
 * This class models a new PApplet to view a replay on.
 * @author Derek Batts
 *
 */
public class ReplayViewer extends PApplet {

	private static final long serialVersionUID = 4070639300389438329L;

	// A state lock
	public Semaphore lock = new Semaphore(1);
	// the replay time-line
	public TimeLine timeline;
	// A flag for playing/stopping
	public boolean playing = false;
	// A queue of frames
	public PriorityQueue<UpdatePacket> frameQueue;
	// A list of all the things in the last frame
	LinkedList<UpdatePacket> lastFrame = new LinkedList<UpdatePacket>();
	PImage bg1;
	PImage bg2;
	
	// Window stuff
	private static final int WIDTH = 500;
	private static final int HEIGHT = 500;
	
	public void settings(){
		size(WIDTH, HEIGHT);
	}
	
	@Override
	public void setup(){
		bg1 = loadImage("resources/images/spi_bg0.jpg");
		bg2 = loadImage("resources/images/spi_bg1.png");
		bg1.resize(WIDTH, HEIGHT);
		bg2.resize(WIDTH, HEIGHT);
		size(WIDTH, HEIGHT);
		frameQueue = Client.replayQueue;
		timeline = Client.replayTimeLine;
		Client.replayViewer = this;
	}
	
	@Override
	public void draw(){
		// Lock the state
		try {
			lock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Only draw if we are playing the replay
		if(playing){
			background(25);
			image(bg1,0, 0);
			image(bg2, 0, 0);
			// Mark whether or not we draw a frame
			boolean drewframe = false;
			// Loop through all frame
			for(Iterator<UpdatePacket> it = frameQueue.iterator() ; it.hasNext() ;){
					UpdatePacket u = it.next();
					// Only draw if it matches the current time
					if(!(u.timestamp == timeline.getTime()))
						continue;
					// Draw stuff
					for(int i = 0 ; i < u.numRects ; i++){
						fill(color(u.rectColors[i][0], u.rectColors[i][1], u.rectColors[i][2]));
						rect((float) u.rectVals[i][0], (float) u.rectVals[i][1], (float) u.rectVals[i][2], (float) u.rectVals[i][3]);
					}
					for(int i = 0 ; i < u.numSprites ; i++){
						PImage img = new PImage(u.sprites[i].getImage());
						image(img, u.spriteCoords[i][0], u.spriteCoords[i][1]);
					}
					textSize(14);
					fill(230, 230, 230);
					for(int i = 0 ; i < u.numStrings ; i++){
						text(u.strings[i], 10, 15 + (i * 17));
					}
					// Remember this frame
					lastFrame.add(u);
					// Signal a frame was drawn
					drewframe = true;
			}
			
			// Check of no frame was drawn
			if(!drewframe){
				// Draw the previous frame
				for(UpdatePacket u : lastFrame){
					for(int i = 0 ; i < u.numRects ; i++){
						fill(color(u.rectColors[i][0], u.rectColors[i][1], u.rectColors[i][2]));
						rect((float) u.rectVals[i][0], (float) u.rectVals[i][1], (float) u.rectVals[i][2], (float) u.rectVals[i][3]);
					}
				}
				// Clear the previous frame
				lastFrame.clear();
			}
			// Step the time-line
			timeline.step();
		}
		// Unlock the state
		lock.release();
	}

}
