# Disk Scheduling Algorithms Simulator

## Overview
This project simulates various disk scheduling algorithms for analyzing disk head movements when servicing requests. It implements eight popular disk scheduling algorithms:

1. **First-Come-First-Serve (FCFS)**
2. **Shortest Seek Time First (SSTF)**
3. **SCAN**
4. **C-SCAN**
5. **LOOK**
6. **C-LOOK**
7. **FSCAN** - Freeze SCAN with two rotating queues
8. **N-Step SCAN** - SCAN with subqueues of size N

The simulator allows testing these algorithms with custom requests, initial head positions, batch testing configurations, and workload generation with reproducible seeds.

## Features
- **Algorithms**: Implements and evaluates FCFS, SSTF, SCAN, C-SCAN, LOOK, C-LOOK, FSCAN, and N-Step SCAN.
- **Custom Input**: Accepts custom disk requests and initial head position via command-line arguments.
- **Configurable Disk Geometry**: Set custom cylinder bounds (min/max).
- **Configurable Sweep Direction**: Choose initial head direction (LEFT/RIGHT).
- **Configurable Wrap Policy**: Choose wrap behavior for circular algorithms (C-SCAN, C-LOOK).
- **Algorithm Selection**: Run specific algorithms or all algorithms.
- **Batch Mode**: Allows batch testing with multiple request sets and initial positions.
- **Workload Generator**: Generate requests with different distributions (uniform, normal, hotspot).
- **Reproducible Runs**: Use a seed for reproducible random workload generation.
- **Time-Based Scheduling**: Support for request arrival times with FSCAN and N-Step SCAN.
- **Movement Calculation**: Logs the servicing order and calculates total head movements.
- **Input Validation**: Validates all parameters with helpful error messages.

---

## Prerequisites
Ensure you have the following installed:
- **Java Development Kit (JDK)** (Version 8 or higher)

---

## Directory Structure
```
.
├── DiskSchedulingAlgorithm.java   # Abstract class for disk scheduling algorithms
├── DiskConfig.java                # Configuration class for disk parameters
├── FCFS.java                      # FCFS implementation
├── SSTF.java                      # SSTF implementation
├── SCAN.java                      # SCAN implementation
├── C_SCAN.java                    # C-SCAN implementation
├── LOOK.java                      # LOOK implementation
├── C_LOOK.java                    # C-LOOK implementation
├── FSCAN.java                     # FSCAN implementation (two rotating queues)
├── N_Step_SCAN.java               # N-Step SCAN implementation
├── Request.java                   # Request class with cylinder and arrival time
├── WorkloadGenerator.java         # Workload generator with distributions
├── Main.java                      # Entry point for the application
├── .gitignore                     # Git ignore file for .class files
```

---

