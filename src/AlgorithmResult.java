import java.util.ArrayList;
import java.util.List;

/**
 * Structured result from a disk scheduling algorithm execution.
 * Contains total movement, service order, full path, timing, and comprehensive metrics.
 */
public class AlgorithmResult {
    private final String algorithmName;
    private final int totalMovement;
    private final int initialPosition;
    private final List<Integer> serviceOrder;      // Order in which requests were serviced
    private final List<Integer> headPath;          // Full path of head movement including start
    private final List<Long> serviceTimes;         // Time at which each request was serviced
    private final List<Long> arrivalTimes;         // When each request arrived (for latency calc)
    private final List<Integer> seekDistances;     // Individual seek distances
    private final DiskConfig config;
    private final long executionTimeNanos;         // Wall-clock execution time
    
    // Cached metrics
    private Metrics cachedMetrics = null;
    
    public AlgorithmResult(String algorithmName, int initialPosition, int totalMovement,
                           List<Integer> serviceOrder, List<Integer> headPath,
                           List<Long> serviceTimes, DiskConfig config) {
        this(algorithmName, initialPosition, totalMovement, serviceOrder, headPath,
             serviceTimes, null, null, config, 0);
    }
    
    public AlgorithmResult(String algorithmName, int initialPosition, int totalMovement,
                           List<Integer> serviceOrder, List<Integer> headPath,
                           List<Long> serviceTimes, List<Long> arrivalTimes,
                           List<Integer> seekDistances, DiskConfig config,
                           long executionTimeNanos) {
        this.algorithmName = algorithmName;
        this.initialPosition = initialPosition;
        this.totalMovement = totalMovement;
        this.serviceOrder = new ArrayList<>(serviceOrder);
        this.headPath = new ArrayList<>(headPath);
        this.serviceTimes = serviceTimes != null ? new ArrayList<>(serviceTimes) : new ArrayList<>();
        this.arrivalTimes = arrivalTimes != null ? new ArrayList<>(arrivalTimes) : new ArrayList<>();
        this.seekDistances = seekDistances != null ? new ArrayList<>(seekDistances) : new ArrayList<>();
        this.config = config;
        this.executionTimeNanos = executionTimeNanos;
    }
    
    // Getters
    public String getAlgorithmName() { return algorithmName; }
    public int getTotalMovement() { return totalMovement; }
    public int getInitialPosition() { return initialPosition; }
    public List<Integer> getServiceOrder() { return new ArrayList<>(serviceOrder); }
    public List<Integer> getHeadPath() { return new ArrayList<>(headPath); }
    public List<Long> getServiceTimes() { return new ArrayList<>(serviceTimes); }
    public List<Long> getArrivalTimes() { return new ArrayList<>(arrivalTimes); }
    public List<Integer> getSeekDistances() { return new ArrayList<>(seekDistances); }
    public DiskConfig getConfig() { return config; }
    public long getExecutionTimeNanos() { return executionTimeNanos; }
    
    public int getRequestCount() { return serviceOrder.size(); }
    
    /**
     * Get comprehensive metrics.
     */
    public Metrics getMetrics() {
        if (cachedMetrics == null) {
            cachedMetrics = new Metrics();
        }
        return cachedMetrics;
    }
    
    /**
     * Calculate average seek distance.
     */
    public double getAverageSeekDistance() {
        return getMetrics().avgSeek;
    }
    
    /**
     * Comprehensive metrics class.
     */
    public class Metrics {
        public final double avgSeek;
        public final int maxSeek;
        public final int minSeek;
        public final double seekStdDev;
        
        public final double avgLatency;      // Average time from arrival to service
        public final long maxLatency;
        public final long minLatency;
        public final double latencyStdDev;
        
        public final double throughput;      // Requests per time unit
        public final double fairnessIndex;   // Jain's fairness index
        public final double responseTimeVariance;
        
        public final long totalSimulatedTime;
        public final double executionTimeMs;
        
