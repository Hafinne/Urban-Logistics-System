import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class MainApp extends JFrame {

    // --- Core Backend Data Structures ---
    private MasterRegistrySLL masterLog = new MasterRegistrySLL();
    private IntakeBufferDLL intakeBuffer = new IntakeBufferDLL();
    private StandardDeliveryQueue deliveryQueue = new StandardDeliveryQueue();
    private TruckLoadingStack truckStack = new TruckLoadingStack();
    private AddressDirectoryAVL addressDirectory = new AddressDirectoryAVL();
    private CityGraph cityMap = new CityGraph();

    // --- Styling Constants ---
    public static final Color COLOR_BG_DARK = new Color(12, 12, 16);     // Dark tech space black
    public static final Color COLOR_CARD_BG = new Color(20, 20, 28);     // Card background
    public static final Color COLOR_CYAN = new Color(0, 229, 255);        // Electric neon cyan
    public static final Color COLOR_NEON_GREEN = new Color(57, 255, 20); // Toxic neon green
    public static final Color COLOR_PINK = new Color(255, 0, 127);       // Cyberpunk magenta/pink
    public static final Color COLOR_TEXT = new Color(220, 225, 235);     // Modern off-white text
    public static final Color COLOR_MUTED = new Color(100, 105, 120);    // Muted grey text
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_DIGITAL = new Font("Monospaced", Font.BOLD, 12);

    // --- UI Component References ---
    private JComboBox<String> cbStartNode;
    private JComboBox<String> cbEndNode;
    private JTextField tfPackageId;
    private JTextField tfPackageDest;
    private JTextField tfSearchNeighborhood;
    private JTextArea taLogConsole;

    // Panels that need repainting
    private DLLVisualPanel dllPanel;
    private DispatchVisualPanel dispatchPanel;
    private GraphVisualPanel graphPanel;
    private AVLVisualPanel avlPanel;

    // Animation Tick Count
    private int tickCount = 0;

    public MainApp() {
        super("ENCODERS ANTIGRAVITY DISPATCH TERMINAL v2.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1350, 880);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG_DARK);
        setLayout(new BorderLayout(10, 10));

        // Create UI parts
        initHeaderPanel();
        initMainDashboard();

        // Load Default Data Automatically
        autoLoadData();

        // Start Animation / Physics Loop (60 FPS)
        javax.swing.Timer animTimer = new javax.swing.Timer(16, e -> {
            tickCount++;
            repaintVisuals();
        });
        animTimer.start();
    }

    private void repaintVisuals() {
        if (dllPanel != null) dllPanel.repaint();
        if (dispatchPanel != null) dispatchPanel.repaint();
        if (graphPanel != null) graphPanel.repaint();
        if (avlPanel != null) avlPanel.repaint();
    }

    private void logMessage(String system, String text) {
        String timeStamp = String.format("[%04d]", tickCount % 10000);
        taLogConsole.append(timeStamp + " [" + system + "] " + text + "\n");
        taLogConsole.setCaretPosition(taLogConsole.getDocument().getLength());
    }

    // --- AUTOMATIC LOADING OF DEFAULT FILES ---
    private void autoLoadData() {
        File mapFile = new File("mapData.txt");
        if (mapFile.exists()) {
            loadMapDataFromFile(mapFile);
        } else {
            logMessage("WARNING", "mapData.txt not found. Please load manually.");
        }

        File pkgFile = new File("packageData.txt");
        if (pkgFile.exists()) {
            loadPackageDataFromFile(pkgFile);
        } else {
            logMessage("WARNING", "packageData.txt not found. Please load manually.");
        }
    }

    private void loadMapDataFromFile(File file) {
        try (Scanner sc = new Scanner(file)) {
            int count = 0;
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.startsWith("#") || line.isEmpty()) continue;
                String[] p = line.split("\\s+");
                if (p.length >= 3) {
                    cityMap.addEdge(p[0], p[1], Integer.parseInt(p[2]));
                    count++;
                }
            }
            logMessage("SYSTEM", "Loaded " + count + " edges into CityGraph.");
            populateNodeComboBoxes();
        } catch (Exception e) {
            logMessage("ERROR", "Map load failed: " + e.getMessage());
        }
    }

    private void loadPackageDataFromFile(File file) {
        try (Scanner sc = new Scanner(file)) {
            int count = 0;
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.startsWith("#") || line.isEmpty() || !line.contains(" ")) continue;
                String[] p = line.split("\\s+");
                if (p.length >= 2) {
                    Package pkg = new Package(p[0], p[1]);
                    masterLog.addRecord(pkg);
                    intakeBuffer.insertAtTail(pkg);
                    addressDirectory.insert(p[1], "ID_" + p[0]);
                    count++;
                }
            }
            logMessage("SYSTEM", "Loaded " + count + " packages into SLL, DLL & AVL Tree.");
        } catch (Exception e) {
            logMessage("ERROR", "Package load failed: " + e.getMessage());
        }
    }

    private void populateNodeComboBoxes() {
        cbStartNode.removeAllItems();
        cbEndNode.removeAllItems();
        Set<String> nodes = cityMap.getAdjList().keySet();
        for (String node : nodes) {
            cbStartNode.addItem(node);
            cbEndNode.addItem(node);
        }
        // Set default select values if possible
        if (nodes.contains("Meydan")) {
            cbStartNode.setSelectedItem("Meydan");
        }
        if (nodes.contains("Talas")) {
            cbEndNode.setSelectedItem("Talas");
        }
    }

    // --- UI DESIGN METHODS ---

    // Top Header with Branding and Controls
    private void initHeaderPanel() {
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Paint dark dashboard tech background with neon bottom highlight
                g2.setColor(COLOR_CARD_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Glowing bottom strip
                g2.setColor(COLOR_CYAN);
                g2.fillRect(0, getHeight() - 4, getWidth(), 4);

                // Add grid lines for cyberpunk overlay
                g2.setColor(new Color(255, 255, 255, 10));
                for (int i = 0; i < getWidth(); i += 40) {
                    g2.drawLine(i, 0, i, getHeight());
                }
            }
        };
        headerPanel.setPreferredSize(new Dimension(getWidth(), 110));
        headerPanel.setLayout(new BorderLayout(10, 10));
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Branding
        JPanel brandingPanel = new JPanel(new GridLayout(2, 1));
        brandingPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("ENCODERS LOGISTICS & DISTRIBUTION");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_CYAN);
        JLabel lblSub = new JLabel("ANTIGRAVITY OPERATIONAL HUB // CONTROL TERMINAL V2.0");
        lblSub.setFont(FONT_DIGITAL);
        lblSub.setForeground(COLOR_NEON_GREEN);
        brandingPanel.add(lblTitle);
        brandingPanel.add(lblSub);
        headerPanel.add(brandingPanel, BorderLayout.WEST);

        // Control Inputs (Manual Registration, File Loader, Queue Operations)
        JPanel controlGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        controlGroup.setOpaque(false);

        // Manual Entry Panel
        JPanel manualEntryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        manualEntryPanel.setOpaque(false);
        manualEntryPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_MUTED), "REGISTER NEW CARGO", 
                0, 0, FONT_DIGITAL, COLOR_CYAN));

        JLabel lblId = new JLabel("ID:");
        lblId.setForeground(COLOR_TEXT);
        lblId.setFont(FONT_DIGITAL);
        tfPackageId = new JTextField("PKG999", 6);
        tfPackageId.setBackground(COLOR_BG_DARK);
        tfPackageId.setForeground(COLOR_NEON_GREEN);
        tfPackageId.setCaretColor(COLOR_NEON_GREEN);
        tfPackageId.setBorder(BorderFactory.createLineBorder(COLOR_MUTED));

        JLabel lblDest = new JLabel("DEST:");
        lblDest.setForeground(COLOR_TEXT);
        lblDest.setFont(FONT_DIGITAL);
        tfPackageDest = new JTextField("Talas", 8);
        tfPackageDest.setBackground(COLOR_BG_DARK);
        tfPackageDest.setForeground(COLOR_NEON_GREEN);
        tfPackageDest.setCaretColor(COLOR_NEON_GREEN);
        tfPackageDest.setBorder(BorderFactory.createLineBorder(COLOR_MUTED));

        JButton btnRegister = new NeonButton("REGISTER", COLOR_NEON_GREEN);
        btnRegister.addActionListener(e -> {
            String id = tfPackageId.getText().trim();
            String dest = tfPackageDest.getText().trim();
            if (id.isEmpty() || dest.isEmpty()) {
                logMessage("ERROR", "Cargo ID and Destination must not be empty.");
                return;
            }
            Package newP = new Package(id, dest);
            masterLog.addRecord(newP);
            intakeBuffer.insertAtTail(newP);
            addressDirectory.insert(dest, "ID_" + id);
            logMessage("OPERATION", "Package " + newP + " registered into DLL buffer and AVL address directory.");
        });

        manualEntryPanel.add(lblId);
        manualEntryPanel.add(tfPackageId);
        manualEntryPanel.add(lblDest);
        manualEntryPanel.add(tfPackageDest);
        manualEntryPanel.add(btnRegister);
        controlGroup.add(manualEntryPanel);

        // Buffer Dispatch Operations
        JPanel dispatchOps = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        dispatchOps.setOpaque(false);
        dispatchOps.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_MUTED), "PHYSICAL FLOW SYSTEMS", 
                0, 0, FONT_DIGITAL, COLOR_CYAN));

        JButton btnBufferProcess = new NeonButton("PROCESS BUFFER", COLOR_CYAN);
        btnBufferProcess.addActionListener(e -> {
            Package p = intakeBuffer.removeFromHead();
            if (p != null) {
                deliveryQueue.enqueue(p);
                logMessage("FLOW", "Moved package " + p + " from Intake Buffer (DLL) to Standard Queue (FIFO).");
            } else {
                logMessage("WARNING", "Intake Buffer (DLL) is empty.");
            }
        });

        JButton btnLoadTruck = new NeonButton("LOAD TRUCK", COLOR_PINK);
        btnLoadTruck.addActionListener(e -> {
            Package p = deliveryQueue.dequeue();
            if (p != null) {
                truckStack.push(p);
                logMessage("FLOW", "Pushed package " + p + " from Queue (FIFO) to Truck Loading Stack (LIFO).");
            } else {
                logMessage("WARNING", "Delivery Queue (FIFO) is empty.");
            }
        });

        dispatchOps.add(btnBufferProcess);
        dispatchOps.add(btnLoadTruck);
        controlGroup.add(dispatchOps);

        // Load Files Manually
        JPanel fileOps = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        fileOps.setOpaque(false);
        fileOps.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_MUTED), "DATA FILES", 
                0, 0, FONT_DIGITAL, COLOR_CYAN));

        JButton btnLoadMap = new NeonButton("LOAD MAP", COLOR_TEXT);
        btnLoadMap.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(".");
            int val = chooser.showOpenDialog(MainApp.this);
            if (val == JFileChooser.APPROVE_OPTION) {
                loadMapDataFromFile(chooser.getSelectedFile());
            }
        });

        JButton btnLoadPkg = new NeonButton("LOAD PKG", COLOR_TEXT);
        btnLoadPkg.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(".");
            int val = chooser.showOpenDialog(MainApp.this);
            if (val == JFileChooser.APPROVE_OPTION) {
                loadPackageDataFromFile(chooser.getSelectedFile());
            }
        });

        fileOps.add(btnLoadMap);
        fileOps.add(btnLoadPkg);
        controlGroup.add(fileOps);

        headerPanel.add(controlGroup, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
    }

    // Set up dashboard sections split in half
    private void initMainDashboard() {
        // Main split pane dividing Left Column and Right Column
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(520);
        mainSplit.setDividerSize(5);
        mainSplit.setBackground(COLOR_BG_DARK);
        mainSplit.setOpaque(false);
        mainSplit.setBorder(new EmptyBorder(10, 10, 10, 10));

        // LEFT COLUMN: DLL (top), Queue/Stack (middle), SLL Audit Logs Console (bottom)
        JPanel leftColPanel = new JPanel();
        leftColPanel.setLayout(new BoxLayout(leftColPanel, BoxLayout.Y_AXIS));
        leftColPanel.setOpaque(false);

        dllPanel = new DLLVisualPanel();
        dllPanel.setPreferredSize(new Dimension(500, 180));
        dllPanel.setMaximumSize(new Dimension(500, 180));
        leftColPanel.add(dllPanel);
        leftColPanel.add(Box.createVerticalStrut(10));

        dispatchPanel = new DispatchVisualPanel();
        dispatchPanel.setPreferredSize(new Dimension(500, 290));
        dispatchPanel.setMaximumSize(new Dimension(500, 290));
        leftColPanel.add(dispatchPanel);
        leftColPanel.add(Box.createVerticalStrut(10));

        // Logs panel
        JPanel logConsolePanel = new JPanel(new BorderLayout());
        logConsolePanel.setOpaque(false);
        logConsolePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_MUTED), "OPERATIONAL SYSTEM AUDIT (SLL LOG)",
                0, 0, FONT_DIGITAL, COLOR_CYAN));
        taLogConsole = new JTextArea();
        taLogConsole.setBackground(new Color(8, 8, 12));
        taLogConsole.setForeground(COLOR_NEON_GREEN);
        taLogConsole.setFont(FONT_DIGITAL);
        taLogConsole.setEditable(false);
        taLogConsole.setCaretColor(COLOR_NEON_GREEN);
        JScrollPane scrollLogs = new JScrollPane(taLogConsole);
        scrollLogs.setBorder(null);
        scrollLogs.getViewport().setBackground(new Color(8, 8, 12));
        logConsolePanel.add(scrollLogs, BorderLayout.CENTER);

        // Panel controls to trigger standard display log
        JButton btnShowSLL = new NeonButton("PRINT ALL SLL DAILY RECORDS", COLOR_CYAN);
        btnShowSLL.addActionListener(e -> {
            logMessage("COMMAND", "Dumping SLL Master Registry Log to stdout and local GUI...");
            masterLog.displayLog(); // Prints to system out
            // Also list in console
            logMessage("LOG-DUMP", "--- Start SLL Log Dump ---");
            MasterRegistrySLL.Node current = masterLog.getHead();
            while (current != null) {
                logMessage("RECORD", current.pkg.toString());
                current = current.next;
            }
            logMessage("LOG-DUMP", "--- End SLL Log Dump ---");
        });
        logConsolePanel.add(btnShowSLL, BorderLayout.SOUTH);

        leftColPanel.add(logConsolePanel);

        // RIGHT COLUMN: Map Panel (top), AVL Panel (bottom)
        JPanel rightColPanel = new JPanel(new BorderLayout(0, 10));
        rightColPanel.setOpaque(false);

        // Map Panel Grouping
        JPanel mapGroup = new JPanel(new BorderLayout());
        mapGroup.setOpaque(false);
        mapGroup.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_MUTED), "KAYSERI URBAN INFRASTRUCTURE NETWORK (GRAPH ROUTING)",
                0, 0, FONT_DIGITAL, COLOR_CYAN));

        graphPanel = new GraphVisualPanel();
        mapGroup.add(graphPanel, BorderLayout.CENTER);

        // Map routing control bar
        JPanel routingBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        routingBar.setBackground(COLOR_CARD_BG);
        routingBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_MUTED));

        JLabel lblStart = new JLabel("START:");
        lblStart.setForeground(COLOR_TEXT);
        lblStart.setFont(FONT_DIGITAL);
        cbStartNode = new JComboBox<>();
        cbStartNode.setBackground(COLOR_BG_DARK);
        cbStartNode.setForeground(COLOR_CYAN);
        cbStartNode.setFont(FONT_BODY);

        JLabel lblEnd = new JLabel("END:");
        lblEnd.setForeground(COLOR_TEXT);
        lblEnd.setFont(FONT_DIGITAL);
        cbEndNode = new JComboBox<>();
        cbEndNode.setBackground(COLOR_BG_DARK);
        cbEndNode.setForeground(COLOR_CYAN);
        cbEndNode.setFont(FONT_BODY);

        JButton btnDijkstra = new NeonButton("DIJKSTRA PATH", COLOR_NEON_GREEN);
        btnDijkstra.addActionListener(e -> {
            String start = (String) cbStartNode.getSelectedItem();
            String end = (String) cbEndNode.getSelectedItem();
            if (start == null || end == null) return;
            logMessage("DIJKSTRA", "Calculating shortest path from " + start + " to " + end);
            cityMap.calculateShortestPath(start, end); // Call original method to print on stdout
            
            // Get path for GUI animation
            List<String> path = cityMap.getShortestPathList(start, end);
            if (path != null) {
                logMessage("DIJKSTRA", "Path computed: " + String.join(" -> ", path));
                graphPanel.triggerDijkstraAnimation(path);
            } else {
                logMessage("ERROR", "No routing possible between " + start + " and " + end);
            }
        });

        JButton btnMST = new NeonButton("MIN SPANNING TREE", COLOR_PINK);
        btnMST.addActionListener(e -> {
            logMessage("PRIM-MST", "Calculating minimum spanning tree infrastructure optimization...");
            cityMap.calculateMST(); // Prints to system out
            List<CityGraph.MSTEdge> mst = cityMap.getMSTEdges();
            graphPanel.triggerMSTHighlight(mst);
            logMessage("PRIM-MST", "Highlighted optimized MST network in Pink.");
        });

        routingBar.add(lblStart);
        routingBar.add(cbStartNode);
        routingBar.add(lblEnd);
        routingBar.add(cbEndNode);
        routingBar.add(btnDijkstra);
        routingBar.add(btnMST);
        mapGroup.add(routingBar, BorderLayout.SOUTH);

        rightColPanel.add(mapGroup, BorderLayout.CENTER);

        // AVL Directory Panel Grouping
        JPanel avlGroup = new JPanel(new BorderLayout());
        avlGroup.setOpaque(false);
        avlGroup.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_MUTED), "ADDRESS DATABASE DIRECTORY (AVL TREE)",
                0, 0, FONT_DIGITAL, COLOR_CYAN));

        avlPanel = new AVLVisualPanel();
        avlGroup.add(avlPanel, BorderLayout.CENTER);

        // AVL search control bar
        JPanel avlBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        avlBar.setBackground(COLOR_CARD_BG);
        avlBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_MUTED));

        JLabel lblSearch = new JLabel("NEIGHBORHOOD:");
        lblSearch.setForeground(COLOR_TEXT);
        lblSearch.setFont(FONT_DIGITAL);
        tfSearchNeighborhood = new JTextField("Talas", 12);
        tfSearchNeighborhood.setBackground(COLOR_BG_DARK);
        tfSearchNeighborhood.setForeground(COLOR_CYAN);
        tfSearchNeighborhood.setBorder(BorderFactory.createLineBorder(COLOR_MUTED));

        JButton btnSearchAVL = new NeonButton("SEARCH", COLOR_CYAN);
        btnSearchAVL.addActionListener(e -> {
            String nh = tfSearchNeighborhood.getText().trim();
            if (nh.isEmpty()) return;
            logMessage("DATABASE", "Searching address registry AVL for: " + nh);
            String result = addressDirectory.search(nh);
            logMessage("DATABASE", "AVL Result: " + result);
            avlPanel.highlightNode(nh);
        });

        avlBar.add(lblSearch);
        avlBar.add(tfSearchNeighborhood);
        avlBar.add(btnSearchAVL);
        avlGroup.add(avlBar, BorderLayout.SOUTH);

        rightColPanel.add(avlGroup, BorderLayout.SOUTH);
        avlGroup.setPreferredSize(new Dimension(800, 310));

        mainSplit.setLeftComponent(leftColPanel);
        mainSplit.setRightComponent(rightColPanel);
        add(mainSplit, BorderLayout.CENTER);
    }

    // --- CUSTOM PAINTING PANELS ---

    // 1. Intake Buffer DLL Panel
    private class DLLVisualPanel extends JPanel {
        DLLVisualPanel() {
            setOpaque(false);
            setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(COLOR_MUTED), "INTAKE BUFFER (DOUBLE LINKED LIST)", 
                    0, 0, FONT_DIGITAL, COLOR_CYAN));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Background Card
            g2.setColor(COLOR_CARD_BG);
            g2.fillRoundRect(5, 15, getWidth() - 10, getHeight() - 20, 10, 10);
            g2.setColor(COLOR_MUTED);
            g2.drawRoundRect(5, 15, getWidth() - 10, getHeight() - 20, 10, 10);

            // Traverse DLL
            IntakeBufferDLL.Node head = intakeBuffer.getHead();
            if (head == null) {
                g2.setColor(COLOR_MUTED);
                g2.setFont(FONT_SUBTITLE);
                g2.drawString("DLL BUFFER EMPTY. NO PACKAGES REGISTERED.", 50, getHeight() / 2 + 5);
                return;
            }

            int cardWidth = 85;
            int cardHeight = 50;
            int gap = 35;
            int yPos = getHeight() / 2 - 15;
            int xPos = 25;

            IntakeBufferDLL.Node curr = head;
            int nodeCount = 0;
            while (curr != null) {
                // Draw floating card
                double floatOffset = Math.sin(tickCount * 0.08 + nodeCount) * 4.0;
                int currentY = yPos + (int) floatOffset;

                // Box fill and neon outline
                g2.setColor(new Color(0, 229, 255, 20));
                g2.fillRoundRect(xPos, currentY, cardWidth, cardHeight, 8, 8);
                g2.setColor(COLOR_CYAN);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(xPos, currentY, cardWidth, cardHeight, 8, 8);

                // Package ID & Destination text
                g2.setFont(FONT_DIGITAL);
                g2.setColor(COLOR_TEXT);
                g2.drawString(curr.pkg.packageID, xPos + 8, currentY + 20);
                g2.setFont(FONT_BODY);
                g2.setColor(COLOR_MUTED);
                g2.drawString(truncateText(curr.pkg.destination, 10), xPos + 8, currentY + 40);

                // Pointers tags (Head & Tail)
                if (curr == head) {
                    g2.setFont(FONT_DIGITAL);
                    g2.setColor(COLOR_NEON_GREEN);
                    g2.drawString("[HEAD]", xPos + 18, currentY - 8);
                }
                if (curr.next == null) {
                    g2.setFont(FONT_DIGITAL);
                    g2.setColor(COLOR_PINK);
                    g2.drawString("[TAIL]", xPos + 18, currentY + cardHeight + 15);
                }

                // Connections
                if (curr.next != null) {
                    int nextX = xPos + cardWidth;
                    int arrowY = currentY + cardHeight / 2;
                    g2.setColor(COLOR_NEON_GREEN);
                    g2.setStroke(new BasicStroke(1.0f));
                    
                    // Double arrow line
                    g2.drawLine(nextX + 2, arrowY, nextX + gap - 4, arrowY);
                    // Next arrow head
                    g2.drawLine(nextX + gap - 8, arrowY - 4, nextX + gap - 4, arrowY);
                    g2.drawLine(nextX + gap - 8, arrowY + 4, nextX + gap - 4, arrowY);
                    // Prev arrow head
                    g2.drawLine(nextX + 6, arrowY - 4, nextX + 2, arrowY);
                    g2.drawLine(nextX + 6, arrowY + 4, nextX + 2, arrowY);
                }

                xPos += cardWidth + gap;
                curr = curr.next;
                nodeCount++;
                if (xPos + cardWidth > getWidth() - 20) {
                    // Cutoff indicator
                    g2.setColor(COLOR_MUTED);
                    g2.drawString("+MORE", xPos, yPos + 25);
                    break;
                }
            }
        }
    }

    // 2. Dispatch FIFO Queue and LIFO Stack Panel
    private class DispatchVisualPanel extends JPanel {
        DispatchVisualPanel() {
            setOpaque(false);
            setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(COLOR_MUTED), "DISPATCH SYSTEMS (FIFO QUEUE & LIFO STACK)", 
                    0, 0, FONT_DIGITAL, COLOR_CYAN));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Card Panel Background
            g2.setColor(COLOR_CARD_BG);
            g2.fillRoundRect(5, 15, getWidth() - 10, getHeight() - 20, 10, 10);
            g2.setColor(COLOR_MUTED);
            g2.drawRoundRect(5, 15, getWidth() - 10, getHeight() - 20, 10, 10);

            int width = getWidth();
            int height = getHeight();

            // Split the card space vertically into left half (Queue) and right half (Stack)
            int splitX = width / 2;
            g2.setColor(new Color(255, 255, 255, 20));
            g2.drawLine(splitX, 25, splitX, height - 15);

            // --- LEFT HALF: Standard Delivery FIFO Queue ---
            g2.setFont(FONT_SUBTITLE);
            g2.setColor(COLOR_CYAN);
            g2.drawString("STANDARD DELIVERY (FIFO QUEUE)", 20, 42);

            // Queue pipe silo
            int qPipeX = 20;
            int qPipeY = 65;
            int qPipeW = splitX - 40;
            int qPipeH = 180;
            g2.setColor(new Color(255, 255, 255, 5));
            g2.fillRect(qPipeX, qPipeY, qPipeW, qPipeH);
            g2.setColor(new Color(0, 229, 255, 50));
            g2.drawRect(qPipeX, qPipeY, qPipeW, qPipeH);
            
            // Tube side highlights
            g2.setColor(new Color(0, 229, 255, 80));
            g2.drawLine(qPipeX, qPipeY + 15, qPipeX + qPipeW, qPipeY + 15);
            g2.drawLine(qPipeX, qPipeY + qPipeH - 15, qPipeX + qPipeW, qPipeY + qPipeH - 15);

            // Draw FIFO items (Horizontal loading)
            StandardDeliveryQueue.Node qCurr = deliveryQueue.getFront();
            int qBoxW = 60;
            int qBoxH = 110;
            int qX = qPipeX + 10;
            int qY = qPipeY + 35;
            int boxCount = 0;

            if (qCurr == null) {
                g2.setColor(COLOR_MUTED);
                g2.setFont(FONT_DIGITAL);
                g2.drawString("[ QUEUE PIPELINE EMPTY ]", qPipeX + qPipeW/2 - 80, qPipeY + qPipeH/2 + 5);
            }

            while (qCurr != null) {
                // Drawing box
                g2.setColor(new Color(0, 229, 255, 30));
                g2.fillRoundRect(qX, qY, qBoxW, qBoxH, 6, 6);
                g2.setColor(COLOR_CYAN);
                g2.drawRoundRect(qX, qY, qBoxW, qBoxH, 6, 6);

                g2.setFont(FONT_DIGITAL);
                g2.setColor(COLOR_TEXT);
                g2.drawString(qCurr.pkg.packageID, qX + 6, qY + 40);
                g2.setFont(FONT_BODY);
                g2.setColor(COLOR_MUTED);
                g2.drawString(truncateText(qCurr.pkg.destination, 6), qX + 6, qY + 70);

                // Front & Rear markers
                if (qCurr == deliveryQueue.getFront()) {
                    g2.setFont(FONT_DIGITAL);
                    g2.setColor(COLOR_NEON_GREEN);
                    g2.drawString("<-FRONT", qX + 2, qY - 10);
                }
                if (qCurr.next == null) {
                    g2.setFont(FONT_DIGITAL);
                    g2.setColor(COLOR_PINK);
                    g2.drawString("<-REAR", qX + 6, qY + qBoxH + 15);
                }

                qX += qBoxW + 10;
                qCurr = qCurr.next;
                boxCount++;
                if (qX + qBoxW > qPipeX + qPipeW - 10) {
                    g2.setColor(COLOR_MUTED);
                    g2.drawString("...", qX, qY + 50);
                    break;
                }
            }


            // --- RIGHT HALF: Truck Loading LIFO Stack ---
            g2.setFont(FONT_SUBTITLE);
            g2.setColor(COLOR_PINK);
            g2.drawString("TRUCK LOADING (LIFO STACK)", splitX + 20, 42);

            // Loading silo container (drawn like a loading tube)
            int siloX = splitX + 50;
            int siloY = 65;
            int siloW = 140;
            int siloH = 180;
            
            // Draw silo structure
            g2.setColor(new Color(255, 255, 255, 5));
            g2.fillRect(siloX, siloY, siloW, siloH);
            g2.setColor(new Color(255, 0, 127, 40));
            g2.drawRect(siloX, siloY, siloW, siloH);
            // Open top indicator
            g2.setColor(COLOR_CARD_BG);
            g2.fillRect(siloX + 5, siloY - 2, siloW - 10, 5);

            // Draw stack items inside silo (drawn from bottom up)
            TruckLoadingStack.Node sCurr = truckStack.getTop();
            int sBoxH = 34;
            int sBoxW = siloW - 20;
            int sX = siloX + 10;
            int sY = siloY + siloH - 10 - sBoxH; // Start at the bottom of the silo

            if (sCurr == null) {
                g2.setColor(COLOR_MUTED);
                g2.setFont(FONT_DIGITAL);
                g2.drawString("[ SILO EMPTY ]", siloX + siloW/2 - 45, siloY + siloH/2 + 5);
            }

            int stackIndex = 0;
            while (sCurr != null) {
                // Highlight top node
                boolean isTop = (stackIndex == 0);

                g2.setColor(isTop ? new Color(255, 0, 127, 30) : new Color(255, 255, 255, 10));
                g2.fillRoundRect(sX, sY, sBoxW, sBoxH, 5, 5);
                g2.setColor(isTop ? COLOR_PINK : COLOR_MUTED);
                g2.drawRoundRect(sX, sY, sBoxW, sBoxH, 5, 5);

                g2.setFont(FONT_DIGITAL);
                g2.setColor(COLOR_TEXT);
                g2.drawString(sCurr.pkg.packageID + " -> " + truncateText(sCurr.pkg.destination, 7), sX + 10, sY + 22);

                if (isTop) {
                    g2.setFont(FONT_DIGITAL);
                    g2.setColor(COLOR_NEON_GREEN);
                    g2.drawString("TOP", sX + sBoxW + 6, sY + 22);
                }

                sY -= (sBoxH + 6);
                sCurr = sCurr.next;
                stackIndex++;

                if (sY < siloY + 5) {
                    g2.setColor(COLOR_PINK);
                    g2.drawString("MAX CAP EXCEEDED", siloX + 20, siloY - 12);
                    break;
                }
            }
        }
    }

    // Helper text truncation
    private static String truncateText(String s, int len) {
        if (s.length() <= len) return s;
        return s.substring(0, len - 2) + "..";
    }

    // 3. Graph visualizer with coordinates, float animation, and Dijkstra/MST highlights
    private class GraphVisualPanel extends JPanel {
        // Spatial Coordinates for Kayseri neighborhoods
        private final Map<String, Point> nodeCoords = new HashMap<>();
        private List<String> activePath = null;
        private List<CityGraph.MSTEdge> activeMST = null;

        // Dijkstra animated delivery package state
        private boolean pathAnimationActive = false;
        private double animationProgress = 0.0; // Float progress from 0.0 to activePath.size()-1
        private double animationSpeed = 0.035;

        GraphVisualPanel() {
            setOpaque(false);
            // hardcode approximate positions for clear layout
            nodeCoords.put("Meydan", new Point(290, 150));
            nodeCoords.put("Alpaslan", new Point(400, 100));
            nodeCoords.put("Talas", new Point(420, 210));
            nodeCoords.put("Erkilet", new Point(190, 70));
            nodeCoords.put("Belsin", new Point(150, 170));
            nodeCoords.put("Ildem", new Point(530, 80));
            nodeCoords.put("Mimsin", new Point(510, 210));
            nodeCoords.put("Anbar", new Point(70, 210));
        }

        public void triggerDijkstraAnimation(List<String> path) {
            this.activePath = path;
            this.activeMST = null; // Turn off MST display
            this.animationProgress = 0.0;
            this.pathAnimationActive = true;
            logMessage("SIMULATION", "Antigravity package traversal launched along shortest route.");
        }

        public void triggerMSTHighlight(List<CityGraph.MSTEdge> mst) {
            this.activeMST = mst;
            this.activePath = null; // Turn off path display
            this.pathAnimationActive = false;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Card Panel Background
            g2.setColor(COLOR_CARD_BG);
            g2.fillRoundRect(5, 15, getWidth() - 10, getHeight() - 20, 10, 10);
            g2.setColor(COLOR_MUTED);
            g2.drawRoundRect(5, 15, getWidth() - 10, getHeight() - 20, 10, 10);

            // Compute tick offsets for floating spheres
            Map<String, Point> currentPositions = new HashMap<>();
            for (Map.Entry<String, Point> entry : nodeCoords.entrySet()) {
                double floatOffset = Math.sin(tickCount * 0.045 + entry.getKey().hashCode()) * 6.0;
                double floatOffsetX = Math.cos(tickCount * 0.03 + entry.getKey().hashCode()) * 3.0;
                Point base = entry.getValue();
                // Map the base points coordinates relative to panel size
                int screenX = (int) (base.x * (getWidth() / 650.0)) + (int) floatOffsetX;
                int screenY = (int) (base.y * (getHeight() / 310.0)) + (int) floatOffset + 15;
                currentPositions.put(entry.getKey(), new Point(screenX, screenY));
            }

            // Draw Default Edges first
            Map<String, List<CityGraph.Edge>> adj = cityMap.getAdjList();
            g2.setStroke(new BasicStroke(1.0f));
            for (String source : adj.keySet()) {
                Point p1 = currentPositions.get(source);
                if (p1 == null) continue;
                for (CityGraph.Edge edge : adj.get(source)) {
                    Point p2 = currentPositions.get(edge.target);
                    if (p2 == null) continue;
                    
                    // Draw dim grey default connection
                    g2.setColor(new Color(255, 255, 255, 15));
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                    
                    // Draw distance weight in center
                    int midX = (p1.x + p2.x) / 2;
                    int midY = (p1.y + p2.y) / 2;
                    g2.setFont(FONT_DIGITAL);
                    g2.setColor(COLOR_MUTED);
                    g2.drawString(edge.weight + "km", midX - 10, midY + 4);
                }
            }

            // Draw MST Highlight if active
            if (activeMST != null) {
                g2.setStroke(new BasicStroke(3.0f));
                for (CityGraph.MSTEdge edge : activeMST) {
                    Point p1 = currentPositions.get(edge.source);
                    Point p2 = currentPositions.get(edge.destination);
                    if (p1 != null && p2 != null) {
                        // Pink glow spanning tree lines
                        g2.setColor(new Color(255, 0, 127, 40));
                        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.setColor(COLOR_PINK);
                        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                    }
                }
            }

            // Draw Dijkstra Shortest Path Highlight if active
            if (activePath != null && activePath.size() > 1) {
                g2.setStroke(new BasicStroke(3.0f));
                for (int i = 0; i < activePath.size() - 1; i++) {
                    Point p1 = currentPositions.get(activePath.get(i));
                    Point p2 = currentPositions.get(activePath.get(i + 1));
                    if (p1 != null && p2 != null) {
                        // Cyan glowing path lines
                        g2.setColor(new Color(0, 229, 255, 40));
                        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.setColor(COLOR_CYAN);
                        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                    }
                }
            }

            // Draw Nodes (Floating glass spheres)
            int sphereRadius = 24;
            for (Map.Entry<String, Point> node : currentPositions.entrySet()) {
                Point pt = node.getValue();
                String name = node.getKey();

                // Check if node is part of active path
                boolean isHighlighted = (activePath != null && activePath.contains(name));
                boolean isMSTHighlighted = false;
                if (activeMST != null) {
                    for (CityGraph.MSTEdge e : activeMST) {
                        if (e.source.equals(name) || e.destination.equals(name)) {
                            isMSTHighlighted = true;
                            break;
                        }
                    }
                }

                // Draw outer glowing halo if highlighted
                if (isHighlighted) {
                    g2.setColor(new Color(0, 229, 255, 40));
                    g2.fillOval(pt.x - sphereRadius - 4, pt.y - sphereRadius - 4, (sphereRadius + 4) * 2, (sphereRadius + 4) * 2);
                } else if (isMSTHighlighted) {
                    g2.setColor(new Color(255, 0, 127, 40));
                    g2.fillOval(pt.x - sphereRadius - 4, pt.y - sphereRadius - 4, (sphereRadius + 4) * 2, (sphereRadius + 4) * 2);
                }

                // Draw radial gradient sphere bubble
                RadialGradientPaint gradient = new RadialGradientPaint(
                        new Point2D.Double(pt.x - sphereRadius / 3.0, pt.y - sphereRadius / 3.0),
                        sphereRadius,
                        new float[]{0.0f, 0.7f, 1.0f},
                        new Color[]{
                                new Color(255, 255, 255, 80),
                                isHighlighted ? new Color(0, 229, 255, 40) : (isMSTHighlighted ? new Color(255, 0, 127, 40) : new Color(30, 30, 45, 100)),
                                isHighlighted ? COLOR_CYAN : (isMSTHighlighted ? COLOR_PINK : COLOR_MUTED)
                        }
                );
                g2.setPaint(gradient);
                g2.fillOval(pt.x - sphereRadius, pt.y - sphereRadius, sphereRadius * 2, sphereRadius * 2);

                // Draw crisp border outline
                g2.setStroke(new BasicStroke(1.2f));
                g2.setColor(isHighlighted ? COLOR_CYAN : (isMSTHighlighted ? COLOR_PINK : new Color(255, 255, 255, 30)));
                g2.drawOval(pt.x - sphereRadius, pt.y - sphereRadius, sphereRadius * 2, sphereRadius * 2);

                // Label Text
                g2.setFont(FONT_DIGITAL);
                g2.setColor(isHighlighted ? COLOR_CYAN : (isMSTHighlighted ? COLOR_PINK : COLOR_TEXT));
                g2.drawString(name, pt.x - sphereRadius, pt.y - sphereRadius - 6);
            }

            // Animate package traversing the shortest path
            if (pathAnimationActive && activePath != null && activePath.size() > 1) {
                // Increment animation ticks
                animationProgress += animationSpeed;
                if (animationProgress >= activePath.size() - 1) {
                    animationProgress = activePath.size() - 1;
                    pathAnimationActive = false;
                    logMessage("SIMULATION", "Cargo package safely landed at " + activePath.get(activePath.size() - 1) + ".");
                }

                int currentSegment = (int) animationProgress;
                double segmentProgress = animationProgress - currentSegment;

                if (currentSegment < activePath.size() - 1) {
                    Point p1 = currentPositions.get(activePath.get(currentSegment));
                    Point p2 = currentPositions.get(activePath.get(currentSegment + 1));
                    
                    if (p1 != null && p2 != null) {
                        // Interpolate coordinates
                        int cargoX = (int) (p1.x + (p2.x - p1.x) * segmentProgress);
                        int cargoY = (int) (p1.y + (p2.y - p1.y) * segmentProgress);

                        // Draw sliding packages box (Antigravity sphere glow)
                        int cargoRadius = 8;
                        // Glowing trail
                        g2.setColor(new Color(57, 255, 20, 100));
                        g2.fillOval(cargoX - cargoRadius - 3, cargoY - cargoRadius - 3, (cargoRadius + 3) * 2, (cargoRadius + 3) * 2);
                        // Core package box
                        g2.setColor(COLOR_NEON_GREEN);
                        g2.fillRect(cargoX - 6, cargoY - 6, 12, 12);
                        g2.setColor(COLOR_BG_DARK);
                        g2.drawRect(cargoX - 6, cargoY - 6, 12, 12);
                    }
                }
            }
        }
    }

    // 4. AVL Tree visualizer panel
    private class AVLVisualPanel extends JPanel {
        private String highlightNeighborhood = null;
        private int searchPulseTimer = 0;

        AVLVisualPanel() {
            setOpaque(false);
            setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(COLOR_MUTED), "DATABASE DIRECTORY SCHEMA", 
                    0, 0, FONT_DIGITAL, COLOR_CYAN));
        }

        public void highlightNode(String neighborhood) {
            this.highlightNeighborhood = neighborhood;
            this.searchPulseTimer = 40; // Pulsate 40 frames
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Background Card
            g2.setColor(COLOR_CARD_BG);
            g2.fillRoundRect(5, 15, getWidth() - 10, getHeight() - 20, 10, 10);
            g2.setColor(COLOR_MUTED);
            g2.drawRoundRect(5, 15, getWidth() - 10, getHeight() - 20, 10, 10);

            AddressDirectoryAVL.Node root = addressDirectory.getRoot();
            if (root == null) {
                g2.setColor(COLOR_MUTED);
                g2.setFont(FONT_SUBTITLE);
                g2.drawString("AVL ADRESS REGISTRY EMPTY. REGISTER CARGO TO FILL.", getWidth() / 2 - 170, getHeight() / 2 + 5);
                return;
            }

            // Layout tree parameters
            int startX = getWidth() / 2;
            int startY = 55;
            int hSpacing = getWidth() / 5; // Initial spacing factor
            
            drawAVLNode(g2, root, startX, startY, hSpacing);

            if (searchPulseTimer > 0) {
                searchPulseTimer--;
            }
        }

        private void drawAVLNode(Graphics2D g2, AddressDirectoryAVL.Node node, int x, int y, int hSpacing) {
            if (node == null) return;

            int nodeRadius = 18;
            boolean isHighlighted = (highlightNeighborhood != null && highlightNeighborhood.equalsIgnoreCase(node.neighborhood));

            // Draw branch lines first so they sit below nodes
            g2.setStroke(new BasicStroke(1.2f));
            if (node.left != null) {
                int childX = x - hSpacing;
                int childY = y + 55;
                g2.setColor(COLOR_MUTED);
                g2.drawLine(x, y + nodeRadius, childX, childY - nodeRadius);
                drawAVLNode(g2, node.left, childX, childY, hSpacing / 2);
            }

            if (node.right != null) {
                int childX = x + hSpacing;
                int childY = y + 55;
                g2.setColor(COLOR_MUTED);
                g2.drawLine(x, y + nodeRadius, childX, childY - nodeRadius);
                drawAVLNode(g2, node.right, childX, childY, hSpacing / 2);
            }

            // Draw Node circle fill
            if (isHighlighted) {
                // Pulse halo
                int pulseSize = nodeRadius + (searchPulseTimer % 8);
                g2.setColor(new Color(57, 255, 20, 30));
                g2.fillOval(x - pulseSize, y - pulseSize, pulseSize * 2, pulseSize * 2);
                
                g2.setColor(new Color(57, 255, 20, 50));
                g2.fillOval(x - nodeRadius, y - nodeRadius, nodeRadius * 2, nodeRadius * 2);
                g2.setColor(COLOR_NEON_GREEN);
            } else {
                g2.setColor(new Color(12, 12, 16));
                g2.fillOval(x - nodeRadius, y - nodeRadius, nodeRadius * 2, nodeRadius * 2);
                g2.setColor(COLOR_CYAN);
            }

            // Node border
            g2.drawOval(x - nodeRadius, y - nodeRadius, nodeRadius * 2, nodeRadius * 2);

            // Height and Node labels inside/around the sphere
            g2.setFont(FONT_DIGITAL);
            g2.drawString("H:" + node.height, x - 10, y + 5);

            // Neighborhood Name label below
            g2.setFont(FONT_BODY);
            g2.setColor(isHighlighted ? COLOR_NEON_GREEN : COLOR_TEXT);
            g2.drawString(node.neighborhood, x - 30, y + nodeRadius + 14);

            // If searched, draw registry details tooltip
            if (isHighlighted) {
                g2.setFont(FONT_DIGITAL);
                g2.setColor(COLOR_NEON_GREEN);
                g2.drawString("IDs: " + node.customerIDs, x - 40, y - nodeRadius - 6);
            }
        }
    }

    // --- CYBERPUNK CUSTOM BUTTON CLASS ---
    private static class NeonButton extends JButton {
        private final Color neonColor;
        private boolean isHovered = false;

        NeonButton(String label, Color color) {
            super(label);
            this.neonColor = color;
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setFont(FONT_DIGITAL);
            setForeground(COLOR_TEXT);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    setForeground(Color.WHITE);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    setForeground(COLOR_TEXT);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Background Fill
            if (isHovered) {
                g2.setColor(new Color(neonColor.getRed(), neonColor.getGreen(), neonColor.getBlue(), 35));
            } else {
                g2.setColor(new Color(15, 15, 20));
            }
            g2.fillRoundRect(2, 2, w - 4, h - 4, 6, 6);

            // Neon Border
            g2.setColor(isHovered ? neonColor : new Color(neonColor.getRed(), neonColor.getGreen(), neonColor.getBlue(), 120));
            g2.setStroke(new BasicStroke(isHovered ? 1.8f : 1.0f));
            g2.drawRoundRect(2, 2, w - 4, h - 4, 6, 6);

            // Corner Cyberpunk notch detailing
            g2.setColor(neonColor);
            g2.fillRect(2, 2, 4, 2);
            g2.fillRect(2, 2, 2, 4);
            g2.fillRect(w - 6, h - 4, 4, 2);
            g2.fillRect(w - 4, h - 6, 2, 4);

            // Draw String centered
            FontMetrics fm = g2.getFontMetrics();
            int strW = fm.stringWidth(getText());
            int strH = fm.getAscent();
            g2.drawString(getText(), (w - strW) / 2, (h + strH) / 2 - 2);
        }
    }

    // --- APPLICATION BOOT ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainApp app = new MainApp();
            app.setVisible(true);
        });
    }
}
