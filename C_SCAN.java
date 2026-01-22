import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class C_SCAN extends DiskSchedulingAlgorithm {

    public C_SCAN(List<Integer> requests, int initialPosition) {
        super(requests, initialPosition);
    }
    
    public C_SCAN(List<Integer> requests, int initialPosition, DiskConfig config) {
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
                logService(position, "C-SCAN");
            }
        }
        
        // Sort both lists ascending
        Collections.sort(rightRequests);
        Collections.sort(leftRequests);
        
        if (movingRight) {
            // Service right side first
            for (int req : rightRequests) {
                movement += req - position;
                position = req;
                logService(position, "C-SCAN");
            }
            
            // If there are requests on the left, wrap around
            if (!leftRequests.isEmpty()) {
                // Go to upper bound
                movement += upperBound - position;
                position = upperBound;
                
                // Wrap to lower bound (this is the wrap distance)
                movement += upperBound - lowerBound;
                position = lowerBound;
                
                // Service left side (now continuing in same direction from lower bound)
                for (int req : leftRequests) {
                    movement += req - position;
                    position = req;
                    logService(position, "C-SCAN");
                }
            }
        } else {
            // Moving left: service left side first (in descending order)
            Collections.sort(leftRequests, Collections.reverseOrder());
            
            for (int req : leftRequests) {
                movement += position - req;
                position = req;
                logService(position, "C-SCAN");
            }
            
            // If there are requests on the right, wrap around
            if (!rightRequests.isEmpty()) {
                // Go to lower bound
                movement += position - lowerBound;
                position = lowerBound;
                
                // Wrap to upper bound (this is the wrap distance)
                movement += upperBound - lowerBound;
                position = upperBound;
                
                // Service right side (in descending order, continuing in same direction)
                Collections.sort(rightRequests, Collections.reverseOrder());
                for (int req : rightRequests) {
                    movement += position - req;
                    position = req;
                    logService(position, "C-SCAN");
                }
            }
        }

        return movement;
    }
}
