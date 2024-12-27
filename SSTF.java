import java.util.ArrayList;
import java.util.List;

public class SSTF extends DiskSchedulingAlgorithm {

    public SSTF(List<Integer> requests, int initialPosition) {
        super(requests, initialPosition);
    }

    @Override
    public int execute() {
        List<Integer> localRequests = new ArrayList<>(requests);
        int position = initialPosition;

        while (!localRequests.isEmpty()) {
            int closestRequest = localRequests.get(0);
            int closestDistance = Math.abs(position - closestRequest);

            for (int req : localRequests) {
                int distance = Math.abs(position - req);
                if (distance < closestDistance) {
                    closestRequest = req;
                    closestDistance = distance;
                }
            }

            movement += closestDistance;
            position = closestRequest;
            logService(position, "SSTF");
            localRequests.remove(Integer.valueOf(closestRequest));
        }

        return movement;
    }
}
