package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class TemperatureController extends ActorThread<WashingMessage> {

    private WashingIO io;


    private static final int D_T = 10; // Temperature regulation interval
    private static final int TIME_UNIT = D_T * 1000 / Settings.SPEEDUP;
    private static final double SENSOR_MARGIN = 0.2;
    private static final int TEMP_SAFETY_MARGIN = 2;
    private static final double M_U = ((D_T * 0.0478) + SENSOR_MARGIN); // Upper temperature margin
    private static final double M_L = ((D_T * 0.00952) + SENSOR_MARGIN); // Lower temperaure margin

    public TemperatureController(WashingIO io) {

        this.io = io;
    }

    @Override
    public void run() {

        try {
            int targetTemp = 0;
            boolean ackToBeSent = false;
            WashingMessage previousMessage = new WashingMessage(this, WashingMessage.Order.TEMP_IDLE);
            while (true) {
                // wait for up to 10 (simulated) seconds for a WashingMessage
                WashingMessage message = receiveWithTimeout(TIME_UNIT);
                // if message is null, it means 10 seonds passed and no message was received

                if (message != null) {
                    previousMessage = message;
                    ackToBeSent = true;
                    switch (message.order()) {
                        case TEMP_SET_40:
                            System.out.println("keeping temp at 40");
                            targetTemp = 40;
                            break;
                        case TEMP_SET_60:
                            System.out.println("keeping temp at 60");
                            targetTemp = 60;
                            break;
                        case TEMP_IDLE:
                            System.out.println("temp to ambient");
                            targetTemp = 0;
                            break;
                        default:
                            System.out.println("tempController deafult " + message.order());
                    }
                }

                // Regulates temperature every 10 (D_T) seconds
                if (io.getTemperature() < ((targetTemp - TEMP_SAFETY_MARGIN) + M_L)) { // ml = 0.2952
                    io.heat(true); // turn on heat relay if water temperature is lower than the lower temperature margin
                } else if (io.getTemperature() > (targetTemp - M_U)) { // mu = 0.678
                    io.heat(false); // turn off heat realay if temperature is higher than upper temperature margin
                    if (ackToBeSent) { // send ack when target temperature is reached
                        previousMessage.sender().send(new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT));
                        ackToBeSent = false;
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
