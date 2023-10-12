package factory.simulation;

import factory.model.Conveyor;
import factory.model.Widget;

public class FactoryMonitor {
    
    private boolean painting = false;
    private boolean pressing = false;
    private Conveyor conveyor;

   public FactoryMonitor(Conveyor conveyor){
        this.conveyor = conveyor;
   }

    public synchronized void enableTool(Widget widget){
        if(widget == Widget.GREEN_BLOB)
            pressing = true;
        if(widget == Widget.BLUE_MARBLE)
            painting = true;
        conveyor.off();
    }


   public synchronized void disableTool(Widget widget){
        if(widget == Widget.GREEN_BLOB)
            pressing = false;
        if(widget == Widget.BLUE_MARBLE)
            painting = false;
        notifyAll();
    }

    public synchronized void checkToolOn(){
        while(painting || pressing) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        conveyor.on();
    }
}
