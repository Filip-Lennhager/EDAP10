package lift;

/**
 * Represents the lift's movement and behavior.
 */
public class LiftThread extends Thread {

    private LiftView view;
    private LiftMonitor monitor;
    private int currentFloor = 0;
    private boolean goingUp;

    private final int TOP_FLOOR, BOTTOM_FLOOR = 0;

    /**
     * Constructs a new LiftThread.
     * 
     * @param view     The view associated with the lift.
     * @param monitor  The monitor controlling the lift's behavior.
     * @param topFloor The top floor that the lift can reach.
     */
    public LiftThread(LiftView view, LiftMonitor monitor, int topFloor) {

        TOP_FLOOR = topFloor - 1; // index conversion
        this.view = view;
        this.monitor = monitor;
    }

    @Override
    public void run() {

        while (true) {
            try {
                monitor.awaitPassenger(currentFloor); // Wait for passenger to enter/exit lift
            } catch (Exception e) {
                e.printStackTrace();
            }
            view.moveLift(currentFloor, nextFloor());

        }
    }

    /**
     * Determines the next floor the lift should move to.
     * 
     * @return The next floor number.
     */
    public int nextFloor() {

        // Change direction if at top or bottom floor
        if (currentFloor == BOTTOM_FLOOR)
            goingUp = true;
        else if (currentFloor == TOP_FLOOR)
            goingUp = false;

        // Increment if going up, otherwise decrement
        currentFloor += goingUp ? 1 : -1;
        return currentFloor;
    }
}
