package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;
import static wash.control.WashingMessage.Order.*;

public class WaterController extends ActorThread<WashingMessage> {

    private WashingIO io;

    private final int WATER_LIMIT = 10;

    public WaterController(WashingIO io) {

        this.io = io;
    }

    @Override
    public void run() {

        try {
            boolean ackSent = false;
            boolean tryToStop = false;
            WashingMessage previousMessage = null;

            while (true) {
                // wait for up to a (simulated) second for a WashingMessage
                WashingMessage message = receiveWithTimeout(1000 / Settings.SPEEDUP);
                // if m is null, it means a seond passed and no message was received

                if (message != null) {
                    ackSent = false;
                    tryToStop = false;

                    if (message.order() == WATER_IDLE) {
                        tryToStop = true;
                    } else if (message.order() == WATER_FILL || message.order() == WATER_DRAIN) {
                        previousMessage = message;
                    }
                }
                // Draining or filling unil water is set to IDLE and within safety requirements
                // (updates each second)
                if (previousMessage != null) {
                    switch (previousMessage.order()) {
                        case WATER_FILL:
                            if (io.getWaterLevel() > WATER_LIMIT) {
                                io.fill(false);
                                if (!ackSent && tryToStop) {
                                    previousMessage.sender().send(new WashingMessage(this, ACKNOWLEDGMENT));
                                    ackSent = true;
                                }
                            } else {
                                io.drain(false); // Drain pump disabled when input valve is open
                                io.fill(true);
                            }
                            break;

                        case WATER_DRAIN:
                            if (io.getWaterLevel() == 0) {
                                if (!ackSent && tryToStop) {
                                    previousMessage.sender().send(new WashingMessage(this, ACKNOWLEDGMENT));
                                    io.drain(false);
                                    ackSent = true;
                                }
                            } else {
                                io.fill(false); // Input valve closed when drain pump is active
                                io.drain(true);
                            }
                            break;
                        default:
                            System.out.println("WaterController deafult " + previousMessage.order());

                    }
                }
            }
        } catch (InterruptedException unexpected) {
            // we don't expect this thread to be interrupted,
            // so throw an error if it happens anyway
            throw new Error(unexpected);
        }
    }
}