## Setup
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/eyadgad/Disk-Scheduling-Algorithms-Simulator.git
   cd Disk-Scheduling-Algorithms-Simulator
   ```

2. **Compile the Source Code**:
   Navigate to the `src` directory and compile all Java files:
   ```bash
   javac *.java
   ```

3. **Run the Program**:
   Execute the program using the `java` command:
   ```bash
   java Main
   ```

---

## Usage
### Command-Line Arguments
You can pass the following arguments to customize the simulation:

#### Basic Options
| Option | Description | Default |
|--------|-------------|---------|
| `-r`, `--requests` | Space-separated list of cylinder numbers | Predefined list |
| `-i`, `--initial_position` | Initial head position | 1000 |
| `-b`, `--batch` | Enable batch testing mode | false |
| `-h`, `--help` | Show help message | - |

#### Disk Configuration Options
| Option | Description | Default |
|--------|-------------|---------|
| `--min-cylinder`, `--lower` | Lower cylinder bound | 0 |
| `--max-cylinder`, `--upper` | Upper cylinder bound | 4999 |
| `--direction`, `--dir` | Initial sweep direction: `LEFT` or `RIGHT` | RIGHT |
| `--wrap`, `--wrap-policy` | Wrap policy: `START` or `FIRST` | START |

**Direction Aliases**: `L`/`R`, `UP`/`DOWN`, `INCREASING`/`DECREASING`

**Wrap Policies**:
- `START` (or `BOUNDARY`): Wrap to disk boundary (traditional C-SCAN/C-LOOK)
- `FIRST` (or `FIRST_REQ`): Jump directly to first request in new direction

#### Algorithm Selection Options
| Option | Description | Default |
|--------|-------------|---------|
| `-a`, `--algorithms` | Space-separated list of algorithms to run | All |
| `--list-algorithms` | List all available algorithms | - |

**Available Algorithms**: `FCFS`, `SSTF`, `SCAN`, `C_SCAN`, `LOOK`, `C_LOOK`, `FSCAN`, `N_STEP_SCAN`

#### Workload Generation Options
| Option | Description | Default |
|--------|-------------|---------|
| `-g`, `--generate` | Use workload generator instead of `-r` | false |
| `-s`, `--seed` | Random seed for reproducibility | Current time |
| `-d`, `--distribution` | Distribution: `UNIFORM`, `NORMAL`, `HOTSPOT` | UNIFORM |
| `-c`, `--count` | Number of requests to generate | 20 |
| `-t`, `--time-span` | Max arrival time span (enables time-based mode) | 0 |
| `--time-based` | Enable time-based scheduling mode | false |
| `-n`, `--n-step` | Step size N for N-Step SCAN | 4 |

### Examples

1. **Default Run**:
   ```bash
   java Main
   ```
   This will use the default requests and initial position.

2. **Custom Requests**:
   ```bash
   java Main -r 100 200 300 400 -i 150
   ```
   This runs the simulator with custom requests `[100, 200, 300, 400]` and an initial position of `150`.

3. **Batch Testing**:
   ```bash
   java Main -b
   ```
   This tests multiple scenarios with predefined request sets and initial positions.

4. **Custom Disk Geometry**:
   ```bash
   java Main --lower 0 --upper 999 -i 500 -r 100 200 800 900
   ```
   Simulates a disk with cylinders 0-999.

5. **Custom Initial Direction**:
   ```bash
   java Main --direction LEFT -r 100 200 800 -i 500
   ```
   Start the head moving left (toward lower cylinders).

6. **Custom Wrap Policy**:
   ```bash
   java Main --wrap FIRST -a C_LOOK -r 100 500 200
   ```
   C-LOOK jumps directly to first request when wrapping.

7. **Select Specific Algorithms**:
   ```bash
   java Main -a SCAN C_SCAN LOOK -r 100 200 300
   ```
   Only run SCAN, C-SCAN, and LOOK algorithms.

8. **Generate Uniform Workload with Seed**:
   ```bash
   java Main -g -s 12345 -d UNIFORM -c 30
   ```
   Generates 30 requests with uniform distribution using seed 12345.

9. **Generate Normal Distribution Workload**:
   ```bash
   java Main -g -s 42 -d NORMAL -c 25 -i 2500
   ```
   Generates 25 requests centered around the middle of the disk.

10. **Generate Hotspot Workload**:
    ```bash
    java Main -g -s 100 -d HOTSPOT -c 40
    ```
    Generates 40 requests clustered around hotspot regions (500, 2500, 4000).

11. **Time-Based Scheduling**:
    ```bash
    java Main -g -s 42 -d UNIFORM -c 20 -t 1000
    ```
    Generates 20 requests with arrival times spread over 1000 time units.

12. **Custom N-Step SCAN Size**:
    ```bash
    java Main -g -s 42 -c 24 -n 6
    ```
    Uses N=6 for N-Step SCAN algorithm.

13. **Combined Options**:
    ```bash
    java Main --lower 0 --upper 9999 --direction LEFT --wrap FIRST \
              -g -s 42 -d HOTSPOT -c 50 -a SCAN C_SCAN FSCAN
    ```
    Full example with custom disk, direction, wrap policy, workload, and algorithm selection.

### Distribution Types

- **UNIFORM**: Requests are uniformly distributed across all cylinders (0-4999).
- **NORMAL**: Requests follow a normal distribution centered on the middle of the disk (cylinder 2500) with standard deviation of 1250.
- **HOTSPOT**: 70% of requests are clustered around three hotspot regions (cylinders 500, 2500, 4000), with the remaining 30% uniformly distributed.

---

## Example Output

### Basic Run
```plaintext
=== Disk Scheduling Algorithms Simulator ===
Input Requests: [2069, 98, 183, 37, 122, 14, 124, 65, 67, 1212, 2296, 2800, 544, 1618, 356, 1523, 4965, 3681]
Initial Position: 1000
N-Step Size: 4

