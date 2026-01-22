import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LOOK extends DiskSchedulingAlgorithm {

    public LOOK(List<Integer> requests, int initialPosition) {
        super(requests, initialPosition);
    }
    
    public LOOK(List<Integer> requests, int initialPosition, DiskConfig config) {
        super(requests, initialPosition, config);
    }

    @Override
    public int execute() {
        List<Integer> localRequests = new ArrayList<>(requests);
        Collections.sort(localRequests);
        int position = initialPosition;
        boolean movingRight = isInitialDirectionRight();
        
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
                logService(position, "LOOK");
            }
        }
        
        // Sort: right ascending, left descending (for reverse traversal)
        Collections.sort(rightRequests);
        Collections.sort(leftRequests, Collections.reverseOrder());
        
        if (movingRight) {
            // Service right side first
            for (int req : rightRequests) {
                movement += req - position;
                position = req;
                logService(position, "LOOK");
            }
            
            // Then reverse and service left side (no need to go to boundary - that's LOOK)
            for (int req : leftRequests) {
                movement += position - req;
                position = req;
                logService(position, "LOOK");
            }
        } else {
            // Service left side first
            for (int req : leftRequests) {
                movement += position - req;
                position = req;
                logService(position, "LOOK");
            }
            
            // Then reverse and service right side
            for (int req : rightRequests) {
                movement += req - position;
                position = req;
                logService(position, "LOOK");
            }
        }

        return movement;
    }
}
