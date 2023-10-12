package train.simulation;

import train.model.Route;
import train.view.TrainView;

public class TrainSimulation {

    public static void main(String[] args) {

        TrainView view = new TrainView();
        IntersectionMonitor monitor = new IntersectionMonitor();

        Route r1 = view.loadRoute();
        Route r2 = view.loadRoute();
        Route r3 = view.loadRoute();
        int nbrOfCars = 20;

        TrainThread t1 = new TrainThread(monitor, r1, nbrOfCars);
        TrainThread t2 = new TrainThread(monitor, r2, nbrOfCars);
        TrainThread t3 = new TrainThread(monitor, r3, nbrOfCars);

        t1.start();
        t2.start();
        t3.start();
    }
}
