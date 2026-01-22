import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class C_LOOK extends DiskSchedulingAlgorithm {

    public C_LOOK(List<Integer> requests, int initialPosition) {
        super(requests, initialPosition);
    }
    
    public C_LOOK(List<Integer> requests, int initialPosition, DiskConfig config) {
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
        boolean wrapToStart = config.getWrapPolicy() == DiskConfig.WrapPolicy.WRAP_TO_START;
        
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
                logService(position, "C-LOOK");
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
                logService(position, "C-LOOK");
            }
            
            // If there are requests on the left, wrap around
            if (!leftRequests.isEmpty()) {
                int firstLeft = leftRequests.get(0); // smallest request on left
                
                if (wrapToStart) {
                    // LOOK doesn't go to disk boundary, goes to last request
                    // Then wraps: position -> upperBound -> lowerBound -> firstRequest
                    movement += upperBound - position;      // to upper bound
                    movement += upperBound - lowerBound;    // wrap distance
                    position = lowerBound;
                    
                    // Service left side from lower bound
                    for (int req : leftRequests) {
                        movement += req - position;
                        position = req;
                        logService(position, "C-LOOK");
                    }
                } else {
                    // WRAP_TO_FIRST_REQ: jump directly to smallest request
                    // This is the "look" behavior - no wasted movement
                    movement += Math.abs(position - firstLeft);
                    position = firstLeft;
                    logService(position, "C-LOOK");
                    
                    // Service rest of left side
                    for (int i = 1; i < leftRequests.size(); i++) {
                        int req = leftRequests.get(i);
                        movement += req - position;
                        position = req;
                        logService(position, "C-LOOK");
                    }
                }
            }
        } else {
            // Moving left: service left side first (in descending order)
            Collections.sort(leftRequests, Collections.reverseOrder());
            
            for (int req : leftRequests) {
                movement += position - req;
                position = req;
                logService(position, "C-LOOK");
            }
            
            // If there are requests on the right, wrap around
            if (!rightRequests.isEmpty()) {
                int lastRight = rightRequests.get(rightRequests.size() - 1); // largest request
                
                if (wrapToStart) {
                    // Wrap: position -> lowerBound -> upperBound -> service from top
                    movement += position - lowerBound;      // to lower bound
                    movement += upperBound - lowerBound;    // wrap distance
                    position = upperBound;
                    
                    // Service right side from upper bound (descending)
                    Collections.sort(rightRequests, Collections.reverseOrder());
                    for (int req : rightRequests) {
                        movement += position - req;
                        position = req;
                        logService(position, "C-LOOK");
                    }
                } else {
                    // WRAP_TO_FIRST_REQ: jump directly to largest request
                    movement += Math.abs(lastRight - position);
                    position = lastRight;
                    logService(position, "C-LOOK");
                    
                    // Service rest of right side (descending)
                    Collections.sort(rightRequests, Collections.reverseOrder());
                    for (int i = 1; i < rightRequests.size(); i++) {
                        int req = rightRequests.get(i);
                        movement += position - req;
                        position = req;
                        logService(position, "C-LOOK");
                    }
                }
            }
        }

        return movement;
    }
}
