import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class C_LOOK extends DiskSchedulingAlgorithm {

    public C_LOOK(List<Integer> requests, int initialPosition) {
        super(requests, initialPosition);
    }

    @Override
    public int execute() {
        List<Integer> localRequests = new ArrayList<>(requests); // Copy requests
        Collections.sort(localRequests); // Sort requests
        int position = initialPosition;

        while (!localRequests.isEmpty()) {
            if (localRequests.contains(position)) {
                logService(position, "C-LOOK");
                localRequests.remove(Integer.valueOf(position));
                if (localRequests.isEmpty()) {
                    break; // Exit if all requests are serviced
                }
            }

            // Move to the next request in the circular order
            Integer nextPosition = null;
            for (int req : localRequests) {
                if (req > position) {
                    nextPosition = req;
                    break;
                }
            }

            if (nextPosition == null) {
                // Wrap around to the smallest request
                nextPosition = localRequests.get(0);
                movement += Math.abs(position - UPPER_CYLINDER); // Move to the end
                movement += Math.abs(UPPER_CYLINDER - LOWER_CYLINDER); // Wrap around
                position = LOWER_CYLINDER;
            }

            movement += Math.abs(position - nextPosition);
            position = nextPosition;
        }

        return movement;
    }

}
