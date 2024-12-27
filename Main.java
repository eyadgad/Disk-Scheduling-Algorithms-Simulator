import java.util.*;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // Default values
        List<Integer> requests = Arrays.asList(2069, 98, 183, 37, 122, 14, 124, 65, 67, 1212, 2296, 2800, 544, 1618, 356, 1523, 4965, 3681);
        int initialPosition = 1000;
        boolean isBatchMode = false;

        // Parse command-line arguments
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-r":
                case "--requests":
                    requests = new ArrayList<>();
                    for (int j = i + 1; j < args.length && !args[j].startsWith("-"); j++, i++) {
                        requests.add(Integer.parseInt(args[j]));
                    }
                    break;
                case "-i":
                case "--initial_position":
                    initialPosition = Integer.parseInt(args[++i]);
                    break;
                case "-b":
                case "--batch":
                    isBatchMode = true;
                    break;
                default:
                    logger.warning("Unknown argument: " + args[i]);
                    break;
            }
        }

        logger.info("=== Disk Scheduling Algorithms Simulator ===");
        logger.info("Input Requests: " + requests);
        logger.info("Initial Position: " + initialPosition);
        logger.info("\n==============================================");

        if (isBatchMode) {
            // Batch mode execution
            batchExecution();
        } else {
            // Single execution
            executeAlgorithms(requests, initialPosition);
        }

        logger.info("Simulation Complete.");
    }

    private static void executeAlgorithms(List<Integer> requests, int initialPosition) {
        // List of algorithms
        List<DiskSchedulingAlgorithm> algorithms = Arrays.asList(
                new FCFS(requests, initialPosition),
                new SSTF(requests, initialPosition),
                new SCAN(requests, initialPosition),
                new C_SCAN(requests, initialPosition),
                new LOOK(requests, initialPosition),
                new C_LOOK(requests, initialPosition)
        );

        // Execute each algorithm and log results
        for (DiskSchedulingAlgorithm algorithm : algorithms) {
            int movement = algorithm.execute();
            logger.info(algorithm.getClass().getSimpleName() + " Total Movement: " + movement);
            logger.info("\n----------------------------------------------");
        }
    }

    private static void batchExecution() {
        List<Integer> batchInitialPositions = Arrays.asList(100, 500, 1000, 2000);
        List<List<Integer>> batchRequests = Arrays.asList(
                Arrays.asList(100, 300, 600, 900),
                Arrays.asList(2069, 98, 183, 37, 122, 14, 124),
                Arrays.asList(124, 250, 500, 750, 1024)
        );

        int batchCounter = 1;

        for (int initialPosition : batchInitialPositions) {
            for (List<Integer> requests : batchRequests) {
                logger.info("Batch " + batchCounter + ": Initial Position: " + initialPosition + ", Requests: " + requests);
                executeAlgorithms(requests, initialPosition);
                logger.info("\n==============================================");
                batchCounter++;
            }
        }
    }
}
