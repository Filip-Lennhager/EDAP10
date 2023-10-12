package simulation;

import lift.LiftMonitor;
import lift.LiftThread;
import lift.LiftView;
import lift.PassengerThread;

/**
 * The main simulation class for the lift system.
 */
public class LiftSimulation {
    public static void main(String[] args) {

        final int NBR_FLOORS = 7, MAX_PASSENGERS = 4, NBR_PASSENGERS = 12;
        final boolean DEBUG_MODE = false;

        LiftView view = new LiftView(NBR_FLOORS, MAX_PASSENGERS);
        LiftMonitor monitor = new LiftMonitor(view, NBR_FLOORS, MAX_PASSENGERS, DEBUG_MODE);
        LiftThread liftThread = new LiftThread(view, monitor, NBR_FLOORS);
        liftThread.start();

        for (int i = 0; i < NBR_PASSENGERS; i++) {
            new PassengerThread(view, monitor).start();
        }
    }
}