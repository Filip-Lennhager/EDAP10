package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;
import wash.io.WashingIO.Spin;
import static wash.control.WashingMessage.Order.ACKNOWLEDGMENT;;

public class SpinController extends ActorThread<WashingMessage> {

    private WashingIO io;

    public SpinController(WashingIO io) {
        this.io = io;
    }

    @Override
    public void run() {

        try {
            boolean spinLeft = true;
            boolean spinSlow = false;

            while (true) {
                // wait for up to a (simulated) minute for a WashingMessage
                WashingMessage message = receiveWithTimeout(60000 / Settings.SPEEDUP);
                // if m is null, it means a minute passed and no message was received

                if (message != null) {
                    switch (message.order()) {
                        case SPIN_SLOW:
                            spinSlow = true;
                            break;

                        case SPIN_FAST:
                            spinSlow = false;
                            io.setSpinMode(Spin.FAST);
                            break;

                        case SPIN_OFF:
                            spinSlow = false;
                            io.setSpinMode(Spin.IDLE);
                            break;
                        default:
                            System.out.println("SpinController deafult " + message.order());

                    }
                    message.sender().send(new WashingMessage(this, ACKNOWLEDGMENT));
                }
                // Alternates spin direction on every timeout during spin mode slow
                if (spinSlow) {
                    if (spinLeft)
                        io.setSpinMode(Spin.LEFT);
                    else
                        io.setSpinMode(Spin.RIGHT);
                    spinLeft = !spinLeft;
                }
            }
        } catch (InterruptedException unexpected) {
            // we don't expect this thread to be interrupted,
            // so throw an error if it happens anyway
            throw new Error(unexpected);
        }
    }
}
