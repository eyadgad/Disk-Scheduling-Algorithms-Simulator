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
    
    // Request data
    protected List<Integer> requests;           // Legacy: cylinder numbers only
    protected List<Request> requestsWithTime;   // New: requests with arrival times
    protected int initialPosition;
    protected int movement;
    protected long currentTime;                 // Current simulated time
    protected boolean timeBasedMode;            // Whether to use time-based scheduling

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
     * Constructor with cylinder numbers and custom config.
     */
    public DiskSchedulingAlgorithm(List<Integer> requests, int initialPosition, DiskConfig config) {
        this.config = config != null ? config : DiskConfig.getDefault();
        this.requests = requests;
        this.requestsWithTime = WorkloadGenerator.fromCylinders(requests);
        this.initialPosition = initialPosition;
        this.movement = 0;
        this.currentTime = 0;
        this.timeBasedMode = false;
    }
    
    /**
     * Constructor using Request objects with arrival times and default config.
     */
    public DiskSchedulingAlgorithm(List<Request> requests, int initialPosition, boolean timeBasedMode) {
        this(requests, initialPosition, timeBasedMode, DiskConfig.getDefault());
    }
    
    /**
     * Full constructor with Request objects and custom config.
     */
    public DiskSchedulingAlgorithm(List<Request> requests, int initialPosition, 
                                    boolean timeBasedMode, DiskConfig config) {
        this.config = config != null ? config : DiskConfig.getDefault();
        this.requestsWithTime = new ArrayList<>(requests);
        this.requests = new ArrayList<>();
        for (Request req : requests) {
            this.requests.add(req.getCylinder());
        }
        this.initialPosition = initialPosition;
        this.movement = 0;
        this.currentTime = 0;
        this.timeBasedMode = timeBasedMode;
    }

    public abstract int execute();
    
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

    protected void logService(int position, String algorithmName) {
        logger.info("(" + algorithmName + ") Servicing at: " + position);
    }
    
    protected void logServiceWithTime(int position, String algorithmName, long time) {
        logger.info("(" + algorithmName + ") Time=" + time + " Servicing at: " + position);
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
}
