/**
 * Configuration for the disk scheduling simulation.
 * Combines DiskConfig with simulation-specific settings.
 */
public class SimulationConfig {
    private final DiskConfig diskConfig;
    private final boolean verbose;              // Enable detailed logging
    private final boolean recordPath;           // Record full head path
    private final boolean recordTiming;         // Record service times
    private final int nStepSize;                // N for N-Step SCAN
    
    // Defaults
    public static final boolean DEFAULT_VERBOSE = true;
    public static final boolean DEFAULT_RECORD_PATH = true;
    public static final boolean DEFAULT_RECORD_TIMING = true;
    public static final int DEFAULT_N_STEP_SIZE = 4;
    
    /**
     * Create with all defaults.
     */
    public SimulationConfig() {
        this(DiskConfig.getDefault());
    }
    
    /**
     * Create with custom disk config and defaults for simulation settings.
     */
    public SimulationConfig(DiskConfig diskConfig) {
        this(diskConfig, DEFAULT_VERBOSE, DEFAULT_RECORD_PATH, DEFAULT_RECORD_TIMING, DEFAULT_N_STEP_SIZE);
    }
    
    /**
     * Create with all custom settings.
     */
    public SimulationConfig(DiskConfig diskConfig, boolean verbose, boolean recordPath,
                            boolean recordTiming, int nStepSize) {
        this.diskConfig = diskConfig != null ? diskConfig : DiskConfig.getDefault();
        this.verbose = verbose;
        this.recordPath = recordPath;
        this.recordTiming = recordTiming;
        this.nStepSize = Math.max(1, nStepSize);
    }
    
    // Getters
    public DiskConfig getDiskConfig() { return diskConfig; }
    public boolean isVerbose() { return verbose; }
    public boolean isRecordPath() { return recordPath; }
    public boolean isRecordTiming() { return recordTiming; }
    public int getNStepSize() { return nStepSize; }
    
    // Convenience delegation to DiskConfig
    public int getLowerCylinder() { return diskConfig.getLowerCylinder(); }
    public int getUpperCylinder() { return diskConfig.getUpperCylinder(); }
    public DiskConfig.Direction getInitialDirection() { return diskConfig.getInitialDirection(); }
    public DiskConfig.WrapPolicy getWrapPolicy() { return diskConfig.getWrapPolicy(); }
    public boolean isMovingRight() { return diskConfig.isMovingRight(); }
    
    /**
     * Builder for SimulationConfig.
     */
    public static class Builder {
        private DiskConfig diskConfig = DiskConfig.getDefault();
        private boolean verbose = DEFAULT_VERBOSE;
        private boolean recordPath = DEFAULT_RECORD_PATH;
        private boolean recordTiming = DEFAULT_RECORD_TIMING;
        private int nStepSize = DEFAULT_N_STEP_SIZE;
        
        public Builder diskConfig(DiskConfig config) {
            this.diskConfig = config;
            return this;
        }
        
        public Builder verbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }
        
        public Builder recordPath(boolean record) {
            this.recordPath = record;
            return this;
        }
        
        public Builder recordTiming(boolean record) {
            this.recordTiming = record;
            return this;
        }
        
        public Builder nStepSize(int n) {
            this.nStepSize = n;
            return this;
        }
        
        public SimulationConfig build() {
            return new SimulationConfig(diskConfig, verbose, recordPath, recordTiming, nStepSize);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @Override
    public String toString() {
        return String.format("SimulationConfig{disk=%s, verbose=%b, recordPath=%b, recordTiming=%b, nStep=%d}",
                             diskConfig, verbose, recordPath, recordTiming, nStepSize);
    }
}
