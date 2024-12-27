# Disk Scheduling Algorithms Simulator

## Overview
This project simulates various disk scheduling algorithms for analyzing disk head movements when servicing requests. It implements six popular disk scheduling algorithms:

1. **First-Come-First-Serve (FCFS)**
2. **Shortest Seek Time First (SSTF)**
3. **SCAN**
4. **C-SCAN**
5. **LOOK**
6. **C-LOOK**

The simulator allows testing these algorithms with custom requests, initial head positions, and batch testing configurations.

## Features
- **Algorithms**: Implements and evaluates FCFS, SSTF, SCAN, C-SCAN, LOOK, and C-LOOK.
- **Custom Input**: Accepts custom disk requests and initial head position via command-line arguments.
- **Batch Mode**: Allows batch testing with multiple request sets and initial positions.
- **Movement Calculation**: Logs the servicing order and calculates total head movements.
- **Custom Logging**: Provides simplified, clean output without timestamps or extra details.

---

## Prerequisites
Ensure you have the following installed:
- **Java Development Kit (JDK)** (Version 8 or higher)

---

## Directory Structure
```
.
├── DiskSchedulingAlgorithm.java   # Abstract class for disk scheduling algorithms
├── FCFS.java                      # FCFS implementation
├── SSTF.java                      # SSTF implementation
├── SCAN.java                      # SCAN implementation
├── C_SCAN.java                    # C-SCAN implementation
├── LOOK.java                      # LOOK implementation
├── C_LOOK.java                    # C-LOOK implementation
├── Main.java                      # Entry point for the application
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
1. **Disk Requests**:
   - Use `-r` or `--requests` to specify a list of disk requests.
   - Default: `[2069, 98, 183, 37, 122, 14, 124, 65, 67, 1212, 2296, 2800, 544, 1618, 356, 1523, 4965, 3681]`

2. **Initial Position**:
   - Use `-i` or `--initial_position` to specify the initial position of the disk head.
   - Default: `1000`

3. **Batch Mode**:
   - Use `-b` or `--batch` to enable batch testing with predefined scenarios.

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

---

## Example Output
```plaintext
=== Disk Scheduling Algorithms Simulator ===
Input Requests: [2069, 98, 183, 37, 122, 14, 124, 65, 67, 1212, 2296, 2800, 544, 1618, 356, 1523, 4965, 3681]
Initial Position: 1000

----------------------------------------------
(FCFS) Total Movement: 12582
----------------------------------------------
(SSTF) Servicing at: 1212
(SSTF) Servicing at: 1523
(SSTF) Total Movement: 6021
----------------------------------------------
(SCAN) Total Movement: 8998
----------------------------------------------
(C-SCAN) Total Movement: 15230
----------------------------------------------
(LOOK) Total Movement: 9231
----------------------------------------------
(C-LOOK) Servicing at: 356
(C-LOOK) Servicing at: 544
(C-LOOK) Total Movement: 7325
----------------------------------------------
Simulation Complete.
```

---

## Extending the Project
To add new algorithms:
1. Create a new class that extends `DiskSchedulingAlgorithm`.
2. Override the `execute()` method with the algorithm logic.
3. Add the new class to the `Main.java` `algorithms` list.

