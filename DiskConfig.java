/**
 * Configuration class for disk scheduling simulation.
 * Holds all configurable parameters with validation and sensible defaults.
 */
public class DiskConfig {
    
    // Disk geometry
    private int lowerCylinder;
    private int upperCylinder;
    
    // Sweep direction
    private Direction initialDirection;
    
    // Wrap policy for circular algorithms (C-SCAN, C-LOOK)
    private WrapPolicy wrapPolicy;
    
    // Default values
    public static final int DEFAULT_LOWER_CYLINDER = 0;
    public static final int DEFAULT_UPPER_CYLINDER = 4999;
    public static final Direction DEFAULT_DIRECTION = Direction.RIGHT;
    public static final WrapPolicy DEFAULT_WRAP_POLICY = WrapPolicy.WRAP_TO_START;
    
    /**
     * Direction of disk head sweep.
     */
    public enum Direction {
        LEFT,   // Towards lower cylinder numbers (decreasing)
        RIGHT;  // Towards higher cylinder numbers (increasing)
        
        public Direction opposite() {
            return this == LEFT ? RIGHT : LEFT;
        }
        
        public static Direction fromString(String s) {
            if (s == null) return DEFAULT_DIRECTION;
            switch (s.toUpperCase().trim()) {
                case "LEFT":
                case "L":
                case "DOWN":
                case "DECREASING":
                    return LEFT;
                case "RIGHT":
                case "R":
                case "UP":
                case "INCREASING":
                default:
                    return RIGHT;
            }
        }
    }
    
    /**
     * Wrap policy for circular algorithms.
     */
    public enum WrapPolicy {
        WRAP_TO_START,      // Jump to start of disk (cylinder 0 or max)
        WRAP_TO_FIRST_REQ;  // Jump directly to first request in new direction
        
        public static WrapPolicy fromString(String s) {
            if (s == null) return DEFAULT_WRAP_POLICY;
            switch (s.toUpperCase().trim()) {
                case "FIRST":
                case "FIRST_REQ":
                case "FIRST_REQUEST":
                case "WRAP_TO_FIRST_REQ":
                    return WRAP_TO_FIRST_REQ;
                case "START":
                case "BOUNDARY":
                case "WRAP_TO_START":
                default:
                    return WRAP_TO_START;
            }
        }
    }
    
    /**
     * Create configuration with all defaults.
     */
    public DiskConfig() {
        this(DEFAULT_LOWER_CYLINDER, DEFAULT_UPPER_CYLINDER, 
             DEFAULT_DIRECTION, DEFAULT_WRAP_POLICY);
    }
    
    /**
     * Create configuration with custom bounds and defaults for others.
     */
    public DiskConfig(int lowerCylinder, int upperCylinder) {
        this(lowerCylinder, upperCylinder, DEFAULT_DIRECTION, DEFAULT_WRAP_POLICY);
    }
    
    /**
     * Create configuration with all custom values.
     */
    public DiskConfig(int lowerCylinder, int upperCylinder, 
                      Direction initialDirection, WrapPolicy wrapPolicy) {
        setDiskBounds(lowerCylinder, upperCylinder);
        this.initialDirection = initialDirection != null ? initialDirection : DEFAULT_DIRECTION;
        this.wrapPolicy = wrapPolicy != null ? wrapPolicy : DEFAULT_WRAP_POLICY;
    }
    
    /**
     * Set disk bounds with validation.
     */
    public void setDiskBounds(int lower, int upper) {
        if (lower < 0) {
            throw new IllegalArgumentException("Lower cylinder cannot be negative: " + lower);
        }
        if (upper <= lower) {
            throw new IllegalArgumentException(
                "Upper cylinder (" + upper + ") must be greater than lower cylinder (" + lower + ")");
        }
        this.lowerCylinder = lower;
        this.upperCylinder = upper;
    }
    
    /**
     * Validate that a cylinder number is within bounds.
     */
    public void validateCylinder(int cylinder) {
        if (cylinder < lowerCylinder || cylinder > upperCylinder) {
            throw new IllegalArgumentException(
                "Cylinder " + cylinder + " is out of bounds [" + lowerCylinder + ", " + upperCylinder + "]");
        }
    }
    
    /**
     * Validate that the initial position is within bounds.
     */
    public void validateInitialPosition(int position) {
        validateCylinder(position);
    }
    
    /**
     * Clamp a cylinder number to valid bounds.
     */
    public int clampCylinder(int cylinder) {
        return Math.max(lowerCylinder, Math.min(upperCylinder, cylinder));
    }
    
    // Getters
    public int getLowerCylinder() {
        return lowerCylinder;
    }
    
    public int getUpperCylinder() {
        return upperCylinder;
    }
    
    public int getDiskSize() {
        return upperCylinder - lowerCylinder + 1;
    }
    
    public Direction getInitialDirection() {
        return initialDirection;
    }
    
    public WrapPolicy getWrapPolicy() {
        return wrapPolicy;
    }
    
    public boolean isMovingRight() {
        return initialDirection == Direction.RIGHT;
    }
    
    // Setters with validation
    public void setLowerCylinder(int lowerCylinder) {
        if (lowerCylinder < 0) {
            throw new IllegalArgumentException("Lower cylinder cannot be negative: " + lowerCylinder);
        }
        if (lowerCylinder >= this.upperCylinder) {
            throw new IllegalArgumentException(
                "Lower cylinder (" + lowerCylinder + ") must be less than upper cylinder (" + upperCylinder + ")");
        }
        this.lowerCylinder = lowerCylinder;
    }
    
    public void setUpperCylinder(int upperCylinder) {
        if (upperCylinder <= this.lowerCylinder) {
            throw new IllegalArgumentException(
                "Upper cylinder (" + upperCylinder + ") must be greater than lower cylinder (" + lowerCylinder + ")");
        }
        this.upperCylinder = upperCylinder;
    }
    
    public void setInitialDirection(Direction direction) {
        this.initialDirection = direction != null ? direction : DEFAULT_DIRECTION;
    }
    
    public void setWrapPolicy(WrapPolicy policy) {
        this.wrapPolicy = policy != null ? policy : DEFAULT_WRAP_POLICY;
    }
    
    /**
     * Create a copy of this configuration.
     */
    public DiskConfig copy() {
        return new DiskConfig(lowerCylinder, upperCylinder, initialDirection, wrapPolicy);
    }
    
    @Override
    public String toString() {
        return String.format("DiskConfig{cylinders=[%d,%d], direction=%s, wrapPolicy=%s}",
                             lowerCylinder, upperCylinder, initialDirection, wrapPolicy);
    }
    
    /**
     * Get a default configuration instance.
     */
    public static DiskConfig getDefault() {
        return new DiskConfig();
    }
}
