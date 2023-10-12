package actor;

import java.util.Scanner;

public class ActorThreadExperiment {

    public static void main(String args[]) {

        ActorThread<String> actorThread = new ActorThread<String>();
        actorThread.start();
        try (Scanner scan = new Scanner(System.in)) {
            while (true) {
                System.out.println("Write input:");
                String input = scan.nextLine();
                String output = "";

                try {
                    System.out.println(actorThread.receiveWithTimeout(3000));
                    actorThread.send("ett");
                    actorThread.send("två");
                    actorThread.send("tre");

                    System.out.println(actorThread.receive());
                    System.out.println(actorThread.receive());
                    System.out.println(actorThread.receive());

                    ActorThread.sleep(1000);
                    actorThread.send(input);
                    ActorThread.sleep(1000);
                    output = actorThread.receive();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println("Echo: " + output);
            }
        }
    }
}
