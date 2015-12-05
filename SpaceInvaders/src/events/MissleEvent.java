package events;

/**
 * This class models the event signaling when a game object has shot a missile.
 * @author Derek Batts
 *
 */
public class MissleEvent extends Event {

	
	private static final long serialVersionUID = 7937441819145232356L;
	// The guid of the creator
	public int creatorGUID;
	
	/**
	 * This constructs the event.
	 * @param time The time the event happened.
	 * @param priority The priority given to this event.
	 * @param creatorGUID The GUID of the object that shot the missile.
	 */
	public MissleEvent(int time, int priority, int creatorGUID) {
		super(time, priority);
		this.creatorGUID = creatorGUID;
	}

}