        Metrics() {
            int n = serviceOrder.size();
            
            // Seek metrics
            if (seekDistances.isEmpty() && headPath.size() > 1) {
                // Calculate from head path if not provided
                for (int i = 1; i < headPath.size(); i++) {
                    seekDistances.add(Math.abs(headPath.get(i) - headPath.get(i - 1)));
                }
            }
            
            if (!seekDistances.isEmpty()) {
                int sumSeek = 0;
                int maxS = Integer.MIN_VALUE;
                int minS = Integer.MAX_VALUE;
                for (int s : seekDistances) {
                    sumSeek += s;
                    maxS = Math.max(maxS, s);
                    minS = Math.min(minS, s);
                }
                this.avgSeek = (double) sumSeek / seekDistances.size();
                this.maxSeek = maxS;
                this.minSeek = minS;
                
                // Standard deviation
                double sumSqDiff = 0;
                for (int s : seekDistances) {
                    sumSqDiff += Math.pow(s - avgSeek, 2);
                }
                this.seekStdDev = Math.sqrt(sumSqDiff / seekDistances.size());
            } else {
                this.avgSeek = n > 0 ? (double) totalMovement / n : 0;
                this.maxSeek = totalMovement;
                this.minSeek = 0;
                this.seekStdDev = 0;
            }
            
            // Latency metrics (time from arrival to service)
            List<Long> latencies = new ArrayList<>();
            if (!serviceTimes.isEmpty() && !arrivalTimes.isEmpty() && 
                serviceTimes.size() == arrivalTimes.size()) {
                for (int i = 0; i < serviceTimes.size(); i++) {
                    latencies.add(serviceTimes.get(i) - arrivalTimes.get(i));
                }
            } else if (!serviceTimes.isEmpty()) {
                // Assume arrival at time 0 for all
                latencies.addAll(serviceTimes);
            }
            
            if (!latencies.isEmpty()) {
                long sumLat = 0;
                long maxL = Long.MIN_VALUE;
                long minL = Long.MAX_VALUE;
                for (long l : latencies) {
                    sumLat += l;
                    maxL = Math.max(maxL, l);
                    minL = Math.min(minL, l);
                }
                this.avgLatency = (double) sumLat / latencies.size();
                this.maxLatency = maxL;
                this.minLatency = minL;
                
                // Latency standard deviation
                double sumSqDiff = 0;
                for (long l : latencies) {
                    sumSqDiff += Math.pow(l - avgLatency, 2);
                }
                this.latencyStdDev = Math.sqrt(sumSqDiff / latencies.size());
                this.responseTimeVariance = sumSqDiff / latencies.size();
            } else {
                this.avgLatency = 0;
                this.maxLatency = 0;
                this.minLatency = 0;
                this.latencyStdDev = 0;
                this.responseTimeVariance = 0;
            }
            
            // Throughput and total time
            if (!serviceTimes.isEmpty()) {
                long maxTime = 0;
                for (long t : serviceTimes) {
                    maxTime = Math.max(maxTime, t);
                }
                this.totalSimulatedTime = maxTime;
                this.throughput = maxTime > 0 ? (double) n / maxTime : n;
            } else {
                this.totalSimulatedTime = totalMovement; // Approximate as 1 time unit per cylinder
                this.throughput = totalMovement > 0 ? (double) n / totalMovement : 0;
            }
            
            // Jain's Fairness Index: (sum(xi))^2 / (n * sum(xi^2))
            // Applied to latencies - 1.0 is perfectly fair, lower is less fair
            if (!latencies.isEmpty() && latencies.size() > 1) {
                double sumX = 0;
                double sumX2 = 0;
                for (long l : latencies) {
                    sumX += l;
                    sumX2 += (double) l * l;
                }
                this.fairnessIndex = (sumX * sumX) / (latencies.size() * sumX2);
            } else {
                this.fairnessIndex = 1.0; // Perfect fairness with 0-1 requests
            }
            
            this.executionTimeMs = executionTimeNanos / 1_000_000.0;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Metrics ===\n");
            sb.append(String.format("Seek - Avg: %.2f, Max: %d, Min: %d, StdDev: %.2f\n",
                      avgSeek, maxSeek, minSeek, seekStdDev));
            if (avgLatency > 0) {
                sb.append(String.format("Latency - Avg: %.2f, Max: %d, Min: %d, StdDev: %.2f\n",
                          avgLatency, maxLatency, minLatency, latencyStdDev));
            }
            sb.append(String.format("Throughput: %.4f req/time-unit\n", throughput));
            sb.append(String.format("Fairness Index: %.4f (1.0 = perfectly fair)\n", fairnessIndex));
            if (executionTimeMs > 0) {
                sb.append(String.format("Execution Time: %.3f ms\n", executionTimeMs));
            }
            return sb.toString();
        }
    }
    
    /**
     * Generate a summary string.
     */
    public String getSummary() {
        Metrics m = getMetrics();
        StringBuilder sb = new StringBuilder();
        sb.append("Algorithm: ").append(algorithmName).append("\n");
        sb.append("Requests Serviced: ").append(serviceOrder.size()).append("\n");
        sb.append("Total Movement: ").append(totalMovement).append(" cylinders\n");
        sb.append(String.format("Seek - Avg: %.2f, Max: %d, Min: %d\n", m.avgSeek, m.maxSeek, m.minSeek));
        if (m.avgLatency > 0) {
            sb.append(String.format("Latency - Avg: %.2f, Max: %d, Min: %d\n", 
                      m.avgLatency, m.maxLatency, m.minLatency));
            sb.append(String.format("Throughput: %.4f req/time-unit\n", m.throughput));
            sb.append(String.format("Fairness Index: %.4f\n", m.fairnessIndex));
        }
        sb.append("Service Order: ").append(serviceOrder).append("\n");
        sb.append("Head Path: ").append(headPath);
        return sb.toString();
    }
    
