import java.util.List;

public class FCFS extends DiskSchedulingAlgorithm {

    public FCFS(List<Integer> requests, int initialPosition) {
        super(requests, initialPosition);
    }
    
    public FCFS(List<Integer> requests, int initialPosition, DiskConfig config) {
        super(requests, initialPosition, config);
    }

    @Override
    public int execute() {
        int position = initialPosition;
        for (int request : requests) {
            movement += Math.abs(position - request);
            position = request;
            logService(position, "FCFS");
        }
        return movement;
    }
}
