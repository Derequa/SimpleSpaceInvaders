package model;

import java.awt.Rectangle;

import processing.core.PApplet;

/**
 * This is a simple class to model a missile in Space Invaders.
 * @author Derek Batts
 *
 */
public class Missle extends GameObject {

	private static final long serialVersionUID = 3273797807159676813L;
	// Constants for missile size and speed
	public static final int MISSLE_V = 4;
	public static final int MISSLE_SIZE = 13;
	
	// Fields for velocity and who shot this missile
	public int vY = 0;
	public boolean playerFired = false;
	public GameObject whoFired;
	
	/**
	 * This constructs a missile object.
	 * @param guid The GUID of this object.
	 * @param s The shape of this object.
	 * @param parent The parent PApplet to draw to.
	 * @param shooter The object that shot this missile.
	 */
	public Missle(int guid, Rectangle s, PApplet parent, GameObject shooter) {
		super(guid, s, parent);
		whoFired = shooter;
	}

	@Override
	public boolean isStatic() {
		return false;
	}

}
