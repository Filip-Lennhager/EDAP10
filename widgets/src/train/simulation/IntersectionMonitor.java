package train.simulation;

import java.util.HashSet;
import java.util.Set;

import train.model.Segment;

public class IntersectionMonitor {

    private Set<Segment> busySegments = new HashSet<>();

    public synchronized void addBusySegment(Segment busySeg) {
        if(busySegments.contains(busySeg)){
            checkIfBusy(busySeg);
        }
        busySegments.add(busySeg);
    }

    public synchronized void removeBusySegment(Segment busySeg) {
        busySegments.remove(busySeg);
        notifyAll();
    }

    private synchronized void checkIfBusy(Segment nextSeg){
        while(busySegments.contains(nextSeg)){
            try {
                wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}

