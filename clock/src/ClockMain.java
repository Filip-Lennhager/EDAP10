import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Semaphore;

import clock.AlarmClockEmulator;
import clock.io.Choice;
import clock.io.ClockInput;
import clock.io.ClockInput.UserInput;
import clock.io.ClockMonitor;
import clock.io.ClockOutput;
import clock.io.Tick;

public class ClockMain {
    public static void main(String[] args) throws InterruptedException {
        AlarmClockEmulator emulator = new AlarmClockEmulator();

        ClockInput  in  = emulator.getInput();
        ClockOutput out = emulator.getOutput();

        Semaphore semaphore = in.getSemaphore();

        long currentTime = System.currentTimeMillis();
        Instant instant = Instant.ofEpochMilli(currentTime);
        ZonedDateTime utcZonedDateTime = instant.atZone(ZoneId.of("UTC"));
        ZonedDateTime cetZonedDateTime = utcZonedDateTime.withZoneSameInstant(ZoneId.of("CET"));

        int seconds = cetZonedDateTime.getSecond();
        int minutes = cetZonedDateTime.getMinute();
        int hours = cetZonedDateTime.getHour();

        ClockMonitor clock = new ClockMonitor(out);
        clock.setClockTime(hours, minutes, seconds);
        //clock.setClockTime(0, 0, 0);
        
        Tick tick = new Tick(clock);
        tick.start();

        while (true) {
        	//Väntar på användare
        	semaphore.acquire();
   
            UserInput userInput = in.getUserInput();
            Choice c = userInput.choice();
 
            if(Choice.TOGGLE_ALARM == c) {
            	clock.toggleAlarm();
            }
            if(Choice.SET_ALARM == c) {
            	clock.setAlarmTime(userInput.hours(), userInput.minutes(), userInput.seconds());
            }
            
            if(Choice.SET_TIME == c) { 
            	clock.setClockTime(userInput.hours(), userInput.minutes(), userInput.seconds());
            }
            
        }
    }
}
