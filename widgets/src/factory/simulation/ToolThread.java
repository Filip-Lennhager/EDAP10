package factory.simulation;

import factory.model.Tool;
import factory.model.Widget;

public class ToolThread extends Thread {

    private FactoryMonitor monitor;
    private Tool tool;
    private Widget widget;

    public ToolThread(FactoryMonitor monitor, Widget widget, Tool tool){

        this.monitor = monitor;
        this.tool = tool;
        this.widget = widget;
    }
    
    @Override public void run() {

        while(true){
            tool.waitFor(widget);
            monitor.enableTool(widget);
            tool.performAction();
            monitor.disableTool(widget);
            monitor.checkToolOn();
        }

    }
}
