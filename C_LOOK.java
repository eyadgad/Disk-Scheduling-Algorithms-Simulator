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

        while (!localRequests.isEmpty()) {
            if (localRequests.contains(position)) {
                logService(position, "C-LOOK");
                localRequests.remove(Integer.valueOf(position));
                if (localRequests.isEmpty()) {
                    break;
                }
            }

            Integer nextPosition = null;
            
            if (movingRight) {
                // Find next request in right direction
                for (int req : localRequests) {
                    if (req > position) {
                        nextPosition = req;
                        break;
                    }
                }
                
                if (nextPosition == null) {
                    // Wrap around to the smallest request
                    nextPosition = localRequests.get(0);
                    if (wrapToStart) {
                        // Go to upper bound, then wrap to lower bound
                        movement += Math.abs(position - upperBound);
                        movement += Math.abs(upperBound - lowerBound);
                        position = lowerBound;
                    } else {
                        // Jump directly to first request (WRAP_TO_FIRST_REQ)
                        movement += Math.abs(position - nextPosition);
                        position = nextPosition;
                        continue;
                    }
                }
            } else {
                // Find next request in left direction (descending order)
                for (int i = localRequests.size() - 1; i >= 0; i--) {
                    int req = localRequests.get(i);
                    if (req < position) {
                        nextPosition = req;
                        break;
                    }
                }
                
                if (nextPosition == null) {
                    // Wrap around to the largest request
                    nextPosition = localRequests.get(localRequests.size() - 1);
                    if (wrapToStart) {
                        // Go to lower bound, then wrap to upper bound
                        movement += Math.abs(position - lowerBound);
                        movement += Math.abs(upperBound - lowerBound);
                        position = upperBound;
                    } else {
                        // Jump directly to last request
                        movement += Math.abs(position - nextPosition);
                        position = nextPosition;
                        continue;
                    }
                }
            }

            movement += Math.abs(position - nextPosition);
            position = nextPosition;
        }

        return movement;
    }
}
