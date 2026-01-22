import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SCAN extends DiskSchedulingAlgorithm {

    public SCAN(List<Integer> requests, int initialPosition) {
        super(requests, initialPosition);
    }
    
    public SCAN(List<Integer> requests, int initialPosition, DiskConfig config) {
        super(requests, initialPosition, config);
    }

    @Override
    public int execute() {
        List<Integer> localRequests = new ArrayList<>(requests);
        Collections.sort(localRequests);
        int position = initialPosition;
        boolean movingRight = isInitialDirectionRight();
        
        int upperBound = getUpperCylinder();
        int lowerBound = getLowerCylinder();
        
        // Separate requests into left and right of initial position
        List<Integer> leftRequests = new ArrayList<>();
        List<Integer> rightRequests = new ArrayList<>();
        
        for (int req : localRequests) {
            if (req < position) {
                leftRequests.add(req);
            } else if (req > position) {
                rightRequests.add(req);
            } else {
                // Request at current position - service immediately
                logService(position, "SCAN");
            }
        }
        
        // Sort: right ascending, left descending
        Collections.sort(rightRequests);
        Collections.sort(leftRequests, Collections.reverseOrder());
        
        if (movingRight) {
            // Service right side first, then reverse to left
            position = serviceRequests(rightRequests, position, true);
            
            if (!leftRequests.isEmpty()) {
                // Must go to upper bound before reversing (SCAN behavior)
                if (!rightRequests.isEmpty() || position < upperBound) {
                    movement += upperBound - position;
                    position = upperBound;
                }
                // Now reverse and service left side
                position = serviceRequests(leftRequests, position, false);
            }
        } else {
            // Service left side first, then reverse to right
            position = serviceRequests(leftRequests, position, false);
            
            if (!rightRequests.isEmpty()) {
                // Must go to lower bound before reversing (SCAN behavior)
                if (!leftRequests.isEmpty() || position > lowerBound) {
                    movement += position - lowerBound;
                    position = lowerBound;
                }
                // Now reverse and service right side
                position = serviceRequests(rightRequests, position, true);
            }
        }
        
        return movement;
    }
    
    /**
     * Service a list of requests in order, jumping directly between them.
     * Returns the final position after servicing all requests.
     */
    private int serviceRequests(List<Integer> requests, int startPosition, boolean ascending) {
        int position = startPosition;
        for (int req : requests) {
            int distance = Math.abs(req - position);
            movement += distance;
            position = req;
            logService(position, "SCAN");
        }
        return position;
    }
}
