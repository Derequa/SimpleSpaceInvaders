package model;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import server.GameManager;

/**
 * This class models a cluster of aliens in Space Invaders.
 * This is not a game object but rather a wrapper for a collection of
 * objects representing aliens, and methods to interact with the whole.
 * @author clone
 *
 */
public class AlienCluster {
	
	// Constants for setting up the aliens
	public static final int ROW_SPACING = 10;
	public static final int COL_SPACING = 10;
	public static final int BOX_SIZE = 22;
	public static final int X_START = 10;
	public static final int Y_START = 10;
	public static final int WORTH_HIGH = 100;
	public static final int WORTH_MED = 50;
	public static final int WORTH_LOW = 30;
	// The parent object
	public GameManager parent;
	
	// A 2D array list of boxes (aliens)
	ArrayList<ArrayList<Box>> cluster = new ArrayList<ArrayList<Box>>();
	// Fields for remembering size and speed
	int height;
	int width;
	int size;
	int speed = 1;
	private int speed2 = 2;
	private int speed3 = 4;
	// The location of the cluster
	int posX = X_START;
	int posY = Y_START;
	// The number of aliens left in the cluster
	public int numLeft;
	// Which way is the cluster moving?
	boolean xDirection = false;
	// The cool-down for the cluster's missiles
	public int cooldown = 90;

	/**
	 * This constructs a cluster of aliens according to the given parameters.
	 * @param width The width (number of aliens) of the cluster.
	 * @param height The height (number of aliens) of the cluster.
	 * @param parent The parent/creator for this cluster.
	 */
	public AlienCluster(int width, int height, GameManager parent){
		// Set fields
		this.height = height;
		this.width = width;
		size = width * height;
		numLeft = size;
		this.parent = parent;
		// Make the 2D array
		for(int i = 0 ; i < height ; i++)
			cluster.add(i, new ArrayList<Box>());
		// Initialize where we are placing aliens
		int currentX = X_START;
		int currentY = Y_START;
		// Loop for each row
		for(int i = 0 ; i < height ; i++){
			// Loop for each slot in the row
			for(int j = 0 ; j < width ; j++){
				// Make a new box (alien)
				Box b = new Box(GameManager.guidMaker++, new Rectangle(currentX, currentY, BOX_SIZE, BOX_SIZE), parent);
				// Update the current x co-ordinate
				currentX += BOX_SIZE + ROW_SPACING;
				b.setVisible(true);
				
				// Generate a random number
				int result = GameManager.rand.nextInt(10);
				// 10% of the time we make a high worth box
				if(result == 0){
					b.worth = WORTH_HIGH;
					b.setSprite(GameManager.imageLib.get(0));
				}
				// 30% of the time we make a medium worth box
				else if((result > 0) && (result < 4)){
					b.worth = WORTH_MED;
					b.setSprite(GameManager.imageLib.get(1));
				}
				// 60% of the time we make a low worth box
				else {
					b.worth = WORTH_LOW;
					b.setSprite(GameManager.imageLib.get(2));
				}
				// Add the alien to the cluster and master object list
				cluster.get(i).add(j, b);
				GameManager.objects.put(b.getGUID(), b);
			}
			// Update our current placement x and y
			currentY += BOX_SIZE + COL_SPACING;
			currentX = X_START;
		}
	}
	
	/**
	 * This method looks through the cluster of aliens and finds all the
	 * aliens who are not obstructed by other aliens and can potentially shoot at
	 * the player. It the randomly picks one and returns it to the caller.
	 * @return A random alien that can shoot at the player.
	 */
	public Box getShooter(){
		// Only look if we are allowed to shoot
		if(cooldown > 0)
			return null;
		// Make a list of potential shooters
		LinkedList<Box> canFire = new LinkedList<Box>();
		// Loop through each row
		for(int i = 0 ; i < cluster.size() ; i++){
			// Initialize a counter for index
			int j = -1;
			loop1: for(Box b : cluster.get(i)){
				j++;
				if(b == null)
					continue loop1;
				// When we find a box loop to the next rows and see if there are boxes in front of us
				for(int k = i + 1 ; k < cluster.size() ; k++){
					// If we find one in front of us move on
					if(cluster.get(k).size() <= j)
						continue loop1;
					else if(cluster.get(k).get(j) != null)
						continue loop1;
				}
				// if we make it here, add the box to the valid list
				canFire.add(b);
			}
		}
		// Return a random element from the list
		return canFire.get(GameManager.rand.nextInt(canFire.size()));
	}
	
	/**
	 * This method removes a given box from the cluster.
	 * NOTE: The box is removed by setting its slot to null.
	 * 		 Shuffling the elements in the 2D array would ruin the getShooter method.
	 * @param b The box to remove.
	 */
	public void removeFromCluster(Box b){
		// Loop for each row
		for(int i = 0 ; i < cluster.size() ; i++){
			// See if the box is in this row
			int index = cluster.get(i).indexOf(b);
			if(index >= 0){
				// Set the slot to null
				cluster.get(i).set(index, null);
				// Update the cluster based on the removal
				numLeft--;
				if((numLeft <= (size / 8)) && (speed < speed3))
					speed = speed3;
				else if((numLeft <= (size / 4)) && (speed < speed2))
					speed = speed2;
				return;
			}
		}
	}
	
	/**
	 * This method removes all the aliens left and returns them in a set.
	 * @return All the boxes that were in the cluster in a set.
	 */
	public Set<Box> removeAll(){
		// Make an empty set
		Set<Box> ret = new HashSet<Box>();
		// Loop for every row
		for(int i = 0 ; i < cluster.size() ; i++){
			// Loop for every slot
			for(int j = 0 ; j < cluster.get(i).size() ; j++){
				// Add the box to the return set and set its slot to null
				if(cluster.get(i).get(j) != null){
					ret.add(cluster.get(i).get(j));
					cluster.get(i).set(j, null);
				}
			}
		}
		return ret;
	}
}
