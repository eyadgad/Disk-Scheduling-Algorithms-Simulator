import java.util.*;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    
    // All available algorithm names
    private static final String[] ALL_ALGORITHMS = {
        "FCFS", "SSTF", "SCAN", "C_SCAN", "LOOK", "C_LOOK", "FSCAN", "N_STEP_SCAN"
    };

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
        long maxArrivalTime = 0;
        boolean timeBasedMode = false;
        int nStepSize = 4;
        
        // Disk configuration options
        int lowerCylinder = DiskConfig.DEFAULT_LOWER_CYLINDER;
        int upperCylinder = DiskConfig.DEFAULT_UPPER_CYLINDER;
        DiskConfig.Direction direction = DiskConfig.DEFAULT_DIRECTION;
        DiskConfig.WrapPolicy wrapPolicy = DiskConfig.DEFAULT_WRAP_POLICY;
        
        // Algorithm selection
        Set<String> selectedAlgorithms = new HashSet<>(); // Empty = all

        // Parse command-line arguments
        for (int i = 0; i < args.length; i++) {
            try {
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
                        if (nStepSize < 1) {
                            logger.warning("N-step size must be >= 1. Using 1.");
                            nStepSize = 1;
                        }
                        break;
                    // Disk configuration options
                    case "--min-cylinder":
                    case "--lower":
                        lowerCylinder = Integer.parseInt(args[++i]);
                        if (lowerCylinder < 0) {
                            logger.warning("Lower cylinder cannot be negative. Using 0.");
                            lowerCylinder = 0;
                        }
                        break;
                    case "--max-cylinder":
                    case "--upper":
                        upperCylinder = Integer.parseInt(args[++i]);
                        break;
                    case "--direction":
                    case "--dir":
                        direction = DiskConfig.Direction.fromString(args[++i]);
                        break;
                    case "--wrap":
                    case "--wrap-policy":
                        wrapPolicy = DiskConfig.WrapPolicy.fromString(args[++i]);
                        break;
                    // Algorithm selection
                    case "-a":
                    case "--algorithms":
                        for (int j = i + 1; j < args.length && !args[j].startsWith("-"); j++, i++) {
                            String algo = args[j].toUpperCase().replace("-", "_");
                            if (isValidAlgorithm(algo)) {
                                selectedAlgorithms.add(algo);
                            } else {
                                logger.warning("Unknown algorithm: " + args[j] + ". Skipping.");
                            }
                        }
                        break;
                    case "--list-algorithms":
                        System.out.println("Available algorithms:");
                        for (String algo : ALL_ALGORITHMS) {
                            System.out.println("  " + algo);
                        }
                        return;
                    case "-h":
                    case "--help":
                        printHelp();
                        return;
                    default:
                        if (args[i].startsWith("-")) {
                            logger.warning("Unknown argument: " + args[i]);
                        }
                        break;
                }
            } catch (NumberFormatException e) {
                logger.severe("Invalid number format for argument " + args[i] + ": " + e.getMessage());
                return;
            } catch (ArrayIndexOutOfBoundsException e) {
                logger.severe("Missing value for argument " + args[i]);
                return;
            }
        }
        
        // Validate disk bounds
        if (upperCylinder <= lowerCylinder) {
            logger.severe("Error: Upper cylinder (" + upperCylinder + ") must be greater than lower cylinder (" + lowerCylinder + ")");
            return;
        }
        
        // Validate initial position
        if (initialPosition < lowerCylinder || initialPosition > upperCylinder) {
            logger.severe("Error: Initial position (" + initialPosition + ") must be within disk bounds [" + 
                         lowerCylinder + ", " + upperCylinder + "]");
            return;
        }
        
        // Create disk configuration
        DiskConfig config;
        try {
            config = new DiskConfig(lowerCylinder, upperCylinder, direction, wrapPolicy);
        } catch (IllegalArgumentException e) {
            logger.severe("Invalid disk configuration: " + e.getMessage());
            return;
        }
        
        // If no algorithms selected, use all
        if (selectedAlgorithms.isEmpty()) {
            selectedAlgorithms.addAll(Arrays.asList(ALL_ALGORITHMS));
        }

        logger.info("=== Disk Scheduling Algorithms Simulator ===");
        
        // Generate workload if requested
        List<Request> generatedRequests = null;
        if (useGenerator) {
            WorkloadGenerator generator = new WorkloadGenerator(seed, lowerCylinder, upperCylinder);
            generatedRequests = generator.generate(requestCount, distribution, maxArrivalTime);
            
            logger.info("Generated Workload:");
            logger.info("  Seed: " + seed);
            logger.info("  Distribution: " + distribution);
            logger.info("  Request Count: " + requestCount);
            logger.info("  Max Arrival Time: " + maxArrivalTime);
            logger.info("  Time-Based Mode: " + timeBasedMode);
            
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
            // Validate requests against disk bounds
            for (int req : requests) {
                if (req < lowerCylinder || req > upperCylinder) {
                    logger.warning("Request " + req + " is outside disk bounds [" + 
                                  lowerCylinder + ", " + upperCylinder + "]. Clamping.");
                }
            }
            logger.info("Input Requests: " + requests);
            generatedRequests = WorkloadGenerator.fromCylinders(requests);
        }
        
        // Log configuration
        logger.info("Disk Configuration:");
        logger.info("  Cylinder Range: [" + lowerCylinder + ", " + upperCylinder + "]");
        logger.info("  Initial Position: " + initialPosition);
        logger.info("  Initial Direction: " + direction);
        logger.info("  Wrap Policy: " + wrapPolicy);
        logger.info("  N-Step Size: " + nStepSize);
        logger.info("  Algorithms: " + selectedAlgorithms);
        logger.info("\n==============================================");

        if (isBatchMode) {
            batchExecution(nStepSize, config, selectedAlgorithms);
        } else if (timeBasedMode && generatedRequests != null) {
            executeAlgorithmsTimeBased(generatedRequests, initialPosition, nStepSize, config, selectedAlgorithms);
        } else {
            executeAlgorithms(requests, initialPosition, nStepSize, config, selectedAlgorithms);
        }

        logger.info("Simulation Complete.");
    }
    
    private static boolean isValidAlgorithm(String name) {
        for (String algo : ALL_ALGORITHMS) {
            if (algo.equals(name)) return true;
        }
        return false;
    }
    
    private static void printHelp() {
        System.out.println("Disk Scheduling Algorithms Simulator");
        System.out.println();
        System.out.println("Usage: java Main [options]");
        System.out.println();
        System.out.println("Basic Options:");
        System.out.println("  -r, --requests <list>      Space-separated list of cylinder numbers");
        System.out.println("  -i, --initial_position <n> Initial head position (default: 1000)");
        System.out.println("  -b, --batch                Run batch mode with predefined test cases");
        System.out.println("  -h, --help                 Show this help message");
        System.out.println();
        System.out.println("Disk Configuration:");
        System.out.println("  --min-cylinder, --lower <n>  Lower cylinder bound (default: 0)");
        System.out.println("  --max-cylinder, --upper <n>  Upper cylinder bound (default: 4999)");
        System.out.println("  --direction, --dir <dir>     Initial sweep direction: LEFT/RIGHT (default: RIGHT)");
        System.out.println("                               Aliases: L/R, UP/DOWN, INCREASING/DECREASING");
        System.out.println("  --wrap, --wrap-policy <p>    Wrap policy for C-SCAN/C-LOOK:");
        System.out.println("                               START (wrap to boundary) or FIRST (wrap to first request)");
        System.out.println();
        System.out.println("Algorithm Selection:");
        System.out.println("  -a, --algorithms <list>    Space-separated list of algorithms to run");
        System.out.println("                             Available: FCFS, SSTF, SCAN, C_SCAN, LOOK, C_LOOK,");
        System.out.println("                                        FSCAN, N_STEP_SCAN");
        System.out.println("  --list-algorithms          List all available algorithms");
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
        System.out.println("  java Main --lower 0 --upper 999 -i 500 --direction LEFT");
        System.out.println("  java Main -a SCAN C_SCAN LOOK -r 100 200 300");
        System.out.println("  java Main --wrap FIRST -a C_LOOK -r 100 500 200");
        System.out.println("  java Main -g -s 42 -d HOTSPOT -c 25 -t 1000");
    }

    private static void executeAlgorithms(List<Integer> requests, int initialPosition, 
                                          int nStepSize, DiskConfig config, Set<String> selected) {
        List<DiskSchedulingAlgorithm> algorithms = createAlgorithms(
            requests, null, initialPosition, nStepSize, config, selected, false);

        for (DiskSchedulingAlgorithm algorithm : algorithms) {
            int movement = algorithm.execute();
            String name = getAlgorithmDisplayName(algorithm);
            logger.info(name + " Total Movement: " + movement);
            logger.info("\n----------------------------------------------");
        }
    }
    
    private static void executeAlgorithmsTimeBased(List<Request> requests, int initialPosition, 
                                                    int nStepSize, DiskConfig config, Set<String> selected) {
        List<Integer> cylinders = new ArrayList<>();
        for (Request req : requests) {
            cylinders.add(req.getCylinder());
        }
        
        logger.info("Running algorithms in time-based mode...");
        logger.info("(Note: Only FSCAN and N-Step SCAN use arrival times; others use cylinder order)");
        logger.info("");
        
        List<DiskSchedulingAlgorithm> algorithms = createAlgorithms(
            cylinders, requests, initialPosition, nStepSize, config, selected, true);

        for (DiskSchedulingAlgorithm algorithm : algorithms) {
            int movement = algorithm.execute();
            String name = getAlgorithmDisplayName(algorithm);
            logger.info(name + " Total Movement: " + movement);
            logger.info("\n----------------------------------------------");
        }
    }
    
    private static List<DiskSchedulingAlgorithm> createAlgorithms(
            List<Integer> cylinders, List<Request> requests, int initialPosition,
            int nStepSize, DiskConfig config, Set<String> selected, boolean timeBasedMode) {
        
        List<DiskSchedulingAlgorithm> algorithms = new ArrayList<>();
        
        if (selected.contains("FCFS")) {
            algorithms.add(new FCFS(cylinders, initialPosition, config));
        }
        if (selected.contains("SSTF")) {
            algorithms.add(new SSTF(cylinders, initialPosition, config));
        }
        if (selected.contains("SCAN")) {
            algorithms.add(new SCAN(cylinders, initialPosition, config));
        }
        if (selected.contains("C_SCAN")) {
            algorithms.add(new C_SCAN(cylinders, initialPosition, config));
        }
        if (selected.contains("LOOK")) {
            algorithms.add(new LOOK(cylinders, initialPosition, config));
        }
        if (selected.contains("C_LOOK")) {
            algorithms.add(new C_LOOK(cylinders, initialPosition, config));
        }
        if (selected.contains("FSCAN")) {
            if (timeBasedMode && requests != null) {
                algorithms.add(new FSCAN(requests, initialPosition, true, config));
            } else {
                algorithms.add(new FSCAN(cylinders, initialPosition, config));
            }
        }
        if (selected.contains("N_STEP_SCAN")) {
            if (timeBasedMode && requests != null) {
                algorithms.add(new N_Step_SCAN(requests, initialPosition, true, nStepSize, config));
            } else {
                algorithms.add(new N_Step_SCAN(cylinders, initialPosition, nStepSize, config));
            }
        }
        
        return algorithms;
    }
    
    private static String getAlgorithmDisplayName(DiskSchedulingAlgorithm algorithm) {
        String name = algorithm.getClass().getSimpleName();
        if (algorithm instanceof N_Step_SCAN) {
            name += " (N=" + ((N_Step_SCAN) algorithm).getStepSize() + ")";
        }
        if (algorithm.timeBasedMode) {
            name += " [time-based]";
        }
        return name;
    }

    private static void batchExecution(int nStepSize, DiskConfig config, Set<String> selected) {
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
                executeAlgorithms(requests, initialPosition, nStepSize, config, selected);
                logger.info("\n==============================================");
                batchCounter++;
            }
        }
    }
}
