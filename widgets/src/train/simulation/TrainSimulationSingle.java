package train.simulation;

import java.util.LinkedList;

import train.model.Route;
import train.model.Segment;
import train.view.TrainView;

public class TrainSimulationSingle {

    public static void main(String[] args) {

        TrainView view = new TrainView();
        Route route = view.loadRoute();
        LinkedList<Segment> train = new LinkedList<Segment>();  

        for(int i=0; i<3; i++) {
            Segment next = route.next();
            train.addFirst(next);
            try {
                next.enter();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    
        while(true){ 
            Segment next = route.next();
            train.addFirst(next);
            try {
                next.enter();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            train.removeLast().exit();
        }
    }

}
