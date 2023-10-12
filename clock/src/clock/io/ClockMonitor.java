package clock.io;

import java.util.concurrent.Semaphore;

public class ClockMonitor {
	private ClockOutput output;
	
	private int currentTime = 0;
	private int alarmTime = 0;
	private boolean alarmManuallyTurnedOff = false;
	private static final int ALARM_DURATION = 20;
	private Semaphore mutex = new Semaphore(1);

	private boolean isAlarmOn = false;
	
	public ClockMonitor(ClockOutput output)
	{
		this.output = output;
	}
	
	public void tick()
	{
		acquire();

		if(currentTime == 24*3600-1) //Reset at midnight
			currentTime = 0;
		else
			currentTime ++;
		displayClockTime();

		if(isAlarmOn && !alarmManuallyTurnedOff && ((currentTime >= alarmTime) && (currentTime < alarmTime + ALARM_DURATION))) {
			System.out.println("ALARM"); //Ring alarm
			output.alarm();
		} else if (currentTime >= alarmTime + ALARM_DURATION) {
			alarmManuallyTurnedOff = false;  // Reset the flag after the alarm cycle
		}	
		mutex.release();
	}
	
	public void setClockTime(int h, int min, int s)
	{
		acquire();
		currentTime = h * 3600 + min * 60 + s;
		displayClockTime();
		mutex.release();
		
	}
	
	public void setAlarmTime(int h, int min, int s)
	{
		acquire();
		alarmTime = h * 3600 + min * 60 + s;
		alarmManuallyTurnedOff = false;  // Reset the flag when alarm time is updated
		mutex.release();
		
	}

	public void toggleAlarm() 
	{
		acquire();
		isAlarmOn = !isAlarmOn;
		if (!isAlarmOn && (currentTime >= alarmTime) && (currentTime < alarmTime + ALARM_DURATION)) {
			alarmManuallyTurnedOff = true;
		}
		output.setAlarmIndicator(isAlarmOn);
		mutex.release();
	}
	
	
	private void displayClockTime() 
	{	
		int h = currentTime / 3600;
		int min = ((currentTime - 3600*h)/ 60);
		int s = currentTime % 60;
		output.displayTime(h, min, s);		
	}
	
	private void acquire()
	{
		try {
			mutex.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
