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
        int diskSize = upperBound - lowerBound;

        while (!localRequests.isEmpty()) {
            if (localRequests.contains(position)) {
                logService(position, "C-SCAN");
                localRequests.remove(Integer.valueOf(position));
            }

            if (movingRight) {
                position++;
                movement++;
                
                if (position > upperBound) {
                    // Wrap around to start
                    position = lowerBound;
                    movement += diskSize; // Cost of wrap-around
                }
            } else {
                position--;
                movement++;
                
                if (position < lowerBound) {
                    // Wrap around to end
                    position = upperBound;
                    movement += diskSize; // Cost of wrap-around
                }
            }
        }

        return movement;
    }
}
