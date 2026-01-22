import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates disk I/O workloads with different distribution patterns.
 * Supports uniform, normal, and hotspot distributions with reproducible seeds.
 */
public class WorkloadGenerator {
    
    public enum Distribution {
        UNIFORM,    // Requests uniformly distributed across all cylinders
        NORMAL,     // Requests follow normal distribution centered on disk
        HOTSPOT     // Requests clustered around specific hotspot regions
    }
    
    private final Random random;
    private final int minCylinder;
    private final int maxCylinder;
    private final long seed;
    
    // Hotspot configuration
    private static final double HOTSPOT_PROBABILITY = 0.7; // 70% of requests go to hotspots
    private static final int[] DEFAULT_HOTSPOTS = {500, 2500, 4000}; // Default hotspot centers
    private static final int HOTSPOT_RADIUS = 200; // Radius around hotspot center
    
    /**
     * Create a workload generator with a specific seed for reproducibility.
     */
    public WorkloadGenerator(long seed) {
        this(seed, 0, 4999);
    }
    
    /**
     * Create a workload generator with custom cylinder range.
     */
    public WorkloadGenerator(long seed, int minCylinder, int maxCylinder) {
        this.seed = seed;
        this.random = new Random(seed);
        this.minCylinder = minCylinder;
        this.maxCylinder = maxCylinder;
    }
    
    public long getSeed() {
        return seed;
    }
    
    /**
     * Reset the random generator to reproduce the same sequence.
     */
    public void reset() {
        random.setSeed(seed);
    }
    
    /**
     * Generate a list of requests with specified distribution.
     * 
     * @param count Number of requests to generate
     * @param distribution Distribution pattern to use
     * @param maxArrivalTime Maximum arrival time span (requests arrive between 0 and this value)
     * @return List of generated requests
     */
    public List<Request> generate(int count, Distribution distribution, long maxArrivalTime) {
        List<Request> requests = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            int cylinder = generateCylinder(distribution);
            long arrivalTime = generateArrivalTime(maxArrivalTime);
            requests.add(new Request(cylinder, arrivalTime));
        }
        
        // Sort by arrival time
        requests.sort(Request::compareTo);
        
        return requests;
    }
    
    /**
     * Generate requests with zero arrival time (all arrive at once).
     */
    public List<Request> generate(int count, Distribution distribution) {
        return generate(count, distribution, 0);
    }
    
    /**
     * Generate cylinder number based on distribution.
     */
    private int generateCylinder(Distribution distribution) {
        switch (distribution) {
            case UNIFORM:
                return generateUniformCylinder();
            case NORMAL:
                return generateNormalCylinder();
            case HOTSPOT:
                return generateHotspotCylinder();
            default:
                return generateUniformCylinder();
        }
    }
    
    /**
     * Uniform distribution: equal probability for all cylinders.
     */
    private int generateUniformCylinder() {
        return minCylinder + random.nextInt(maxCylinder - minCylinder + 1);
    }
    
    /**
     * Normal distribution: centered on middle of disk, with std dev of 1/4 range.
     */
    private int generateNormalCylinder() {
        double mean = (minCylinder + maxCylinder) / 2.0;
        double stdDev = (maxCylinder - minCylinder) / 4.0;
        
        double value = random.nextGaussian() * stdDev + mean;
        
        // Clamp to valid range
        int cylinder = (int) Math.round(value);
        return Math.max(minCylinder, Math.min(maxCylinder, cylinder));
    }
    
    /**
     * Hotspot distribution: most requests go to specific regions.
     */
    private int generateHotspotCylinder() {
        if (random.nextDouble() < HOTSPOT_PROBABILITY) {
            // Select a random hotspot
            int hotspotCenter = DEFAULT_HOTSPOTS[random.nextInt(DEFAULT_HOTSPOTS.length)];
            
            // Generate within hotspot radius (using normal distribution)
            double offset = random.nextGaussian() * (HOTSPOT_RADIUS / 2.0);
            int cylinder = hotspotCenter + (int) Math.round(offset);
            
            return Math.max(minCylinder, Math.min(maxCylinder, cylinder));
        } else {
            // Outside hotspot: uniform distribution
            return generateUniformCylinder();
        }
    }
    
    /**
     * Generate arrival time using exponential distribution (Poisson process).
     */
    private long generateArrivalTime(long maxArrivalTime) {
        if (maxArrivalTime <= 0) {
            return 0;
        }
        // Uniform distribution over the time range
        return (long) (random.nextDouble() * maxArrivalTime);
    }
    
    /**
     * Generate requests with exponentially distributed inter-arrival times.
     * 
     * @param count Number of requests
     * @param distribution Cylinder distribution
     * @param meanInterArrivalTime Mean time between arrivals
     * @return List of requests with realistic arrival times
     */
    public List<Request> generateWithPoissonArrivals(int count, Distribution distribution, 
                                                       double meanInterArrivalTime) {
        List<Request> requests = new ArrayList<>();
        long currentTime = 0;
        
        for (int i = 0; i < count; i++) {
            int cylinder = generateCylinder(distribution);
            requests.add(new Request(cylinder, currentTime));
            
            // Exponential inter-arrival time
            double interArrival = -meanInterArrivalTime * Math.log(1 - random.nextDouble());
            currentTime += (long) interArrival;
        }
        
        return requests;
    }
    
    /**
     * Convert a list of cylinder integers to Request objects with zero arrival time.
     */
    public static List<Request> fromCylinders(List<Integer> cylinders) {
        List<Request> requests = new ArrayList<>();
        for (int cyl : cylinders) {
            requests.add(new Request(cyl, 0));
        }
        return requests;
    }
    
    /**
     * Convert a list of cylinder integers to Request objects with sequential arrival times.
     */
    public static List<Request> fromCylindersWithSequentialArrival(List<Integer> cylinders, 
                                                                     long intervalBetweenArrivals) {
        List<Request> requests = new ArrayList<>();
        long time = 0;
        for (int cyl : cylinders) {
            requests.add(new Request(cyl, time));
            time += intervalBetweenArrivals;
        }
        return requests;
    }
    
    @Override
    public String toString() {
        return String.format("WorkloadGenerator{seed=%d, cylinders=[%d,%d]}", 
                             seed, minCylinder, maxCylinder);
    }
}
