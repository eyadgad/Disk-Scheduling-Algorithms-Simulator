import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public abstract class DiskSchedulingAlgorithm {
    // Legacy constants for backward compatibility
    protected static final int LOWER_CYLINDER = DiskConfig.DEFAULT_LOWER_CYLINDER;
    protected static final int UPPER_CYLINDER = DiskConfig.DEFAULT_UPPER_CYLINDER;
    
    // Configuration
    protected DiskConfig config;
    protected SimulationConfig simConfig;
    
    // Request data
    protected List<Integer> requests;           // Legacy: cylinder numbers only
    protected List<Request> requestsWithTime;   // New: requests with arrival times
    protected int initialPosition;
    protected int movement;
    protected long currentTime;                 // Current simulated time
    protected boolean timeBasedMode;            // Whether to use time-based scheduling
    
    // Result tracking
    protected List<Integer> serviceOrder;       // Order of serviced requests
    protected List<Integer> headPath;           // Full path of head movement
    protected List<Long> serviceTimes;          // Service times for time-based mode
    protected List<Long> arrivalTimes;          // Arrival times for latency calculation
    protected List<Integer> seekDistances;      // Individual seek distances
    protected long executionStartTime;          // For wall-clock timing
    protected long executionEndTime;

    protected static final Logger logger = Logger.getLogger(DiskSchedulingAlgorithm.class.getName());

    static {
        // Configure the logger to display simple output
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleFormatter() {
            @Override
            public synchronized String format(java.util.logging.LogRecord record) {
                return String.format("%s%n", record.getMessage());
            }
        });
        logger.setUseParentHandlers(false); // Remove default handlers
        logger.addHandler(handler);
    }
    
    /**
     * Legacy constructor using cylinder numbers only with default config.
     */
    public DiskSchedulingAlgorithm(List<Integer> requests, int initialPosition) {
        this(requests, initialPosition, DiskConfig.getDefault());
    }
    
    /**
     * Constructor with cylinder numbers and custom DiskConfig.
     */
    public DiskSchedulingAlgorithm(List<Integer> requests, int initialPosition, DiskConfig config) {
        this.config = config != null ? config : DiskConfig.getDefault();
        this.simConfig = new SimulationConfig(this.config);
        this.requests = requests;
        this.requestsWithTime = WorkloadGenerator.fromCylinders(requests);
        this.initialPosition = initialPosition;
        initializeState();
    }
    
    /**
     * Constructor with SimulationConfig.
     */
    public DiskSchedulingAlgorithm(List<Integer> requests, int initialPosition, SimulationConfig simConfig) {
        this.simConfig = simConfig != null ? simConfig : new SimulationConfig();
        this.config = this.simConfig.getDiskConfig();
        this.requests = requests;
        this.requestsWithTime = WorkloadGenerator.fromCylinders(requests);
        this.initialPosition = initialPosition;
        initializeState();
    }
    
    /**
     * Constructor using Request objects with arrival times and default config.
     */
    public DiskSchedulingAlgorithm(List<Request> requests, int initialPosition, boolean timeBasedMode) {
        this(requests, initialPosition, timeBasedMode, DiskConfig.getDefault());
    }
    
    /**
     * Full constructor with Request objects and custom DiskConfig.
     */
    public DiskSchedulingAlgorithm(List<Request> requests, int initialPosition, 
                                    boolean timeBasedMode, DiskConfig config) {
        this.config = config != null ? config : DiskConfig.getDefault();
        this.simConfig = new SimulationConfig(this.config);
        this.requestsWithTime = new ArrayList<>(requests);
        this.requests = new ArrayList<>();
        for (Request req : requests) {
            this.requests.add(req.getCylinder());
        }
        this.initialPosition = initialPosition;
        this.timeBasedMode = timeBasedMode;
        initializeState();
    }
    
    /**
     * Constructor with Request objects and SimulationConfig.
     */
    public DiskSchedulingAlgorithm(List<Request> requests, int initialPosition, 
                                    boolean timeBasedMode, SimulationConfig simConfig) {
        this.simConfig = simConfig != null ? simConfig : new SimulationConfig();
        this.config = this.simConfig.getDiskConfig();
        this.requestsWithTime = new ArrayList<>(requests);
        this.requests = new ArrayList<>();
        for (Request req : requests) {
            this.requests.add(req.getCylinder());
        }
        this.initialPosition = initialPosition;
        this.timeBasedMode = timeBasedMode;
        initializeState();
    }
    
    /**
     * Initialize tracking state.
     */
    private void initializeState() {
        this.movement = 0;
        this.currentTime = 0;
        this.serviceOrder = new ArrayList<>();
        this.headPath = new ArrayList<>();
        this.serviceTimes = new ArrayList<>();
        this.arrivalTimes = new ArrayList<>();
        this.seekDistances = new ArrayList<>();
        this.executionStartTime = 0;
        this.executionEndTime = 0;
        // Record initial position in path
        this.headPath.add(initialPosition);
    }
    
    /**
     * Reset state for re-execution.
     */
    protected void resetState() {
        initializeState();
    }

    /**
     * Execute the algorithm and return total movement (legacy).
     */
    public abstract int execute();
    
    /**
     * Execute the algorithm and return structured result.
     */
    public AlgorithmResult executeWithResult() {
        // Reset state in case of re-execution
        resetState();
        
        // Track execution time
        executionStartTime = System.nanoTime();
        
        // Execute the algorithm
        execute();
        
        executionEndTime = System.nanoTime();
        
        // Build and return result
        return buildResult();
    }
    
    /**
     * Build the AlgorithmResult from current state.
     */
    protected AlgorithmResult buildResult() {
        // Collect arrival times from requests if in time-based mode
        List<Long> arrivals = new ArrayList<>();
        if (timeBasedMode && requestsWithTime != null) {
            for (int cyl : serviceOrder) {
                // Find the request with this cylinder that was serviced
                for (Request req : requestsWithTime) {
                    if (req.getCylinder() == cyl && !arrivals.contains(req.getArrivalTime())) {
                        arrivals.add(req.getArrivalTime());
                        break;
                    }
                }
            }
        }
        
        return new AlgorithmResult(
            getAlgorithmName(),
            initialPosition,
            movement,
            serviceOrder,
            headPath,
            serviceTimes,
            arrivals.isEmpty() ? arrivalTimes : arrivals,
            seekDistances,
            config,
            executionEndTime - executionStartTime
        );
    }
    
    /**
     * Get the algorithm name for display.
     */
    public String getAlgorithmName() {
        return getClass().getSimpleName();
    }
    
    /**
     * Get requests that have arrived by the current time.
     */
    protected List<Request> getArrivedRequests(List<Request> pendingRequests) {
        List<Request> arrived = new ArrayList<>();
        for (Request req : pendingRequests) {
            if (req.getArrivalTime() <= currentTime) {
                arrived.add(req);
            }
        }
        return arrived;
    }
    
    /**
     * Advance time to when the next request arrives.
     */
    protected void advanceTimeToNextArrival(List<Request> pendingRequests) {
        long nextArrival = Long.MAX_VALUE;
        for (Request req : pendingRequests) {
            if (req.getArrivalTime() > currentTime && req.getArrivalTime() < nextArrival) {
                nextArrival = req.getArrivalTime();
            }
        }
        if (nextArrival != Long.MAX_VALUE) {
            currentTime = nextArrival;
        }
    }
    
    // Convenience methods to access config bounds
    protected int getLowerCylinder() {
        return config.getLowerCylinder();
    }
    
    protected int getUpperCylinder() {
        return config.getUpperCylinder();
    }
    
    protected boolean isInitialDirectionRight() {
        return config.isMovingRight();
    }

    /**
     * Record a service event and log it.
     */
    protected void recordService(int position, String algorithmName) {
        // Track seek distance
        if (!headPath.isEmpty()) {
            int lastPos = headPath.get(headPath.size() - 1);
            int seekDist = Math.abs(position - lastPos);
            if (seekDist > 0) {
                seekDistances.add(seekDist);
            }
        }
        
        serviceOrder.add(position);
        headPath.add(position);
        if (timeBasedMode) {
            serviceTimes.add(currentTime);
        }
        if (simConfig.isVerbose()) {
            logger.info("(" + algorithmName + ") Servicing at: " + position);
        }
    }
    
    /**
     * Record a service event with time and log it.
     */
    protected void recordServiceWithTime(int position, String algorithmName, long time) {
        // Track seek distance
        if (!headPath.isEmpty()) {
            int lastPos = headPath.get(headPath.size() - 1);
            int seekDist = Math.abs(position - lastPos);
            if (seekDist > 0) {
                seekDistances.add(seekDist);
            }
        }
        
        serviceOrder.add(position);
        headPath.add(position);
        serviceTimes.add(time);
        if (simConfig.isVerbose()) {
            logger.info("(" + algorithmName + ") Time=" + time + " Servicing at: " + position);
        }
    }
    
    /**
     * Record head movement to a position (without servicing a request).
     */
    protected void recordHeadMove(int position) {
        // Track seek distance for non-service moves too
        if (!headPath.isEmpty()) {
            int lastPos = headPath.get(headPath.size() - 1);
            int seekDist = Math.abs(position - lastPos);
            if (seekDist > 0) {
                seekDistances.add(seekDist);
            }
        }
        headPath.add(position);
    }
    
    /**
     * Record arrival time for a request (for latency tracking).
     */
    protected void recordArrival(long arrivalTime) {
        arrivalTimes.add(arrivalTime);
    }
    
    // Legacy log methods - still work but prefer recordService
    protected void logService(int position, String algorithmName) {
        recordService(position, algorithmName);
    }
    
    protected void logServiceWithTime(int position, String algorithmName, long time) {
        recordServiceWithTime(position, algorithmName, time);
    }
    
    public int getMovement() {
        return movement;
    }
    
    public long getCurrentTime() {
        return currentTime;
    }
    
    public DiskConfig getConfig() {
        return config;
    }
    
    public SimulationConfig getSimConfig() {
        return simConfig;
    }
    
    public List<Integer> getServiceOrder() {
        return new ArrayList<>(serviceOrder);
    }
    
    public List<Integer> getHeadPath() {
        return new ArrayList<>(headPath);
    }
}
