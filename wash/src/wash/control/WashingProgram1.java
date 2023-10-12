package wash.control;

import wash.io.WashingIO;

import static wash.control.WashingMessage.Order.*;

import actor.ActorThread;

/**
 * Program 1 - Color Wash
 * Lock the hatch,let water into the machine, heat to 40 deg C, keep the
 * temperature for 30
 * minutes, drain, rinse 5 times 2 minutes in cold water, centrifuge for 5
 * minutes and unlock the hatch.
 * While washing and rinsing the barrel should spin slowly, switching between
 * left and right direction every minute. While centrifuging, the drain pump
 * should run to evacuate excess water.
 */
public class WashingProgram1 extends ActorThread<WashingMessage> {

    private WashingIO io;
    private ActorThread<WashingMessage> temp;
    private ActorThread<WashingMessage> water;
    private ActorThread<WashingMessage> spin;

    public WashingProgram1(WashingIO io,
            ActorThread<WashingMessage> temp,
            ActorThread<WashingMessage> water,
            ActorThread<WashingMessage> spin) {
        this.io = io;
        this.temp = temp;
        this.water = water;
        this.spin = spin;
    }

    @Override
    public void run() {

        // Here, this::receive passes the receive() method of Washing Program 1 as the
        // implementation of the MessageReceiver functional interface.
        WashingUtility washingUtility = new WashingUtility(this, this::receive, temp, water, spin);
        try {
            // Lock the hatch
            io.lock(true);
            // Wash in 40 deg C for 30 min
            washingUtility.wash(TEMP_SET_40, 30);
            // Rinse for 2 minutes in cold water, repeat 5 times
            washingUtility.rinse(5, 2);
            // Centrifuge for 5 minutes
            washingUtility.centrifuge(5);
            // Now that the barrel has stopped, it is safe to open the hatch.
            io.lock(false);

            System.out.println("Washing Program 1 finished");
        } catch (InterruptedException e1) {

            // If we end up here, it means the program was interrupted:
            // set all controllers to idle
            washingUtility.interrupted();
            System.out.println("Washing Program 1 terminated");
        }
    }

    @Override
    public String toString(){
        return "Washing Program 1";
    }
}
