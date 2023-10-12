package train.simulation;

import java.util.LinkedList;

import train.model.Route;
import train.model.Segment;

public class TrainThread extends Thread{

    private IntersectionMonitor monitor;
    private Route route;
    private LinkedList<Segment> train;
    private int nbrOfCars;

    public TrainThread(IntersectionMonitor monitor, Route route, int nbrOfCars){
        this.monitor = monitor;
        train = new LinkedList<Segment>();   
        this.route = route;
        this.nbrOfCars = nbrOfCars;     
    }
    
    @Override public void run(){
        addCars(nbrOfCars);
        while(true){
           move(route);
        }
    }

    private void addCars(int nbrOfCars){
        for(int i = 0; i < nbrOfCars; i++) {
            Segment next = route.next();
            train.addFirst(next);
            monitor.addBusySegment(next);
            try {
                next.enter();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void move(Route route) {
        Segment next = route.next();
       
        monitor.addBusySegment(next);
        train.addFirst(next);
        try {
            next.enter();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Segment last =train.removeLast();
        last.exit();
        monitor.removeBusySegment(last);
        
           
   }

   
}
