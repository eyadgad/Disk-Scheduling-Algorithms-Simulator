import java.util.*;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // Default values
        List<Integer> requests = Arrays.asList(2069, 98, 183, 37, 122, 14, 124, 65, 67, 1212, 2296, 2800, 544, 1618, 356, 1523, 4965, 3681);
        int initialPosition = 1000;
        boolean isBatchMode = false;
        
        // Workload generation options
        boolean useGenerator = false;
        long seed = System.currentTimeMillis();
        WorkloadGenerator.Distribution distribution = WorkloadGenerator.Distribution.UNIFORM;
        int requestCount = 20;
        long maxArrivalTime = 0;  // 0 = all arrive at once
        boolean timeBasedMode = false;
        int nStepSize = 4;  // N for N-Step SCAN

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
                case "-g":
                case "--generate":
                    useGenerator = true;
                    break;
                case "-s":
                case "--seed":
                    seed = Long.parseLong(args[++i]);
                    break;
                case "-d":
                case "--distribution":
                    String distStr = args[++i].toUpperCase();
                    try {
                        distribution = WorkloadGenerator.Distribution.valueOf(distStr);
                    } catch (IllegalArgumentException e) {
                        logger.warning("Unknown distribution: " + distStr + ". Using UNIFORM.");
                    }
                    break;
                case "-c":
                case "--count":
                    requestCount = Integer.parseInt(args[++i]);
                    break;
                case "-t":
                case "--time-span":
                    maxArrivalTime = Long.parseLong(args[++i]);
                    timeBasedMode = maxArrivalTime > 0;
                    break;
                case "--time-based":
                    timeBasedMode = true;
                    break;
                case "-n":
                case "--n-step":
                    nStepSize = Integer.parseInt(args[++i]);
                    break;
                case "-h":
                case "--help":
                    printHelp();
                    return;
                default:
                    logger.warning("Unknown argument: " + args[i]);
                    break;
            }
        }

        logger.info("=== Disk Scheduling Algorithms Simulator ===");
        
        // Generate workload if requested
        List<Request> generatedRequests = null;
        if (useGenerator) {
            WorkloadGenerator generator = new WorkloadGenerator(seed);
            generatedRequests = generator.generate(requestCount, distribution, maxArrivalTime);
            
            logger.info("Generated Workload:");
            logger.info("  Seed: " + seed);
            logger.info("  Distribution: " + distribution);
            logger.info("  Request Count: " + requestCount);
            logger.info("  Max Arrival Time: " + maxArrivalTime);
            logger.info("  Time-Based Mode: " + timeBasedMode);
            
            // Extract cylinders for display
            List<Integer> cylinders = new ArrayList<>();
            for (Request req : generatedRequests) {
                cylinders.add(req.getCylinder());
            }
            logger.info("  Cylinders: " + cylinders);
            
            if (timeBasedMode) {
                List<Long> arrivalTimes = new ArrayList<>();
                for (Request req : generatedRequests) {
                    arrivalTimes.add(req.getArrivalTime());
                }
                logger.info("  Arrival Times: " + arrivalTimes);
            }
        } else {
            logger.info("Input Requests: " + requests);
            generatedRequests = WorkloadGenerator.fromCylinders(requests);
        }
        
        logger.info("Initial Position: " + initialPosition);
        logger.info("N-Step Size: " + nStepSize);
        logger.info("\n==============================================");

        if (isBatchMode) {
            // Batch mode execution
            batchExecution(nStepSize);
        } else if (timeBasedMode && generatedRequests != null) {
            // Time-based execution with Request objects
            executeAlgorithmsTimeBased(generatedRequests, initialPosition, nStepSize);
        } else {
            // Single execution (legacy mode)
            executeAlgorithms(requests, initialPosition, nStepSize);
        }

        logger.info("Simulation Complete.");
    }
    
    private static void printHelp() {
        System.out.println("Disk Scheduling Algorithms Simulator");
        System.out.println();
        System.out.println("Usage: java Main [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -r, --requests <list>      Space-separated list of cylinder numbers");
        System.out.println("  -i, --initial_position <n> Initial head position (default: 1000)");
        System.out.println("  -b, --batch                Run batch mode with predefined test cases");
        System.out.println();
        System.out.println("Workload Generation:");
        System.out.println("  -g, --generate             Use workload generator instead of -r");
        System.out.println("  -s, --seed <n>             Random seed for reproducibility");
        System.out.println("  -d, --distribution <type>  Distribution: UNIFORM, NORMAL, HOTSPOT");
        System.out.println("  -c, --count <n>            Number of requests to generate (default: 20)");
        System.out.println("  -t, --time-span <n>        Max arrival time span (enables time-based mode)");
        System.out.println("  --time-based               Enable time-based scheduling mode");
        System.out.println("  -n, --n-step <n>           Step size for N-Step SCAN (default: 4)");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java Main -r 100 200 300 400 -i 250");
        System.out.println("  java Main -g -s 12345 -d NORMAL -c 30");
        System.out.println("  java Main -g -s 42 -d HOTSPOT -c 25 -t 1000");
        System.out.println("  java Main -g -s 42 --time-based -n 6");
    }

    private static void executeAlgorithms(List<Integer> requests, int initialPosition, int nStepSize) {
        // List of algorithms
        List<DiskSchedulingAlgorithm> algorithms = Arrays.asList(
                new FCFS(requests, initialPosition),
                new SSTF(requests, initialPosition),
                new SCAN(requests, initialPosition),
                new C_SCAN(requests, initialPosition),
                new LOOK(requests, initialPosition),
                new C_LOOK(requests, initialPosition),
                new FSCAN(requests, initialPosition),
                new N_Step_SCAN(requests, initialPosition, nStepSize)
        );

        // Execute each algorithm and log results
        for (DiskSchedulingAlgorithm algorithm : algorithms) {
            int movement = algorithm.execute();
            String name = algorithm.getClass().getSimpleName();
            if (algorithm instanceof N_Step_SCAN) {
                name += " (N=" + ((N_Step_SCAN) algorithm).getStepSize() + ")";
            }
            logger.info(name + " Total Movement: " + movement);
            logger.info("\n----------------------------------------------");
        }
    }
    
    private static void executeAlgorithmsTimeBased(List<Request> requests, int initialPosition, int nStepSize) {
        // List of algorithms for time-based mode
        // Note: Only FSCAN and N-Step SCAN fully support time-based mode
        // Other algorithms run in legacy mode for comparison
        
        List<Integer> cylinders = new ArrayList<>();
        for (Request req : requests) {
            cylinders.add(req.getCylinder());
        }
        
        logger.info("Running algorithms in time-based mode...");
        logger.info("(Note: Only FSCAN and N-Step SCAN use arrival times; others use cylinder order)");
        logger.info("");
        
        List<DiskSchedulingAlgorithm> algorithms = Arrays.asList(
                new FCFS(cylinders, initialPosition),
                new SSTF(cylinders, initialPosition),
                new SCAN(cylinders, initialPosition),
                new C_SCAN(cylinders, initialPosition),
                new LOOK(cylinders, initialPosition),
                new C_LOOK(cylinders, initialPosition),
                new FSCAN(requests, initialPosition, true),
                new N_Step_SCAN(requests, initialPosition, true, nStepSize)
        );

        // Execute each algorithm and log results
        for (DiskSchedulingAlgorithm algorithm : algorithms) {
            int movement = algorithm.execute();
            String name = algorithm.getClass().getSimpleName();
            if (algorithm instanceof N_Step_SCAN) {
                name += " (N=" + ((N_Step_SCAN) algorithm).getStepSize() + ")";
            }
            if (algorithm.timeBasedMode) {
                name += " [time-based]";
            }
            logger.info(name + " Total Movement: " + movement);
            logger.info("\n----------------------------------------------");
        }
    }

    private static void batchExecution(int nStepSize) {
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
                executeAlgorithms(requests, initialPosition, nStepSize);
                logger.info("\n==============================================");
                batchCounter++;
            }
        }
    }
}