    /**
     * Generate a detailed metrics report.
     */
    public String getMetricsReport() {
        Metrics m = getMetrics();
        StringBuilder sb = new StringBuilder();
        sb.append("+---------------------------------------------------------+\n");
        sb.append(String.format("| %-55s |\n", algorithmName + " Metrics"));
        sb.append("+---------------------------------------------------------+\n");
        sb.append(String.format("| Requests Serviced: %-36d |\n", serviceOrder.size()));
        sb.append(String.format("| Total Head Movement: %-34d |\n", totalMovement));
        sb.append("+---------------------------------------------------------+\n");
        sb.append(String.format("| Seek Distance (cylinders):                              |\n"));
        sb.append(String.format("|   Average: %-44.2f |\n", m.avgSeek));
        sb.append(String.format("|   Maximum: %-44d |\n", m.maxSeek));
        sb.append(String.format("|   Minimum: %-44d |\n", m.minSeek));
        sb.append(String.format("|   Std Dev: %-44.2f |\n", m.seekStdDev));
        
        if (m.avgLatency > 0 || !arrivalTimes.isEmpty()) {
            sb.append("+---------------------------------------------------------+\n");
            sb.append(String.format("| Response Latency (time units):                          |\n"));
            sb.append(String.format("|   Average: %-44.2f |\n", m.avgLatency));
            sb.append(String.format("|   Maximum: %-44d |\n", m.maxLatency));
            sb.append(String.format("|   Minimum: %-44d |\n", m.minLatency));
            sb.append(String.format("|   Std Dev: %-44.2f |\n", m.latencyStdDev));
            sb.append("+---------------------------------------------------------+\n");
            sb.append(String.format("| Throughput: %-43.4f |\n", m.throughput));
            sb.append(String.format("| Fairness Index: %-39.4f |\n", m.fairnessIndex));
            sb.append(String.format("|   (1.0 = perfectly fair, lower = less fair)            |\n"));
        }
        
        if (m.executionTimeMs > 0) {
            sb.append("+---------------------------------------------------------+\n");
            sb.append(String.format("| Execution Time: %-38.3f ms |\n", m.executionTimeMs));
        }
        
        sb.append("+---------------------------------------------------------+\n");
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
        sb.append("+-- ").append(algorithmName).append(" Timeline ");
        sb.append(repeat("-", Math.max(0, width - algorithmName.length() - 14))).append("+\n");
        
        // Scale bar
        sb.append("| Cylinder: ");
        sb.append(String.format("%-" + (width - 12) + "s", minCyl + " " + repeat("-", width - 24) + " " + maxCyl));
        sb.append("|\n");
        sb.append("+" + repeat("-", width) + "+\n");
        
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
                label = "->" + pos;
            } else {
                marker = '.';  // Intermediate point
                label = "->" + pos;
            }
            
            // Build the line
            sb.append("| ");
            for (int j = 0; j < width - 2; j++) {
                if (j == plotPos) {
                    sb.append(marker);
                } else if (j < plotPos && i > 0) {
                    int prevPos = headPath.get(i - 1);
                    int prevPlotPos = (int) ((long)(prevPos - minCyl) * (width - 4) / range);
                    prevPlotPos = Math.max(0, Math.min(width - 4, prevPlotPos));
                    if ((j > prevPlotPos && j < plotPos) || (j < prevPlotPos && j > plotPos)) {
                        sb.append('-');
                    } else {
                        sb.append(' ');
                    }
                } else {
                    sb.append(' ');
                }
            }
            sb.append(" |");
            
            // Add label on the right
            if (i == 0) {
                sb.append(" <- Start (").append(pos).append(")");
            } else {
                sb.append(" <- ").append(pos);
                if (!serviceTimes.isEmpty() && i - 1 < serviceTimes.size()) {
                    sb.append(" @t=").append(serviceTimes.get(i - 1));
                }
            }
            sb.append("\n");
            step++;
        }
        
        // Footer
        sb.append("+" + repeat("-", width) + "+\n");
        sb.append("| Total Movement: ").append(String.format("%-" + (width - 18) + "d", totalMovement)).append("|\n");
        sb.append("+" + repeat("-", width) + "+\n");
        
        return sb.toString();
    }
    
    /**
     * Helper method to repeat a string n times (Java 8 compatible).
     */
    private static String repeat(String s, int n) {
        if (n <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
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
                sb.append(" -> ");
            } else if (curr < prev) {
                sb.append(" <- ");
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
