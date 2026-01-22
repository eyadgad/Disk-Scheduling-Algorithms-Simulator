import java.util.*;
import java.util.logging.*;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    
    static {
        // Configure simple log format (message only, no timestamps)
        Logger rootLogger = Logger.getLogger("");
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setFormatter(new java.util.logging.Formatter() {
                @Override
                public String format(LogRecord record) {
                    return record.getMessage() + "\n";
                }
            });
        }
    }
    
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
        
        // Output options
        boolean showTimeline = false;
        boolean showSimpleTimeline = false;
        boolean showServiceOrder = false;
        boolean showPath = false;
        boolean showMetrics = false;
        boolean quietMode = false;  // Suppress per-service logging
        
        // Benchmark mode
        boolean benchmarkMode = false;
        int benchmarkIterations = 5;
        int[] benchmarkSizes = {100, 500, 1000, 5000};

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
                    // Output options
                    case "--timeline":
                        showTimeline = true;
                        break;
                    case "--simple-timeline":
                        showSimpleTimeline = true;
                        break;
                    case "--show-order":
                        showServiceOrder = true;
                        break;
                    case "--show-path":
                        showPath = true;
                        break;
                    case "-q":
                    case "--quiet":
                        quietMode = true;
                        break;
                    case "-v":
                    case "--verbose":
                        showTimeline = true;
                        showServiceOrder = true;
                        showPath = true;
                        showMetrics = true;
                        break;
                    case "--metrics":
                        showMetrics = true;
                        break;
                    // Benchmark mode
                    case "--benchmark":
                        benchmarkMode = true;
                        quietMode = true;  // Suppress individual service logs
                        break;
                    case "--benchmark-iterations":
                        benchmarkIterations = Integer.parseInt(args[++i]);
                        break;
                    case "--benchmark-sizes":
                        List<Integer> sizes = new ArrayList<>();
                        for (int j = i + 1; j < args.length && !args[j].startsWith("-"); j++, i++) {
                            sizes.add(Integer.parseInt(args[j]));
                        }
                        benchmarkSizes = sizes.stream().mapToInt(Integer::intValue).toArray();
                        break;
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

        // Create simulation config
        SimulationConfig simConfig = SimulationConfig.builder()
            .diskConfig(config)
            .verbose(!quietMode)
            .recordPath(true)
            .recordTiming(timeBasedMode)
            .nStepSize(nStepSize)
            .build();
        
        // Create output options
        OutputOptions outputOpts = new OutputOptions(showTimeline, showSimpleTimeline, 
                                                      showServiceOrder, showPath, showMetrics);

        if (benchmarkMode) {
            runBenchmark(simConfig, selectedAlgorithms, seed, distribution, 
                        benchmarkIterations, benchmarkSizes);
        } else if (isBatchMode) {
            batchExecution(simConfig, selectedAlgorithms, outputOpts);
        } else if (timeBasedMode && generatedRequests != null) {
            executeAlgorithmsTimeBased(generatedRequests, initialPosition, simConfig, selectedAlgorithms, outputOpts);
        } else {
            executeAlgorithms(requests, initialPosition, simConfig, selectedAlgorithms, outputOpts);
        }

        logger.info("Simulation Complete.");
    }
    
    /**
     * Simple class to hold output options.
     */
    static class OutputOptions {
        final boolean showTimeline;
        final boolean showSimpleTimeline;
        final boolean showServiceOrder;
        final boolean showPath;
        final boolean showMetrics;
        
        OutputOptions(boolean timeline, boolean simpleTl, boolean order, boolean path, boolean metrics) {
            this.showTimeline = timeline;
            this.showSimpleTimeline = simpleTl;
            this.showServiceOrder = order;
            this.showPath = path;
            this.showMetrics = metrics;
        }
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
        System.out.println("Output Options:");
        System.out.println("  --timeline             Show ASCII timeline visualization");
        System.out.println("  --simple-timeline      Show simple inline timeline");
        System.out.println("  --show-order           Show service order");
        System.out.println("  --show-path            Show full head path");
        System.out.println("  --metrics              Show detailed metrics report");
        System.out.println("  -q, --quiet            Suppress per-service logging");
        System.out.println("  -v, --verbose          Show all output (timeline, order, path, metrics)");
        System.out.println();
        System.out.println("Benchmark Mode:");
        System.out.println("  --benchmark            Run benchmark with large randomized workloads");
        System.out.println("  --benchmark-iterations <n>  Number of iterations per size (default: 5)");
        System.out.println("  --benchmark-sizes <list>    Workload sizes to test (default: 100 500 1000 5000)");
        System.out.println();
        System.out.println("Metrics Tracked:");
        System.out.println("  - Total/Average/Max/Min seek distance");
        System.out.println("  - Per-request latency (with arrival times)");
        System.out.println("  - Throughput (requests per time unit)");
        System.out.println("  - Fairness Index (Jain's fairness, 1.0 = perfectly fair)");
        System.out.println("  - Execution time");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java Main -r 100 200 300 400 -i 250");
        System.out.println("  java Main -g -s 12345 -d NORMAL -c 30 --metrics");
        System.out.println("  java Main --lower 0 --upper 999 -i 500 --direction LEFT");
        System.out.println("  java Main -a SCAN C_SCAN LOOK -r 100 200 300");
        System.out.println("  java Main -g -s 42 -d HOTSPOT -c 25 -t 1000 --metrics");
        System.out.println("  java Main --benchmark -s 12345 -d UNIFORM");
        System.out.println("  java Main --benchmark --benchmark-sizes 100 1000 10000 -a SSTF LOOK");
    }

    private static void executeAlgorithms(List<Integer> requests, int initialPosition, 
                                          SimulationConfig simConfig, Set<String> selected,
                                          OutputOptions outputOpts) {
        List<DiskSchedulingAlgorithm> algorithms = createAlgorithms(
            requests, null, initialPosition, simConfig, selected, false);

        List<AlgorithmResult> results = new ArrayList<>();
        
        for (DiskSchedulingAlgorithm algorithm : algorithms) {
            AlgorithmResult result = algorithm.executeWithResult();
            results.add(result);
            displayResult(result, outputOpts);
        }
        
        // Display comparison summary
        if (results.size() > 1) {
            displayComparisonSummary(results);
        }
    }
    
    private static void executeAlgorithmsTimeBased(List<Request> requests, int initialPosition, 
                                                    SimulationConfig simConfig, Set<String> selected,
                                                    OutputOptions outputOpts) {
        List<Integer> cylinders = new ArrayList<>();
        for (Request req : requests) {
            cylinders.add(req.getCylinder());
        }
        
        logger.info("Running algorithms in time-based mode...");
        logger.info("(Note: Only FSCAN and N-Step SCAN use arrival times; others use cylinder order)");
        logger.info("");
        
        List<DiskSchedulingAlgorithm> algorithms = createAlgorithms(
            cylinders, requests, initialPosition, simConfig, selected, true);

        List<AlgorithmResult> results = new ArrayList<>();
        
        for (DiskSchedulingAlgorithm algorithm : algorithms) {
            AlgorithmResult result = algorithm.executeWithResult();
            results.add(result);
            displayResult(result, outputOpts);
        }
        
        // Display comparison summary
        if (results.size() > 1) {
            displayComparisonSummary(results);
        }
    }
    
    private static void displayResult(AlgorithmResult result, OutputOptions opts) {
        String name = result.getAlgorithmName();
        AlgorithmResult.Metrics m = result.getMetrics();
        
        // Basic result
        logger.info(name + " Total Movement: " + result.getTotalMovement());
        logger.info(String.format("  Seek - Avg: %.2f, Max: %d, Min: %d", 
                    m.avgSeek, m.maxSeek, m.minSeek));
        
        if (m.avgLatency > 0) {
            logger.info(String.format("  Latency - Avg: %.2f, Max: %d", m.avgLatency, m.maxLatency));
            logger.info(String.format("  Throughput: %.4f, Fairness: %.4f", m.throughput, m.fairnessIndex));
        }
        
        // Optional outputs
        if (opts.showServiceOrder) {
            logger.info("  Service Order: " + result.getServiceOrder());
        }
        
        if (opts.showPath) {
            logger.info("  Head Path: " + result.getHeadPath());
        }
        
        if (opts.showMetrics) {
            System.out.println(result.getMetricsReport());
        }
        
        if (opts.showSimpleTimeline) {
            logger.info("  " + result.getSimpleTimeline());
        }
        
        if (opts.showTimeline) {
            System.out.println(result.getAsciiTimeline());
        }
        
        logger.info("\n----------------------------------------------");
    }
    
    private static void displayComparisonSummary(List<AlgorithmResult> results) {
        boolean hasLatency = results.stream().anyMatch(r -> r.getMetrics().avgLatency > 0);
        
        logger.info("\n+========================================================================================+");
        logger.info("|                                   COMPARISON SUMMARY                                   |");
        logger.info("+========================================================================================+");
        
        // Find best algorithm by movement
        AlgorithmResult bestMovement = results.get(0);
        AlgorithmResult bestFairness = results.get(0);
        for (AlgorithmResult r : results) {
            if (r.getTotalMovement() < bestMovement.getTotalMovement()) {
                bestMovement = r;
            }
            if (r.getMetrics().fairnessIndex > bestFairness.getMetrics().fairnessIndex) {
                bestFairness = r;
            }
        }
        
        // Display table header
        if (hasLatency) {
            logger.info(String.format("| %-18s | %8s | %7s | %7s | %8s | %8s | %7s |", 
                        "Algorithm", "Movement", "AvgSeek", "MaxSeek", "AvgLat", "Fairness", "vsBest"));
            logger.info("+--------------------+----------+---------+---------+----------+----------+---------+");
        } else {
            logger.info(String.format("| %-22s | %10s | %10s | %10s | %10s |", 
                        "Algorithm", "Movement", "Avg Seek", "Max Seek", "vs Best"));
            logger.info("+------------------------+------------+------------+------------+------------+");
        }
        
        for (AlgorithmResult r : results) {
            AlgorithmResult.Metrics m = r.getMetrics();
            String vsPercent;
            if (r == bestMovement) {
                vsPercent = "BEST";
            } else {
                double pct = ((double) r.getTotalMovement() / bestMovement.getTotalMovement() - 1) * 100;
                vsPercent = String.format("+%.1f%%", pct);
            }
            
            if (hasLatency) {
                logger.info(String.format("| %-18s | %8d | %7.1f | %7d | %8.1f | %8.4f | %7s |",
                            truncate(r.getAlgorithmName(), 18),
                            r.getTotalMovement(),
                            m.avgSeek,
                            m.maxSeek,
                            m.avgLatency,
                            m.fairnessIndex,
                            vsPercent));
            } else {
                logger.info(String.format("| %-22s | %10d | %10.2f | %10d | %10s |",
                            truncate(r.getAlgorithmName(), 22),
                            r.getTotalMovement(),
                            m.avgSeek,
                            m.maxSeek,
                            vsPercent));
            }
        }
        
        if (hasLatency) {
            logger.info("+========================================================================================+");
            logger.info(String.format("| Best Movement: %-25s  Best Fairness: %-25s |",
                        truncate(bestMovement.getAlgorithmName(), 25),
                        truncate(bestFairness.getAlgorithmName(), 25)));
        }
        
        logger.info("+========================================================================================+");
    }
    
    private static String truncate(String s, int maxLen) {
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 2) + "..";
    }
    
    /**
     * Run benchmark mode with large randomized workloads.
     */
    private static void runBenchmark(SimulationConfig simConfig, Set<String> selectedAlgorithms,
                                      long seed, WorkloadGenerator.Distribution distribution,
                                      int iterations, int[] sizes) {
        logger.info("\n+==================================================================================+");
        logger.info("|                              BENCHMARK MODE                                      |");
        logger.info("+==================================================================================+\n");
        
        logger.info("Configuration:");
        logger.info("  Seed: " + seed);
        logger.info("  Distribution: " + distribution);
        logger.info("  Iterations per size: " + iterations);
        logger.info("  Workload sizes: " + Arrays.toString(sizes));
        logger.info("  Algorithms: " + selectedAlgorithms);
        logger.info("");
        
        DiskConfig diskConfig = simConfig.getDiskConfig();
        int nStepSize = simConfig.getNStepSize();
        
        // Results storage: size -> algorithm -> list of results
        Map<Integer, Map<String, List<AlgorithmResult>>> allResults = new LinkedHashMap<>();
        
        for (int size : sizes) {
            allResults.put(size, new LinkedHashMap<>());
            for (String algo : ALL_ALGORITHMS) {
                if (selectedAlgorithms.contains(algo)) {
                    allResults.get(size).put(algo, new ArrayList<>());
                }
            }
        }
        
        // Run benchmarks
        for (int size : sizes) {
            logger.info("Running benchmark with " + size + " requests...");
            
            for (int iter = 0; iter < iterations; iter++) {
                long iterSeed = seed + iter * 1000L + size;
                WorkloadGenerator generator = new WorkloadGenerator(iterSeed, 
                    diskConfig.getLowerCylinder(), diskConfig.getUpperCylinder());
                
                // Generate workload with arrival times for latency measurement
                List<Request> requests = generator.generateWithPoissonArrivals(size, distribution, 10.0);
                
                List<Integer> cylinders = new ArrayList<>();
                for (Request req : requests) {
                    cylinders.add(req.getCylinder());
                }
                
                // Random initial position
                int initialPos = diskConfig.getLowerCylinder() + 
                    new Random(iterSeed).nextInt(diskConfig.getUpperCylinder() - diskConfig.getLowerCylinder());
                
                // Create and run algorithms
                List<DiskSchedulingAlgorithm> algorithms = createAlgorithms(
                    cylinders, requests, initialPos, simConfig, selectedAlgorithms, true);
                
                for (DiskSchedulingAlgorithm algo : algorithms) {
                    AlgorithmResult result = algo.executeWithResult();
                    String algoName = getBaseAlgorithmName(algo);
                    allResults.get(size).get(algoName).add(result);
                }
            }
        }
        
        // Display benchmark results
        displayBenchmarkResults(allResults, sizes, selectedAlgorithms);
    }
    
    private static String getBaseAlgorithmName(DiskSchedulingAlgorithm algo) {
        String name = algo.getClass().getSimpleName();
        // Normalize N_Step_SCAN to just the base name for grouping
        return name;
    }
    
    private static void displayBenchmarkResults(Map<Integer, Map<String, List<AlgorithmResult>>> allResults,
                                                  int[] sizes, Set<String> selectedAlgorithms) {
        logger.info("\n");
        logger.info("+============================================================================================================+");
        logger.info("|                                         BENCHMARK RESULTS                                                 |");
        logger.info("+============================================================================================================+");
        
        // Header
        StringBuilder header = new StringBuilder();
        header.append(String.format("| %-16s |", "Algorithm"));
        for (int size : sizes) {
            header.append(String.format(" %10s |", size + " reqs"));
        }
        header.append(" Notes");
        logger.info(header.toString());
        
        logger.info("+------------------+" + repeatStr("------------+", sizes.length) + "-------------------------------------");
        
        // Results for each algorithm
        for (String algoName : ALL_ALGORITHMS) {
            if (!selectedAlgorithms.contains(algoName)) continue;
            
            StringBuilder row = new StringBuilder();
            row.append(String.format("| %-16s |", truncate(algoName, 16)));
            
            double[] avgMovements = new double[sizes.length];
            int idx = 0;
            
            for (int size : sizes) {
                List<AlgorithmResult> results = allResults.get(size).get(algoName);
                if (results != null && !results.isEmpty()) {
                    // Calculate average metrics
                    double avgMove = results.stream().mapToInt(AlgorithmResult::getTotalMovement).average().orElse(0);
                    avgMovements[idx] = avgMove;
                    row.append(String.format(" %10.0f |", avgMove));
                } else {
                    row.append(String.format(" %10s |", "N/A"));
                }
                idx++;
            }
            
            // Add scaling note
            if (avgMovements.length >= 2 && avgMovements[0] > 0) {
                double scaleFactor = avgMovements[avgMovements.length - 1] / avgMovements[0];
                double sizeScale = (double) sizes[sizes.length - 1] / sizes[0];
                String scaling;
                if (scaleFactor < sizeScale * 0.5) {
                    scaling = "Sub-linear [OK]";
                } else if (scaleFactor < sizeScale * 1.5) {
                    scaling = "Linear";
                } else {
                    scaling = "Super-linear";
                }
                row.append(" ").append(scaling);
            }
            
            logger.info(row.toString());
        }
        
        logger.info("+============================================================================================================+");
        
        // Detailed metrics for largest workload
        int largestSize = sizes[sizes.length - 1];
        logger.info("| Detailed Metrics for " + largestSize + " requests (averages):                                                             |");
        logger.info("+------------------+----------+----------+----------+----------+----------+--------------------------------");
        logger.info(String.format("| %-16s | %8s | %8s | %8s | %8s | %8s | %8s", 
                    "Algorithm", "AvgSeek", "MaxSeek", "AvgLat", "MaxLat", "Fairness", "Time(ms)"));
        logger.info("+------------------+----------+----------+----------+----------+----------+--------------------------------");
        
        for (String algoName : ALL_ALGORITHMS) {
            if (!selectedAlgorithms.contains(algoName)) continue;
            
            List<AlgorithmResult> results = allResults.get(largestSize).get(algoName);
            if (results == null || results.isEmpty()) continue;
            
            // Average all metrics
            double avgSeek = results.stream().mapToDouble(r -> r.getMetrics().avgSeek).average().orElse(0);
            double maxSeek = results.stream().mapToDouble(r -> r.getMetrics().maxSeek).average().orElse(0);
            double avgLat = results.stream().mapToDouble(r -> r.getMetrics().avgLatency).average().orElse(0);
            double maxLat = results.stream().mapToDouble(r -> r.getMetrics().maxLatency).average().orElse(0);
            double fairness = results.stream().mapToDouble(r -> r.getMetrics().fairnessIndex).average().orElse(0);
            double execTime = results.stream().mapToDouble(r -> r.getMetrics().executionTimeMs).average().orElse(0);
            
            logger.info(String.format("| %-16s | %8.1f | %8.0f | %8.1f | %8.0f | %8.4f | %8.3f",
                        truncate(algoName, 16), avgSeek, maxSeek, avgLat, maxLat, fairness, execTime));
        }
        
        logger.info("+============================================================================================================+");
    }
    
    private static String repeatStr(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
    
    private static List<DiskSchedulingAlgorithm> createAlgorithms(
            List<Integer> cylinders, List<Request> requests, int initialPosition,
            SimulationConfig simConfig, Set<String> selected, boolean timeBasedMode) {
        
        List<DiskSchedulingAlgorithm> algorithms = new ArrayList<>();
        DiskConfig config = simConfig.getDiskConfig();
        int nStepSize = simConfig.getNStepSize();
        
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

    private static void batchExecution(SimulationConfig simConfig, Set<String> selected, OutputOptions outputOpts) {
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
                executeAlgorithms(requests, initialPosition, simConfig, selected, outputOpts);
                logger.info("\n==============================================");
                batchCounter++;
            }
        }
    }
}
