package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;
import wash.simulation.WashingSimulator;

public class Wash {

    public static void main(String[] args) throws InterruptedException {
        WashingSimulator sim = new WashingSimulator(Settings.SPEEDUP);

        WashingIO io = sim.startSimulation();

        TemperatureController temp = new TemperatureController(io);
        WaterController water = new WaterController(io);
        SpinController spin = new SpinController(io);
        ActorThread<WashingMessage> program = null;
        

        temp.start();
        water.start();
        spin.start();
        
        while (true) {
            int n = io.awaitButton();
            System.out.println("user selected program " + n);


            // if the user presses buttons 1-3, start a washing program
            // if the user presses button 0, and a program has been started, stop it

            switch (n) {
                case 0:
                    System.out.println("STOP");
                    program.interrupt();
                    break;
                case 1:
                    System.out.println("PROGRAM 1");
                    program = new WashingProgram1(io, temp, water, spin);
                    program.start();
                    break;
                case 2:
                    System.out.println("PROGRAM 2");
                    program = new WashingProgram2(io, temp, water, spin);
                    program.start();
                    break;
                case 3:
                    System.out.println("PROGRAM 3");
                    program = new WashingProgram3(io, temp, water, spin);
                    program.start();
                    break;
                default:
                    System.out.println("Enter an input in the range [0..3]");
            }
        }
    }
}
