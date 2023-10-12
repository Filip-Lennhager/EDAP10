package lift;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

/**
 * Monitors and controls the behavior of the lift and passengers.
 * This class is responsible for synchronizing the actions of the lift and the
 * passengers,
 * ensuring that passengers can enter and exit the lift in a thread-safe manner.
 */
public class LiftMonitor {

    private LiftView view;

    private int[] toEnter; // Number of passengers waiting to enter the lift at each floor
    private int[] toExit; // Number of passengers (in lift) waiting to exit at each floor
    private int currentLiftFloor = 0; // The floor the lift is currently on
    private int currentNbrPassengers; // Number of passengers in the lift (0 .. MAX_PASSENGERS)
    private boolean liftMoving = false; // True if lift is moving, otherwise false
    private boolean doorsOpen = false; // True of lift doors are open, false if closed

    private final Semaphore availableSpaces; // Controls permission for passenger entry
    private final int MAX_PASSENGERS;
    private final boolean DEBUG_MODE;

    /**
     * Constructs a LiftMonitor with the specified parameters.
     * 
     * @param view          The view associated with the lift.
     * @param nbrFloors     The number of floors in the building.
     * @param maxPassengers The maximum number of passengers the lift can hold.
     * @param debugMode     If true, debug information will be displayed.
     */
    public LiftMonitor(LiftView view, int nbrFloors, int maxPassengers, boolean debugMode) {

        this.view = view;
        MAX_PASSENGERS = maxPassengers;
        DEBUG_MODE = debugMode;
        availableSpaces = new Semaphore(maxPassengers, true);
        toEnter = new int[nbrFloors];
        toExit = new int[nbrFloors];
    }

    /**
     * Constructs a LiftMonitor with the specified parameters.
     * 
     * @param view          The view associated with the lift.
     * @param nbrFloors     The number of floors in the building.
     * @param maxPassengers The maximum number of passengers the lift can hold.
     * @param debugMode     If true, debug information will be displayed.
     */
    public synchronized void awaitPassenger(int floor) throws InterruptedException {

        liftMoving = false;
        currentLiftFloor = floor;
        // Wait while passegenger wants to enter/exit and there is space availible,
        // or if no passenger wants to enter lift
        while (shouldWaitForPassenger()) {
            notifyAll(); // Passenger wants to enter -> Wake up passenger
            wait(); // Stop Lift
        }
        liftMoving = true;
    }

    /**
     * Determines if the lift should wait for a passenger.
     * 
     * @return true if the lift should wait, false otherwise.
     */
    private boolean shouldWaitForPassenger() {
        boolean passengersWantToEnter = toEnter[currentLiftFloor] > 0 && currentNbrPassengers < MAX_PASSENGERS;
        boolean passengersWantToExit = toExit[currentLiftFloor] > 0;
        boolean noPassengersWaiting = Arrays.stream(toEnter).sum() == 0 && Arrays.stream(toExit).sum() == 0;

        return passengersWantToEnter || passengersWantToExit || noPassengersWaiting;
    }

    /**
     * Makes the passenger wait for the lift to arrive at the specified floor.
     * 
     * @param startFloor The floor the passenger is currently on.
     * @throws InterruptedException If the thread is interrupted while waiting.
     */
    public synchronized void awaitLift(int startFloor) throws InterruptedException {

        notifyAll(); // Notify lift if passenger is waiting
        toEnter[startFloor]++;
        if (DEBUG_MODE) {
            view.showDebugInfo(toEnter, toExit);
        }

        while (startFloor != currentLiftFloor || liftMoving || availableSpaces.availablePermits() == 0) {
            wait(); // Passenger waits for lift
        }
        availableSpaces.acquire(); // Acquire permission for passenger to enter lift

        if (!doorsOpen) {
            view.openDoors(startFloor);
            doorsOpen = true;
        }
    }

    /**
     * Notifies the monitor that a passenger has entered the lift.
     * 
     * @param destinationFloor The floor the passenger wishes to travel to.
     */
    public synchronized void passengerEntered(int destinationFloor) {

        toEnter[currentLiftFloor]--;
        toExit[destinationFloor]++;
        if (DEBUG_MODE) {
            view.showDebugInfo(toEnter, toExit);
        }
        currentNbrPassengers++;
        closeDoors();
        notifyAll(); // start lift
    }

    /**
     * Makes the passenger wait for the lift to arrive at the specified destination
     * floor.
     * 
     * @param floor The destination floor.
     * @throws InterruptedException If the thread is interrupted while waiting.
     */
    public synchronized void awaitExit(int floor) throws InterruptedException {

        // Passenger waits for lift to reach correct floor
        while (floor != currentLiftFloor || liftMoving) {
            wait();
        }
        if (!doorsOpen) {
            view.openDoors(floor);
            doorsOpen = true;
        }
    }

    /**
     * Notifies the monitor that a passenger has exited the lift.
     */
    public synchronized void passengerExited() {

        toExit[currentLiftFloor]--;
        if (DEBUG_MODE) {
            view.showDebugInfo(toEnter, toExit);
        }
        currentNbrPassengers--;
        availableSpaces.release(); // Passenger exited lift -> Release permission to enter
        closeDoors();
        notifyAll(); // start lift

    }

    /**
     * Closes the lift doors if certain conditions are met.
     */
    private void closeDoors() {

        // Close doors if lift is full or all passengers have entered/exited (doorsOpen
        // check is redundant)
        if ((currentNbrPassengers >= MAX_PASSENGERS || toEnter[currentLiftFloor] == 0)
                && toExit[currentLiftFloor] == 0) {
            doorsOpen = false;
            view.closeDoors();

        }
    }
}
