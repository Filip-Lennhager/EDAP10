package wash.control;

// This interface represents the contract for receiving messages. Any class 
// implementing this interface should provide a way to receive messages.
@FunctionalInterface
public interface MessageReceiver {
    WashingMessage receive() throws InterruptedException;
}