import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class C_SCAN extends DiskSchedulingAlgorithm {

    public C_SCAN(List<Integer> requests, int initialPosition) {
        super(requests, initialPosition);
    }

    @Override
    public int execute() {
        List<Integer> localRequests = new ArrayList<>(requests);
        Collections.sort(localRequests);
        int position = initialPosition;

        while (!localRequests.isEmpty()) {
            if (localRequests.contains(position)) {
                logService(position, "C-SCAN");
                localRequests.remove(Integer.valueOf(position));
            }

            position++;
            movement++;

            if (position > UPPER_CYLINDER) {
                position = LOWER_CYLINDER;
                movement += UPPER_CYLINDER;
            }
        }

        return movement;
    }
}
