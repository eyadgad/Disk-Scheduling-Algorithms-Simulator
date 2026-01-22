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

        while (!localRequests.isEmpty()) {
            if (localRequests.contains(position)) {
                logService(position, "SCAN");
                localRequests.remove(Integer.valueOf(position));
            }

            if (movingRight) {
                if (position < upperBound) {
                    position++;
                } else {
                    movingRight = false;
                }
            } else {
                if (position > lowerBound) {
                    position--;
                } else {
                    movingRight = true;
                }
            }
            movement++;
        }
        return movement;
    }
}
