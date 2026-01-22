/**
 * Represents a disk I/O request with cylinder position and arrival time.
 */
public class Request implements Comparable<Request> {
    private final int cylinder;
    private final long arrivalTime;
    
    public Request(int cylinder) {
        this(cylinder, 0L);
    }
    
    public Request(int cylinder, long arrivalTime) {
        this.cylinder = cylinder;
        this.arrivalTime = arrivalTime;
    }
    
    public int getCylinder() {
        return cylinder;
    }
    
    public long getArrivalTime() {
        return arrivalTime;
    }
    
    @Override
    public int compareTo(Request other) {
        // Primary sort by arrival time, secondary by cylinder
        int timeCompare = Long.compare(this.arrivalTime, other.arrivalTime);
        if (timeCompare != 0) {
            return timeCompare;
        }
        return Integer.compare(this.cylinder, other.cylinder);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Request request = (Request) obj;
        return cylinder == request.cylinder && arrivalTime == request.arrivalTime;
    }
    
    @Override
    public int hashCode() {
        return 31 * cylinder + Long.hashCode(arrivalTime);
    }
    
    @Override
    public String toString() {
        return String.format("Request{cyl=%d, arrival=%d}", cylinder, arrivalTime);
    }
}
