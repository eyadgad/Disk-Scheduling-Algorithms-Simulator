import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * A simple Java Swing UI for the Disk Scheduling Algorithms Simulator.
 * Provides input fields for disk configuration and displays simulation results.
 */
public class DiskSchedulerGUI extends JFrame {
    
    // Input fields
    private JTextField lowerBoundField;
    private JTextField upperBoundField;
    private JTextField initialPositionField;
    private JComboBox<String> directionCombo;
    private JTextField requestListField;
    private JComboBox<String> algorithmCombo;
    
    // Output area
    private JTextArea resultArea;
    
    // Available algorithms
    private static final String[] ALGORITHMS = {
        "FCFS", "SSTF", "SCAN", "C_SCAN", "LOOK", "C_LOOK", "FSCAN", "N_Step_SCAN"
    };
    
    // Direction options
    private static final String[] DIRECTIONS = {"RIGHT", "LEFT"};
    
    public DiskSchedulerGUI() {
        super("Disk Scheduling Algorithms Simulator");
        initializeUI();
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Create main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Create input panel
        JPanel inputPanel = createInputPanel();
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        
        // Create result panel
        JPanel resultPanel = createResultPanel();
        mainPanel.add(resultPanel, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Set default values
        setDefaultValues();
        
        // Size and center the window
        setPreferredSize(new Dimension(650, 550));
        pack();
        setLocationRelativeTo(null);
    }
    
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "Configuration", 
            TitledBorder.LEFT, 
            TitledBorder.TOP
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Row 0: Disk Bounds
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Lower Cylinder Bound:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        lowerBoundField = new JTextField(10);
        panel.add(lowerBoundField, gbc);
        
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Upper Cylinder Bound:"), gbc);
        
        gbc.gridx = 3;
        gbc.anchor = GridBagConstraints.WEST;
        upperBoundField = new JTextField(10);
        panel.add(upperBoundField, gbc);
        
        // Row 1: Initial Position and Direction
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Initial Head Position:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        initialPositionField = new JTextField(10);
        panel.add(initialPositionField, gbc);
        
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Initial Direction:"), gbc);
        
        gbc.gridx = 3;
        gbc.anchor = GridBagConstraints.WEST;
        directionCombo = new JComboBox<>(DIRECTIONS);
        panel.add(directionCombo, gbc);
        
        // Row 2: Algorithm Selection
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Algorithm:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        algorithmCombo = new JComboBox<>(ALGORITHMS);
        algorithmCombo.setPreferredSize(new Dimension(200, 25));
        panel.add(algorithmCombo, gbc);
        
        // Row 3: Request List
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Request List:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        requestListField = new JTextField(40);
        requestListField.setToolTipText("Enter comma-separated cylinder numbers (e.g., 98, 183, 37, 122)");
        panel.add(requestListField, gbc);
        
        // Row 4: Hint label
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.gridwidth = 3;
        JLabel hintLabel = new JLabel("(Enter comma or space separated cylinder numbers)");
        hintLabel.setFont(hintLabel.getFont().deriveFont(Font.ITALIC, 11f));
        hintLabel.setForeground(Color.GRAY);
        panel.add(hintLabel, gbc);
        
        return panel;
    }
    
    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "Simulation Results", 
            TitledBorder.LEFT, 
            TitledBorder.TOP
        ));
        
        resultArea = new JTextArea(12, 50);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setMargin(new Insets(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        JButton runButton = new JButton("Run Simulation");
        runButton.setPreferredSize(new Dimension(150, 35));
        runButton.setFont(runButton.getFont().deriveFont(Font.BOLD));
        runButton.addActionListener(e -> runSimulation());
        
        JButton clearButton = new JButton("Clear Results");
        clearButton.setPreferredSize(new Dimension(120, 35));
        clearButton.addActionListener(e -> {
            resultArea.setText("");
        });
        
        JButton resetButton = new JButton("Reset Defaults");
        resetButton.setPreferredSize(new Dimension(120, 35));
        resetButton.addActionListener(e -> setDefaultValues());
        
        panel.add(runButton);
        panel.add(clearButton);
        panel.add(resetButton);
        
        return panel;
    }
    
    private void setDefaultValues() {
        lowerBoundField.setText("0");
        upperBoundField.setText("4999");
        initialPositionField.setText("1000");
        directionCombo.setSelectedItem("RIGHT");
        algorithmCombo.setSelectedIndex(0);
        requestListField.setText("2069, 98, 183, 37, 122, 14, 124, 65, 67, 1212, 2296, 2800, 544, 1618");
        resultArea.setText("Configure the simulation parameters above and click 'Run Simulation'.\n");
    }
    
    private void runSimulation() {
        resultArea.setText("");
        
        try {
            // Parse input values
            int lowerBound = parseIntField(lowerBoundField, "Lower Cylinder Bound");
            int upperBound = parseIntField(upperBoundField, "Upper Cylinder Bound");
            int initialPosition = parseIntField(initialPositionField, "Initial Head Position");
            
            // Validate bounds
            if (lowerBound < 0) {
                showError("Lower cylinder bound cannot be negative.");
                return;
            }
            if (upperBound <= lowerBound) {
                showError("Upper cylinder bound must be greater than lower bound.");
                return;
            }
            if (initialPosition < lowerBound || initialPosition > upperBound) {
                showError("Initial head position must be within cylinder bounds [" + 
                         lowerBound + ", " + upperBound + "].");
                return;
            }
            
            // Parse direction
            String directionStr = (String) directionCombo.getSelectedItem();
            DiskConfig.Direction direction = DiskConfig.Direction.fromString(directionStr);
            
            // Parse request list
            List<Integer> requests = parseRequestList(requestListField.getText());
            if (requests.isEmpty()) {
                showError("Please enter at least one request.");
                return;
            }
            
            // Validate requests within bounds
            for (int req : requests) {
                if (req < lowerBound || req > upperBound) {
                    showError("Request " + req + " is outside cylinder bounds [" + 
                             lowerBound + ", " + upperBound + "].");
                    return;
                }
            }
            
            // Get selected algorithm
            String algorithmName = (String) algorithmCombo.getSelectedItem();
            
            // Create disk configuration
            DiskConfig config = new DiskConfig(lowerBound, upperBound, direction, 
                                               DiskConfig.WrapPolicy.WRAP_TO_START);
            
            // Create and execute algorithm
            DiskSchedulingAlgorithm algorithm = createAlgorithm(algorithmName, requests, 
                                                                 initialPosition, config);
            
            if (algorithm == null) {
                showError("Unknown algorithm: " + algorithmName);
                return;
            }
            
            AlgorithmResult result = algorithm.executeWithResult();
            
            // Display results
            displayResults(result, requests, initialPosition, config);
            
        } catch (NumberFormatException e) {
            showError("Invalid number format: " + e.getMessage());
        } catch (Exception e) {
            showError("Error running simulation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private int parseIntField(JTextField field, String fieldName) throws NumberFormatException {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            throw new NumberFormatException(fieldName + " cannot be empty");
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(fieldName + " must be a valid integer");
        }
    }
    
    private List<Integer> parseRequestList(String input) {
        List<Integer> requests = new ArrayList<>();
        if (input == null || input.trim().isEmpty()) {
            return requests;
        }
        
        // Split by comma, space, or both
        String[] parts = input.split("[,\\s]+");
        for (String part : parts) {
            part = part.trim();
            if (!part.isEmpty()) {
                try {
                    requests.add(Integer.parseInt(part));
                } catch (NumberFormatException e) {
                    // Skip invalid entries
                }
            }
        }
        return requests;
    }
    
    private DiskSchedulingAlgorithm createAlgorithm(String name, List<Integer> requests, 
                                                     int initialPosition, DiskConfig config) {
        switch (name) {
            case "FCFS":
                return new FCFS(requests, initialPosition, config);
            case "SSTF":
                return new SSTF(requests, initialPosition, config);
            case "SCAN":
                return new SCAN(requests, initialPosition, config);
            case "C_SCAN":
                return new C_SCAN(requests, initialPosition, config);
            case "LOOK":
                return new LOOK(requests, initialPosition, config);
            case "C_LOOK":
                return new C_LOOK(requests, initialPosition, config);
            case "FSCAN":
                return new FSCAN(requests, initialPosition, config);
            case "N_Step_SCAN":
                return new N_Step_SCAN(requests, initialPosition, 4, config);
            default:
                return null;
        }
    }
    
    private void displayResults(AlgorithmResult result, List<Integer> requests, 
                                int initialPosition, DiskConfig config) {
        StringBuilder sb = new StringBuilder();
        AlgorithmResult.Metrics metrics = result.getMetrics();
        
        // Header (using ASCII characters for compatibility)
        sb.append("+==================================================================+\n");
        sb.append("|              DISK SCHEDULING SIMULATION RESULTS                 |\n");
        sb.append("+==================================================================+\n");
        
        // Configuration summary
        sb.append("| Configuration:                                                  |\n");
        sb.append(String.format("|   Algorithm: %-51s |\n", result.getAlgorithmName()));
        sb.append(String.format("|   Cylinder Range: [%d, %d]%-37s |\n", 
                  config.getLowerCylinder(), config.getUpperCylinder(), ""));
        sb.append(String.format("|   Initial Position: %-45d |\n", initialPosition));
        sb.append(String.format("|   Initial Direction: %-44s |\n", config.getInitialDirection()));
        sb.append(String.format("|   Number of Requests: %-43d |\n", requests.size()));
        
        sb.append("+------------------------------------------------------------------+\n");
        
        // Main result
        sb.append("| RESULTS:                                                        |\n");
        sb.append("+------------------------------------------------------------------+\n");
        sb.append(String.format("|   >>> TOTAL HEAD MOVEMENT: %-37d |\n", result.getTotalMovement()));
        sb.append("+------------------------------------------------------------------+\n");
        
        // Seek statistics
        sb.append("| Seek Statistics:                                                |\n");
        sb.append(String.format("|   Average Seek Distance: %-39.2f |\n", metrics.avgSeek));
        sb.append(String.format("|   Maximum Seek Distance: %-39d |\n", metrics.maxSeek));
        sb.append(String.format("|   Minimum Seek Distance: %-39d |\n", metrics.minSeek));
        sb.append(String.format("|   Seek Std Deviation: %-42.2f |\n", metrics.seekStdDev));
        
        sb.append("+------------------------------------------------------------------+\n");
        
        // Service order
        sb.append("| Service Order:                                                  |\n");
        List<Integer> serviceOrder = result.getServiceOrder();
        String orderStr = serviceOrder.toString();
        // Wrap long lines
        int maxLineLen = 60;
        while (orderStr.length() > maxLineLen) {
            int breakPoint = orderStr.lastIndexOf(',', maxLineLen);
            if (breakPoint < 0) breakPoint = maxLineLen;
            sb.append(String.format("|   %-63s |\n", orderStr.substring(0, breakPoint + 1)));
            orderStr = orderStr.substring(breakPoint + 1).trim();
        }
        sb.append(String.format("|   %-63s |\n", orderStr));
        
        sb.append("+------------------------------------------------------------------+\n");
        
        // Head path  
        sb.append("| Head Path:                                                      |\n");
        List<Integer> headPath = result.getHeadPath();
        String pathStr = headPath.toString();
        while (pathStr.length() > maxLineLen) {
            int breakPoint = pathStr.lastIndexOf(',', maxLineLen);
            if (breakPoint < 0) breakPoint = maxLineLen;
            sb.append(String.format("|   %-63s |\n", pathStr.substring(0, breakPoint + 1)));
            pathStr = pathStr.substring(breakPoint + 1).trim();
        }
        sb.append(String.format("|   %-63s |\n", pathStr));
        
        sb.append("+==================================================================+\n");
        
        resultArea.setText(sb.toString());
        resultArea.setCaretPosition(0);
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
        resultArea.setText("Error: " + message + "\n");
    }
    
    /**
     * Main entry point for the GUI application.
     */
    public static void main(String[] args) {
        // Use system look and feel for better integration
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fall back to default look and feel
        }
        
        // Create and show GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            DiskSchedulerGUI gui = new DiskSchedulerGUI();
            gui.setVisible(true);
        });
    }
}
