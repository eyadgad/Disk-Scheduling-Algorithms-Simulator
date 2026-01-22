import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * FSCAN (Freeze SCAN) Disk Scheduling Algorithm
 * 
 * Uses two queues: one active queue being serviced using SCAN,
 * and one holding queue for new requests. When the active queue
 * is exhausted, the queues are swapped (rotated).
 * 
 * In time-based mode: requests that arrive while processing the active queue
 * are added to the holding queue. When the active queue is done, queues swap.
 * 
 * In non-time-based mode: initial requests are split into two queues
 * to demonstrate the two-queue rotation mechanism.
 */
public class FSCAN extends DiskSchedulingAlgorithm {

    public FSCAN(List<Integer> requests, int initialPosition) {
        super(requests, initialPosition);
    }
    
    public FSCAN(List<Integer> requests, int initialPosition, DiskConfig config) {
        super(requests, initialPosition, config);
    }
    
    public FSCAN(List<Request> requests, int initialPosition, boolean timeBasedMode) {
        super(requests, initialPosition, timeBasedMode);
    }
    
    public FSCAN(List<Request> requests, int initialPosition, boolean timeBasedMode, DiskConfig config) {
        super(requests, initialPosition, timeBasedMode, config);
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
     * Classic FSCAN: split requests into two queues without considering arrival times.
     */
    private int executeClassic() {
        Queue<List<Integer>> rotatingQueues = new LinkedList<>();
        
        // Divide requests: first half goes to queue 1, second half to queue 2
        List<Integer> queue1 = new ArrayList<>();
        List<Integer> queue2 = new ArrayList<>();
        
        for (int i = 0; i < requests.size(); i++) {
            if (i < requests.size() / 2) {
                queue1.add(requests.get(i));
            } else {
                queue2.add(requests.get(i));
            }
        }
        
        if (!queue1.isEmpty()) {
            rotatingQueues.add(queue1);
        }
        if (!queue2.isEmpty()) {
            rotatingQueues.add(queue2);
        }
        
        int position = initialPosition;
        int queueNumber = 1;
        
        while (!rotatingQueues.isEmpty()) {
            List<Integer> currentQueue = rotatingQueues.poll();
            logger.info("(FSCAN) Processing Queue " + queueNumber + ": " + currentQueue);
            
            position = processSCAN(currentQueue, position);
            queueNumber++;
        }
        
        return movement;
    }
    
    /**
     * Time-based FSCAN: uses arrival times to determine when requests enter queues.
     * While processing the active queue, new arrivals go to the holding queue.
     */
    private int executeTimeBased() {
        List<Request> pendingRequests = new ArrayList<>(requestsWithTime);
        pendingRequests.sort(Comparator.comparingLong(Request::getArrivalTime));
        
        List<Request> activeQueue = new ArrayList<>();
        List<Request> holdingQueue = new ArrayList<>();
        
        int position = initialPosition;
        int queueNumber = 1;
        
        // Initially, get all requests that have arrived at time 0
        currentTime = 0;
        for (Request req : new ArrayList<>(pendingRequests)) {
            if (req.getArrivalTime() <= currentTime) {
                activeQueue.add(req);
                pendingRequests.remove(req);
            }
        }
        
        while (!activeQueue.isEmpty() || !pendingRequests.isEmpty() || !holdingQueue.isEmpty()) {
            // If active queue is empty, try to get more requests
            if (activeQueue.isEmpty()) {
                if (!holdingQueue.isEmpty()) {
                    // Swap: holding becomes active
                    activeQueue = holdingQueue;
                    holdingQueue = new ArrayList<>();
                } else if (!pendingRequests.isEmpty()) {
                    // Wait for next arrival
                    advanceTimeToNextArrival(pendingRequests);
                    for (Request req : new ArrayList<>(pendingRequests)) {
                        if (req.getArrivalTime() <= currentTime) {
                            activeQueue.add(req);
                            pendingRequests.remove(req);
                        }
                    }
                }
            }
            
            if (activeQueue.isEmpty()) {
                break;
            }
            
            // Log queue being processed
            List<Integer> cylinders = new ArrayList<>();
            for (Request r : activeQueue) cylinders.add(r.getCylinder());
            logger.info("(FSCAN) Time=" + currentTime + " Processing Queue " + queueNumber + ": " + cylinders);
            
            // Process active queue using SCAN, while collecting new arrivals to holding queue
            position = processSCANTimeBased(activeQueue, holdingQueue, pendingRequests, position);
            activeQueue.clear();
            queueNumber++;
        }
        
        return movement;
    }
    
    /**
     * Process requests using SCAN, adding new arrivals to holding queue.
     */
    private int processSCANTimeBased(List<Request> activeQueue, List<Request> holdingQueue,
                                      List<Request> pendingRequests, int startPosition) {
        List<Integer> leftRequests = new ArrayList<>();
        List<Integer> rightRequests = new ArrayList<>();
        
        for (Request req : activeQueue) {
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
        
        // Service based on initial direction
        List<Integer> firstPass = movingRight ? rightRequests : leftRequests;
        List<Integer> secondPass = movingRight ? leftRequests : rightRequests;
        
        for (int cyl : firstPass) {
            collectArrivals(holdingQueue, pendingRequests);
            movement += Math.abs(position - cyl);
            currentTime += Math.abs(position - cyl);
            position = cyl;
            logServiceWithTime(position, "FSCAN", currentTime);
        }
        
        for (int cyl : secondPass) {
            collectArrivals(holdingQueue, pendingRequests);
            movement += Math.abs(position - cyl);
            currentTime += Math.abs(position - cyl);
            position = cyl;
            logServiceWithTime(position, "FSCAN", currentTime);
        }
        
        return position;
    }
    
    /**
     * Collect any pending requests that have arrived by current time into holding queue.
     */
    private void collectArrivals(List<Request> holdingQueue, List<Request> pendingRequests) {
        for (Request req : new ArrayList<>(pendingRequests)) {
            if (req.getArrivalTime() <= currentTime) {
                holdingQueue.add(req);
                pendingRequests.remove(req);
            }
        }
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
            logService(position, "FSCAN");
        }
        
        for (int req : secondPass) {
            movement += Math.abs(position - req);
            position = req;
            logService(position, "FSCAN");
        }
        
        return position;
    }
}
