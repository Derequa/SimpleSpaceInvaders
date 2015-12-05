package events;

/**
 * This class models an event signaling that the aliens have won.
 * @author Derek Batts
 *
 */
public class GameOverEvent extends Event {

	private static final long serialVersionUID = -8694246331241995746L;

	/**
	 * This constructs the event.
	 * @param time The time the event happened.
	 * @param priority The priority given to the event.
	 */
	public GameOverEvent(int time, int priority) {
		super(time, priority);
	}

}
