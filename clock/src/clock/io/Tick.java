package clock.io;


public class Tick extends Thread{
	private ClockMonitor clock;
	
	public Tick(ClockMonitor clock) {
		this.clock = clock;
	}
	
	@Override
	public void run() {
		long now;
		long t0 = System.currentTimeMillis();
		long targetTime = t0;
		while(true) {	
			targetTime += 1000;
			try {
				now = System.currentTimeMillis();
				System.out.println("Time since start: " + (now - t0) + " ms"); 
				long sleepTime = targetTime-now;
				Thread.sleep(sleepTime);
				System.out.println("Sleep time: " +  (sleepTime) + " ms");
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			clock.tick();
		}
	}
	
}
