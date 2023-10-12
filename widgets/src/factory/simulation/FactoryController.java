package factory.simulation;

import factory.model.Conveyor;
import factory.model.Tool;
import factory.model.Widget;

public class FactoryController {
    
    public static void main(String[] args) {
        Factory factory = new Factory();

        Conveyor conveyor = factory.getConveyor();
        Tool press = factory.getPressTool();
        Tool paint = factory.getPaintTool();
        FactoryMonitor monitor = new FactoryMonitor(conveyor);

        ToolThread pressThread = new ToolThread(monitor, Widget.GREEN_BLOB, press);
        ToolThread paintThread = new ToolThread(monitor, Widget.BLUE_MARBLE, paint);
        pressThread.start();
        paintThread.start();
    }
}
