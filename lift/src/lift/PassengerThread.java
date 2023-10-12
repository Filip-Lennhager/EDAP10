package lift;

/**
 * Represents the behavior of a passenger in the lift system.
 */
public class PassengerThread extends Thread {

    private LiftView view;
    private LiftMonitor monitor;

    /**
     * Constructs a new PassengerThread.
     * 
     * @param view    The view associated with the passenger.
     * @param monitor The monitor controlling the passenger's behavior.
     */
    public PassengerThread(LiftView view, LiftMonitor monitor) {

        this.view = view;
        this.monitor = monitor;
    }

    @Override
    public void run() {

        while (true) {
            Passenger pass = view.createPassenger();
            pass.begin();
            try {
                monitor.awaitLift(pass.getStartFloor()); // Passenenger waiting to enter lift
                pass.enterLift();
                monitor.passengerEntered(pass.getDestinationFloor()); // Passenger entered lift
                monitor.awaitExit(pass.getDestinationFloor()); // Passenger waiting to leave lift
            } catch (Exception e) {
                e.printStackTrace();
            }
            pass.exitLift();
            monitor.passengerExited(); // Passenger left lift

            pass.end();
        }
    }
}
