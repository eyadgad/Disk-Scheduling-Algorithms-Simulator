import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * N-Step SCAN Disk Scheduling Algorithm
 * 
 * Divides the request queue into subqueues of size N.
 * Each subqueue is processed using SCAN algorithm before moving
 * to the next subqueue. This prevents indefinite postponement
 * and provides better response time variance than plain SCAN.
 * 
 * When N=1, this behaves like FCFS.
 * When N is very large, this behaves like SCAN.
 * FSCAN is a special case where N = queue_size (two queues).
 * 
 * In time-based mode, requests are grouped into subqueues of size N
 * as they arrive or become available.
 */
public class N_Step_SCAN extends DiskSchedulingAlgorithm {
    
    private final int stepSize;
    
    /**
     * Constructor with default step size of 4
     */
    public N_Step_SCAN(List<Integer> requests, int initialPosition) {
        this(requests, initialPosition, 4);
    }
    
    /**
     * Constructor with custom step size N
     */
    public N_Step_SCAN(List<Integer> requests, int initialPosition, int stepSize) {
        super(requests, initialPosition);
        this.stepSize = Math.max(1, stepSize);
    }
    
    /**
     * Constructor with custom config
     */
    public N_Step_SCAN(List<Integer> requests, int initialPosition, int stepSize, DiskConfig config) {
        super(requests, initialPosition, config);
        this.stepSize = Math.max(1, stepSize);
    }
    
    /**
     * Constructor for time-based mode with Request objects
     */
    public N_Step_SCAN(List<Request> requests, int initialPosition, boolean timeBasedMode) {
        this(requests, initialPosition, timeBasedMode, 4);
    }
    
    /**
     * Constructor for time-based mode with custom step size
     */
    public N_Step_SCAN(List<Request> requests, int initialPosition, boolean timeBasedMode, int stepSize) {
        super(requests, initialPosition, timeBasedMode);
        this.stepSize = Math.max(1, stepSize);
    }
    
    /**
     * Full constructor with time-based mode, step size, and config
     */
    public N_Step_SCAN(List<Request> requests, int initialPosition, boolean timeBasedMode, 
                        int stepSize, DiskConfig config) {
        super(requests, initialPosition, timeBasedMode, config);
        this.stepSize = Math.max(1, stepSize);
    }
    
    public int getStepSize() {
        return stepSize;
    }
    
    @Override
    public String getAlgorithmName() {
        return "N_Step_SCAN (N=" + stepSize + ")";
    }

    @Override
    public int execute() {
        if (timeBasedMode) {
            return executeTimeBased();
        } else {
            return executeClassic();
        }
    }
    
    /**
     * Classic N-Step SCAN: split requests into subqueues of size N.
     */
    private int executeClassic() {
        Queue<List<Integer>> rotatingQueues = new LinkedList<>();
        
        List<Integer> allRequests = new ArrayList<>(requests);
        
        // Divide requests into chunks of size N
        for (int i = 0; i < allRequests.size(); i += stepSize) {
            int end = Math.min(i + stepSize, allRequests.size());
            List<Integer> subQueue = new ArrayList<>(allRequests.subList(i, end));
            rotatingQueues.add(subQueue);
        }
        
        int position = initialPosition;
        int queueNumber = 1;
        
        logger.info("(N-Step SCAN) Step size N = " + stepSize);
        
        while (!rotatingQueues.isEmpty()) {
            List<Integer> currentQueue = rotatingQueues.poll();
            logger.info("(N-Step SCAN) Processing Subqueue " + queueNumber + ": " + currentQueue);
            
            position = processSCAN(currentQueue, position);
            queueNumber++;
        }
        
        return movement;
    }
    