----------------------------------------------
FCFS Total Movement: 12582
----------------------------------------------
SSTF Total Movement: 6021
----------------------------------------------
SCAN Total Movement: 8998
----------------------------------------------
C_SCAN Total Movement: 15230
----------------------------------------------
LOOK Total Movement: 9231
----------------------------------------------
C_LOOK Total Movement: 7325
----------------------------------------------
(FSCAN) Processing Queue 1: [2069, 98, 183, 37, 122, 14, 124, 65, 67]
(FSCAN) Processing Queue 2: [1212, 2296, 2800, 544, 1618, 356, 1523, 4965, 3681]
FSCAN Total Movement: 9951
----------------------------------------------
(N-Step SCAN) Step size N = 4
(N-Step SCAN) Processing Subqueue 1: [2069, 98, 183, 37]
(N-Step SCAN) Processing Subqueue 2: [122, 14, 124, 65]
...
N_Step_SCAN (N=4) Total Movement: 8834
----------------------------------------------
Simulation Complete.
```

### Generated Workload with Time-Based Mode
```plaintext
=== Disk Scheduling Algorithms Simulator ===
Generated Workload:
  Seed: 12345
  Distribution: HOTSPOT
  Request Count: 15
  Max Arrival Time: 500
  Time-Based Mode: true
  Cylinders: [2548, 493, 2461, 4021, 515, ...]
  Arrival Times: [23, 45, 78, 102, 156, ...]
Initial Position: 1000
N-Step Size: 4

Running algorithms in time-based mode...
(Note: Only FSCAN and N-Step SCAN use arrival times; others use cylinder order)

...
FSCAN [time-based] Total Movement: 5234
----------------------------------------------
N_Step_SCAN (N=4) [time-based] Total Movement: 4891
----------------------------------------------
Simulation Complete.
```

---

## Algorithm Details

### FSCAN (Freeze SCAN)
FSCAN uses two rotating queues to prevent starvation:
- **Active Queue**: Currently being serviced using SCAN algorithm
- **Holding Queue**: Collects new requests while active queue is being processed
- When the active queue is exhausted, the queues are swapped

In time-based mode, requests that arrive during processing go to the holding queue.

### N-Step SCAN
N-Step SCAN divides requests into subqueues of size N:
- Each subqueue is processed using SCAN before moving to the next
- Provides better response time variance than plain SCAN
- When N=1, behaves like FCFS
- When N is very large, behaves like SCAN
- FSCAN is a special case with two queues

---

## Extending the Project
To add new algorithms:
1. Create a new class that extends `DiskSchedulingAlgorithm`.
2. Override the `execute()` method with the algorithm logic.
3. Add the new class to the `Main.java` `algorithms` list.
4. For time-based scheduling support, add a constructor that accepts `List<Request>` and implement `executeTimeBased()`.

### Request Class
The `Request` class encapsulates:
- `cylinder`: The cylinder number to access
- `arrivalTime`: When the request arrives in the system

### WorkloadGenerator Class
Use `WorkloadGenerator` to create reproducible workloads:
```java
WorkloadGenerator gen = new WorkloadGenerator(12345); // seed
List<Request> requests = gen.generate(20, Distribution.HOTSPOT, 1000);
```

### DiskConfig Class
Configure disk geometry and scheduling behavior:
```java
DiskConfig config = new DiskConfig();
config.setDiskBounds(0, 9999);                           // Custom disk size
config.setInitialDirection(DiskConfig.Direction.LEFT);   // Start moving left
config.setWrapPolicy(DiskConfig.WrapPolicy.WRAP_TO_FIRST_REQ); // Jump to first request

// Use with algorithms
SCAN scan = new SCAN(requests, initialPosition, config);
```

**Direction Options**:
- `Direction.RIGHT` - Move toward higher cylinder numbers (default)
- `Direction.LEFT` - Move toward lower cylinder numbers

**Wrap Policy Options** (for C-SCAN/C-LOOK):
- `WrapPolicy.WRAP_TO_START` - Wrap to disk boundary (default)
- `WrapPolicy.WRAP_TO_FIRST_REQ` - Jump directly to first request

