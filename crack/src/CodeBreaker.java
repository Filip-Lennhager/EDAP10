import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import client.view.ProgressItem;
import client.view.StatusWindow;
import client.view.WorklistItem;
import network.Sniffer;
import network.SnifferCallback;
import rsa.Factorizer;
import rsa.ProgressTracker;

public class CodeBreaker implements SnifferCallback {

    private final JPanel workList;
    private final JPanel progressList;

    private final JProgressBar mainProgressBar;
    private final int PROGRESS_LENGTH = 1000000;

    private ExecutorService threadPool;

    // -----------------------------------------------------------------------

    private CodeBreaker() {

        StatusWindow w = new StatusWindow();
        w.enableErrorChecks();

        threadPool = Executors.newFixedThreadPool(2);
        
        workList = w.getWorkList();
        progressList = w.getProgressList();
        mainProgressBar = w.getProgressBar();
    }

    // -----------------------------------------------------------------------

    public static void main(String[] args) {

        /*
         * Most Swing operations (such as creating view elements) must be performed in
         * the Swing EDT (Event Dispatch Thread).
         * 
         * That's what SwingUtilities.invokeLater is for.
         */

        SwingUtilities.invokeLater(() -> {
            CodeBreaker codeBreaker = new CodeBreaker();
            new Sniffer(codeBreaker).start();
        });
    }

    // -----------------------------------------------------------------------

    /** Called by a Sniffer thread when an encrypted message is obtained. */
    @Override
    public void onMessageIntercepted(String message, BigInteger n) {

        SwingUtilities.invokeLater(() -> {

            WorklistItem item = new WorklistItem(n, message);
            ProgressItem progressItem = new ProgressItem(n, message);
            JButton crackButton = new JButton("BREAK");
            JButton cancelButton = new JButton("Cancel");
            JButton removeButton = createRemoveButton(progressItem);
            progressItem.add(cancelButton);

            crackButton.addActionListener(e -> {
                Runnable task = handleCracker(threadPool, message, n, progressItem, cancelButton);
                Future<?> future = threadPool.submit(task);
                workList.remove(item);
                progressList.add(progressItem);

                mainProgressBar.setMaximum(mainProgressBar.getMaximum() + PROGRESS_LENGTH);
                crackButton.setVisible(false);

                cancelButton.addActionListener(e2 -> {
                    future.cancel(true);

                    progressItem.getTextArea().setText("[Cancelled]");
                    mainProgressBar.setValue(
                            mainProgressBar.getValue() + (PROGRESS_LENGTH - progressItem.getProgressBar().getValue()));
                    progressItem.getProgressBar().setValue(PROGRESS_LENGTH);

                    cancelButton.setVisible(false);
                    progressItem.add(removeButton);

                });

            });
            item.add(crackButton);
            workList.add(item);
        });
    }

    private Runnable handleCracker(ExecutorService threadPool, String message, BigInteger n, ProgressItem progressItem,
            JButton cancelButton) {

        ProgressTracker tracker = new Tracker(mainProgressBar, progressItem);

        Runnable crack = () -> {

            try {
                String decryptedMessage = Factorizer.crack(message, n, tracker);

                SwingUtilities.invokeLater(() -> {
                    cancelButton.setVisible(false);
                    progressItem.getTextArea().setText(decryptedMessage);

                    JButton removeButton = createRemoveButton(progressItem);
                    progressItem.add(removeButton);

                });
            } catch (Throwable t) {
                System.out.println("Item was cancelled");
            }
        };
        return crack;
    }

    private JButton createRemoveButton(ProgressItem progressItem) {
        JButton removeButton = new JButton("REMOVE");
        removeButton.addActionListener(e -> {
            progressList.remove(progressItem);
            mainProgressBar.setValue(mainProgressBar.getValue() - PROGRESS_LENGTH);
            mainProgressBar.setMaximum(mainProgressBar.getMaximum() - PROGRESS_LENGTH);
        });
        return removeButton;
    }

    private static class Tracker implements ProgressTracker {
        private int progress = 0;

        private ProgressItem item;
        private JProgressBar mainProgressBar;

        private Tracker(JProgressBar mainProgressBar, ProgressItem item) {

            this.item = item;
            this.mainProgressBar = mainProgressBar;
        }

        /**
         * Called by Factorizer to indicate progress. The total sum of
         * ppmDelta from all calls will add upp to 1000000 (one million).
         * 
         * @param ppmDelta portion of work done since last call,
         *                 measured in ppm (parts per million)
         */
        @Override
        public void onProgress(int ppmDelta) {

            progress += ppmDelta;
            SwingUtilities.invokeLater(() -> {

                mainProgressBar.setValue(mainProgressBar.getValue() + ppmDelta);
                if (item.getProgressBar().getValue() < 1000000) {
                    item.getProgressBar().setValue(progress);
                }
            });
        }
    }
}