    /**
     * Time-based N-Step SCAN: collect up to N arrived requests, process with SCAN,
     * then collect next batch.
     */
    private int executeTimeBased() {
        List<Request> pendingRequests = new ArrayList<>(requestsWithTime);
        pendingRequests.sort(Comparator.comparingLong(Request::getArrivalTime));
        
        int position = initialPosition;
        int queueNumber = 1;
        currentTime = 0;
        
        logger.info("(N-Step SCAN) Step size N = " + stepSize);
        
        while (!pendingRequests.isEmpty()) {
            // Collect up to N requests that have arrived
            List<Request> arrivedRequests = getArrivedRequests(pendingRequests);
            
            if (arrivedRequests.isEmpty()) {
                // Wait for next arrival
                advanceTimeToNextArrival(pendingRequests);
                continue;
            }
            
            // Take up to N requests for this subqueue
            List<Request> currentBatch = new ArrayList<>();
            for (int i = 0; i < stepSize && !arrivedRequests.isEmpty(); i++) {
                Request req = arrivedRequests.get(0);
                currentBatch.add(req);
                arrivedRequests.remove(0);
                pendingRequests.remove(req);
            }
            
            // Log queue being processed
            List<Integer> cylinders = new ArrayList<>();
            for (Request r : currentBatch) cylinders.add(r.getCylinder());
            logger.info("(N-Step SCAN) Time=" + currentTime + " Processing Subqueue " + queueNumber + ": " + cylinders);
            
            // Process this batch using SCAN
            position = processSCANTimeBased(currentBatch, position);
            queueNumber++;
        }
        
        return movement;
    }
    
    /**
     * Process requests using SCAN (time-based mode).
     */
    private int processSCANTimeBased(List<Request> batch, int startPosition) {
        List<Integer> leftRequests = new ArrayList<>();
        List<Integer> rightRequests = new ArrayList<>();
        
        for (Request req : batch) {
            if (req.getCylinder() < startPosition) {
                leftRequests.add(req.getCylinder());
            } else {
                rightRequests.add(req.getCylinder());
            }
        }
        
        Collections.sort(rightRequests);
        Collections.sort(leftRequests, Collections.reverseOrder());
        
        int position = startPosition;
        boolean movingRight = isInitialDirectionRight();
        
        List<Integer> firstPass = movingRight ? rightRequests : leftRequests;
        List<Integer> secondPass = movingRight ? leftRequests : rightRequests;
        
        for (int cyl : firstPass) {
            movement += Math.abs(position - cyl);
            currentTime += Math.abs(position - cyl);
            position = cyl;
            logServiceWithTime(position, "N-Step SCAN", currentTime);
        }
        
        for (int cyl : secondPass) {
            movement += Math.abs(position - cyl);
            currentTime += Math.abs(position - cyl);
            position = cyl;
            logServiceWithTime(position, "N-Step SCAN", currentTime);
        }
        
        return position;
    }
    
    /**
     * Process a queue of requests using SCAN algorithm (classic mode)
     */
    private int processSCAN(List<Integer> queueRequests, int startPosition) {
        List<Integer> localRequests = new ArrayList<>(queueRequests);
        Collections.sort(localRequests);
        
        int position = startPosition;
        
        List<Integer> leftRequests = new ArrayList<>();
        List<Integer> rightRequests = new ArrayList<>();
        
        for (int req : localRequests) {
            if (req < position) {
                leftRequests.add(req);
            } else {
                rightRequests.add(req);
            }
        }
        
        Collections.sort(leftRequests, Collections.reverseOrder());
        
        boolean movingRight = isInitialDirectionRight();
        List<Integer> firstPass = movingRight ? rightRequests : leftRequests;
        List<Integer> secondPass = movingRight ? leftRequests : rightRequests;
        
        for (int req : firstPass) {
            movement += Math.abs(position - req);
            position = req;
            logService(position, "N-Step SCAN");
        }
        
        for (int req : secondPass) {
            movement += Math.abs(position - req);
            position = req;
            logService(position, "N-Step SCAN");
        }
        
        return position;
    }
}
