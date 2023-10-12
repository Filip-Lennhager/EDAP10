package wash.control;

import wash.io.WashingIO;

import static wash.control.WashingMessage.Order.*;

import actor.ActorThread;

/**
 * Program 3 for washing machine. This also serves as an example of how washing
 * programs can be structured.
 * 
 * This short program stops all regulation of temperature and water levels,
 * stops the barrel from spinning, and drains the machine of water.
 * 
 * It can be used after an emergency stop (program 0) or a power failure.
 */
public class WashingProgram3 extends ActorThread<WashingMessage> {

    private WashingIO io;
    private ActorThread<WashingMessage> temp;
    private ActorThread<WashingMessage> water;
    private ActorThread<WashingMessage> spin;

    public WashingProgram3(WashingIO io,
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

        try {
            System.out.println("Washing Program 3 started");
            // Switch off heating
            System.out.println("setting TEMP_IDLE...");
            temp.send(new WashingMessage(this, TEMP_IDLE));
            // Wait for temperature controller to acknowledge
            waitForAck();

            // Drain barrel, which may take some time.
            System.out.println("setting WATER_DRAIN...");
            water.send(new WashingMessage(this, WATER_DRAIN));
            // Now that the barrel is drained, we can turn off water regulation. To ensure
            // the barrel is drained before we continue, an acknowledgment is required.
            water.send(new WashingMessage(this, WATER_IDLE));
            waitForAck();

            // Switch off spin. We expect an acknowledgment, to ensure
            // the hatch isn't opened while the barrel is spinning.
            System.out.println("setting SPIN_OFF...");
            spin.send(new WashingMessage(this, SPIN_OFF));
            waitForAck();

            // Unlock hatch
            io.lock(true);
            System.out.println("washing program 3 finished");
        } catch (InterruptedException e1) {

            // If we end up here, it means the program was interrupt()'ed:
            // set all controllers to idle
            temp.send(new WashingMessage(this, TEMP_IDLE));
            water.send(new WashingMessage(this, WATER_IDLE));
            spin.send(new WashingMessage(this, SPIN_OFF));
            System.out.println("washing program terminated");
        }
    }

    private void waitForAck() throws InterruptedException {

        WashingMessage ack = receive();
        System.out.println("Washing Program 3 got " + ack);
    }
}
