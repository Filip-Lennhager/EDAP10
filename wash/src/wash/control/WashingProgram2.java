package wash.control;

import wash.io.WashingIO;

import static wash.control.WashingMessage.Order.*;

import actor.ActorThread;

/**
 * Program 2 - White Wash
 * Like program 1, but with a 20 minute pre­wash in 40 deg C. The main wash (30 min­utes) is to be
 * performed in 60 deg C. Between the pre­wash and the main wash, the water in the
 * barrel is drained and replaced with new, clean water.
 */
public class WashingProgram2 extends ActorThread<WashingMessage> {

    private WashingIO io;
    private ActorThread<WashingMessage> temp;
    private ActorThread<WashingMessage> water;
    private ActorThread<WashingMessage> spin;

    public WashingProgram2(WashingIO io,
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

        WashingUtility washingUtility = new WashingUtility(this, this::receive, temp, water, spin);
        try {
            // Lock the hatch
            io.lock(true);
            // Pre Wash in 40 deg C for 20 min
            washingUtility.wash(TEMP_SET_40, 20);
            // Wash in 60 deg C for 30 min
            washingUtility.wash(TEMP_SET_60, 30);
            // Rinse for 2 minutes in cold water, repeat 5 times
            washingUtility.rinse(5, 2);
            // Centrifuge for 5 minutes
            washingUtility.centrifuge(5);
            // Now that the barrel has stopped, it is safe to open the hatch.
            io.lock(false);

            System.out.println("Washing Program 2 finished");
        } catch (InterruptedException e1) {

            // If we end up here, it means the program was interrupt()'ed:
            // set all controllers to idle
            washingUtility.interrupted();
            System.out.println("Washing Program 2 terminated");
        }
    }

    @Override
    public String toString(){
        return "Washing Program 2";
    }
}
