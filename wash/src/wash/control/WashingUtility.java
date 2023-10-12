package wash.control;

import actor.ActorThread;
import static wash.control.WashingMessage.Order.*;

public class WashingUtility {

    private ActorThread<WashingMessage> temp;
    private ActorThread<WashingMessage> water;
    private ActorThread<WashingMessage> spin;
    private ActorThread<WashingMessage> parentThread; // WashingProgram
    // The receiver interface provides a way for this class to obtain messagesß
    // without knowing the specific method of receiving them.
    private MessageReceiver receiver;

    private static final int TIME_UNIT = 60000 / Settings.SPEEDUP;

    // The constructor accepts instances of necessary threads and a receiver method
    // to decouple the utility from specific implementations of message receiving.
    public WashingUtility(ActorThread<WashingMessage> parentThread, MessageReceiver receiver,
            ActorThread<WashingMessage> temp,
            ActorThread<WashingMessage> water, ActorThread<WashingMessage> spin) {
        this.parentThread = parentThread;
        this.receiver = receiver;
        this.temp = temp;
        this.water = water;
        this.spin = spin;
    }

    public void wash(WashingMessage.Order setTemp, int duration) throws InterruptedException {
        System.out.println("setting FILL..");
        water.send(new WashingMessage(parentThread, WATER_FILL));
        water.send(new WashingMessage(parentThread, WATER_IDLE));
        waitForAck();

        System.out.println("setting TEMP_SET...");
        if (setTemp == TEMP_SET_40 || setTemp == TEMP_SET_60) {
            temp.send(new WashingMessage(parentThread, setTemp));
        } else {
            temp.send(new WashingMessage(parentThread, TEMP_IDLE));
        }
        waitForAck();

        // Instruct SpinController to rotate barrel slowly, back and forth
        // Expect an acknowledgment in response.
        System.out.println("setting SPIN_SLOW...");
        spin.send(new WashingMessage(parentThread, SPIN_SLOW));
        waitForAck();

        // Spin for (duration) simulated minutes (one minute == 60000 milliseconds)
        Thread.sleep(duration * TIME_UNIT);

        // Instruct SpinController to stop spin barrel spin.
        // Expect an acknowledgment in response.
        System.out.println("setting SPIN_OFF...");
        spin.send(new WashingMessage(parentThread, SPIN_OFF));
        waitForAck();

        // Switch off heating
        System.out.println("setting TEMP_IDLE... ");
        temp.send(new WashingMessage(parentThread, TEMP_IDLE));
        // Wait for temperature controller to acknowledge
        waitForAck();

        // Drain barrel, which may take some time.
        System.out.println("Setting WATER_DRAIN...");
        water.send(new WashingMessage(parentThread, WATER_DRAIN));
        // Now that the barrel is drained, we can turn off water regulation. To ensure
        // the barrel is drained before we continue, an acknowledgment is required.
        water.send(new WashingMessage(parentThread, WATER_IDLE));
        waitForAck();
    }

    public void rinse(int times, int duration) throws InterruptedException {
        for (int i = 0; i < times; i++) {
            // Fill with cold water
            // Expect an acknowledgment in response.
            System.out.println("setting WATER_FILL...");
            water.send(new WashingMessage(parentThread, WATER_FILL));
            water.send(new WashingMessage(parentThread, WATER_IDLE));
            waitForAck();

            // Instruct SpinController to spin slowly.
            // Expect an acknowledgment in response.
            System.out.println("setting SPIN_SLOW...");
            spin.send(new WashingMessage(parentThread, SPIN_SLOW));
            waitForAck();

            // Spin for two simulated minutes (one minute == 60000 milliseconds)
            Thread.sleep(duration * 60000 / Settings.SPEEDUP);

            // Instruct SpinController to stop.
            // Expect an acknowledgment in response.
            System.out.println("setting SPIN_OFF...");
            spin.send(new WashingMessage(parentThread, SPIN_OFF));
            waitForAck();

            // Drain barrel, which may take some time. To ensure the barrel
            // is drained before we continue, an acknowledgment is required.
            System.out.println("setting WATER_DRAIN...");
            water.send(new WashingMessage(parentThread, WATER_DRAIN));
            water.send(new WashingMessage(parentThread, WATER_IDLE));
            waitForAck();
        }
    }

    public void centrifuge(int duration) throws InterruptedException {

        System.out.println("setting drain...");
        water.send(new WashingMessage(parentThread, WATER_DRAIN));

        // Instruct SpinController to spin fast.
        // Expect an acknowledgment in response.
        System.out.println("setting SPIN_Fast...");
        spin.send(new WashingMessage(parentThread, SPIN_FAST));
        waitForAck();

        // Spin for (duration) simulated minutes (one minute == 60000 milliseconds)
        Thread.sleep(duration * TIME_UNIT);

        // Instruct SpinController to turn off.
        // Expect an acknowledgment in response.
        System.out.println("setting SPIN_OFF...");
        spin.send(new WashingMessage(parentThread, SPIN_OFF));
        waitForAck();
        // Turn off drain pump after cetrifuging
        // Expect an acknowledgment in response.
        System.out.println("setting WATER_IDLE...");
        water.send(new WashingMessage(parentThread, WATER_IDLE));
        waitForAck();
    }

    public void interrupted() {
        temp.send(new WashingMessage(parentThread, TEMP_IDLE));
        water.send(new WashingMessage(parentThread, WATER_IDLE));
        spin.send(new WashingMessage(parentThread, SPIN_OFF));
    }

    private void waitForAck() throws InterruptedException {

        WashingMessage ack = receiver.receive();
        System.out.println(parentThread.toString() + " got " + ack);

    }
}
