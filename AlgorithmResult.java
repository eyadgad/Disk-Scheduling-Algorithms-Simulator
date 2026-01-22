import java.util.ArrayList;
import java.util.List;

/**
 * Structured result from a disk scheduling algorithm execution.
 * Contains total movement, service order, full path, and timing information.
 */
public class AlgorithmResult {
    private final String algorithmName;
    private final int totalMovement;
    private final int initialPosition;
    private final List<Integer> serviceOrder;      // Order in which requests were serviced
    private final List<Integer> headPath;          // Full path of head movement including start
    private final List<Long> serviceTimes;         // Time at which each request was serviced (for time-based)
    private final DiskConfig config;
    
    public AlgorithmResult(String algorithmName, int initialPosition, int totalMovement,
                           List<Integer> serviceOrder, List<Integer> headPath,
                           List<Long> serviceTimes, DiskConfig config) {
        this.algorithmName = algorithmName;
        this.initialPosition = initialPosition;
        this.totalMovement = totalMovement;
        this.serviceOrder = new ArrayList<>(serviceOrder);
        this.headPath = new ArrayList<>(headPath);
        this.serviceTimes = serviceTimes != null ? new ArrayList<>(serviceTimes) : new ArrayList<>();
        this.config = config;
    }
    
    // Getters
    public String getAlgorithmName() { return algorithmName; }
    public int getTotalMovement() { return totalMovement; }
    public int getInitialPosition() { return initialPosition; }
    public List<Integer> getServiceOrder() { return new ArrayList<>(serviceOrder); }
    public List<Integer> getHeadPath() { return new ArrayList<>(headPath); }
    public List<Long> getServiceTimes() { return new ArrayList<>(serviceTimes); }
    public DiskConfig getConfig() { return config; }
    
    public int getRequestCount() { return serviceOrder.size(); }
    
    /**
     * Calculate average seek distance.
     */
    public double getAverageSeekDistance() {
        if (serviceOrder.isEmpty()) return 0;
        return (double) totalMovement / serviceOrder.size();
    }
    
    /**
     * Generate a summary string.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Algorithm: ").append(algorithmName).append("\n");
        sb.append("Total Movement: ").append(totalMovement).append(" cylinders\n");
        sb.append("Requests Serviced: ").append(serviceOrder.size()).append("\n");
        sb.append("Average Seek: ").append(String.format("%.2f", getAverageSeekDistance())).append(" cylinders\n");
        sb.append("Service Order: ").append(serviceOrder).append("\n");
        sb.append("Head Path: ").append(headPath);
        return sb.toString();
    }
    
    /**
     * Generate an ASCII timeline visualization of the disk head movement.
     */
    public String getAsciiTimeline() {
        return getAsciiTimeline(60); // Default width
    }
    
    /**
     * Generate an ASCII timeline with custom width.
     */
    public String getAsciiTimeline(int width) {
        if (headPath.isEmpty()) {
            return "No movement recorded.";
        }
        
        int minCyl = config.getLowerCylinder();
        int maxCyl = config.getUpperCylinder();
        int range = maxCyl - minCyl;
        
        if (range <= 0) {
            return "Invalid cylinder range.";
        }
        
        StringBuilder sb = new StringBuilder();
        
        // Header
        sb.append("\n");
        sb.append("┌─ ").append(algorithmName).append(" Timeline ");
        sb.append("─".repeat(Math.max(0, width - algorithmName.length() - 14))).append("┐\n");
        
        // Scale bar
        sb.append("│ Cylinder: ");
        sb.append(String.format("%-" + (width - 12) + "s", minCyl + " " + "─".repeat(width - 24) + " " + maxCyl));
        sb.append("│\n");
        sb.append("├" + "─".repeat(width) + "┤\n");
        
        // Plot each step
        int step = 0;
        for (int i = 0; i < headPath.size(); i++) {
            int pos = headPath.get(i);
            int plotPos = (int) ((long)(pos - minCyl) * (width - 4) / range);
            plotPos = Math.max(0, Math.min(width - 4, plotPos));
            
            // Determine marker
            char marker;
            String label;
            if (i == 0) {
                marker = 'S';  // Start
                label = "START";
            } else if (serviceOrder.contains(pos) && (i == headPath.size() - 1 || headPath.get(i + 1) != pos)) {
                marker = '*';  // Serviced request
                label = "→" + pos;
            } else {
                marker = '·';  // Intermediate point
                label = "→" + pos;
            }
            
            // Build the line
            sb.append("│ ");
            for (int j = 0; j < width - 2; j++) {
                if (j == plotPos) {
                    sb.append(marker);
                } else if (j < plotPos && i > 0) {
                    int prevPos = headPath.get(i - 1);
                    int prevPlotPos = (int) ((long)(prevPos - minCyl) * (width - 4) / range);
                    prevPlotPos = Math.max(0, Math.min(width - 4, prevPlotPos));
                    if ((j > prevPlotPos && j < plotPos) || (j < prevPlotPos && j > plotPos)) {
                        sb.append('─');
                    } else {
                        sb.append(' ');
                    }
                } else {
                    sb.append(' ');
                }
            }
            sb.append(" │");
            
            // Add label on the right
            if (i == 0) {
                sb.append(" ← Start (").append(pos).append(")");
            } else {
                sb.append(" ← ").append(pos);
                if (!serviceTimes.isEmpty() && i - 1 < serviceTimes.size()) {
                    sb.append(" @t=").append(serviceTimes.get(i - 1));
                }
            }
            sb.append("\n");
            step++;
        }
        
        // Footer
        sb.append("├" + "─".repeat(width) + "┤\n");
        sb.append("│ Total Movement: ").append(String.format("%-" + (width - 18) + "d", totalMovement)).append("│\n");
        sb.append("└" + "─".repeat(width) + "┘\n");
        
        return sb.toString();
    }
    
    /**
     * Generate a simple horizontal ASCII visualization.
     */
    public String getSimpleTimeline() {
        if (headPath.isEmpty()) {
            return "No movement recorded.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(algorithmName).append(": ");
        sb.append(headPath.get(0));
        
        for (int i = 1; i < headPath.size(); i++) {
            int prev = headPath.get(i - 1);
            int curr = headPath.get(i);
            if (curr > prev) {
                sb.append(" → ");
            } else if (curr < prev) {
                sb.append(" ← ");
            } else {
                sb.append(" = ");
            }
            sb.append(curr);
            if (serviceOrder.contains(curr)) {
                sb.append("*");
            }
        }
        
        sb.append(" [Total: ").append(totalMovement).append("]");
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return getSummary();
    }
}
