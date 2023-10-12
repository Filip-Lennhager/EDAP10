package train.simulation;

import train.view.TrainView;

public class TrainSimulationExperiment {

    public static void main(String[] args) {

        TrainView view = new TrainView();
        IntersectionMonitor monitor = new IntersectionMonitor();

        
        int nbrOfRoutes = 20;
        int nbrOfCars = 3; //per train

        for(int i=0; i<nbrOfRoutes; i++) {
            new TrainThread(monitor, view.loadRoute(), nbrOfCars).start();
        }
    }
}
