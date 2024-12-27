import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public abstract class DiskSchedulingAlgorithm {
    protected static final int LOWER_CYLINDER = 0;
    protected static final int UPPER_CYLINDER = 4999;
    protected List<Integer> requests;
    protected int initialPosition;
    protected int movement;

    protected static final Logger logger = Logger.getLogger(DiskSchedulingAlgorithm.class.getName());

    static {
        // Configure the logger to display simple output
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleFormatter() {
            @Override
            public synchronized String format(java.util.logging.LogRecord record) {
                return String.format("%s%n", record.getMessage());
            }
        });
        logger.setUseParentHandlers(false); // Remove default handlers
        logger.addHandler(handler);
    }
    public DiskSchedulingAlgorithm(List<Integer> requests, int initialPosition) {
        this.requests = requests;
        this.initialPosition = initialPosition;
        this.movement = 0;
    }

    public abstract int execute();

    protected void logService(int position, String algorithmName) {
        logger.info("(" + algorithmName + ") Servicing at: " + position);
    }
}
