import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LOOK extends DiskSchedulingAlgorithm {

    public LOOK(List<Integer> requests, int initialPosition) {
        super(requests, initialPosition);
    }

    @Override
    public int execute() {
        List<Integer> localRequests = new ArrayList<>(requests);
        Collections.sort(localRequests);
        int position = initialPosition;
        boolean movingRight = true;

        while (!localRequests.isEmpty()) {
            final int curPosition = position;
            if (localRequests.contains(position)) {
                logService(position, "LOOK");
                localRequests.remove(Integer.valueOf(position));
            }

            if (movingRight) {
                Integer nextRequest = localRequests.stream().filter(req -> req > curPosition).findFirst().orElse(null);
                if (nextRequest == null) {
                    movingRight = false;
                } else {
                    movement += Math.abs(position - nextRequest);
                    position = nextRequest;
                }
            } else {
                Integer nextRequest = localRequests.stream().filter(req -> req < curPosition).findFirst().orElse(null);
                if (nextRequest == null) {
                    movingRight = true;
                } else {
                    movement += Math.abs(position - nextRequest);
                    position = nextRequest;
                }
            }
        }

        return movement;
    }
}
