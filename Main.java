import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        
        // --- 1. INITIALIZE DATA STRUCTURES ---
        MasterRegistrySLL masterLog = new MasterRegistrySLL();
        IntakeBufferDLL intakeBuffer = new IntakeBufferDLL();
        StandardDeliveryQueue deliveryQueue = new StandardDeliveryQueue();
        TruckLoadingStack truckStack = new TruckLoadingStack();
        AddressDirectoryAVL addressDirectory = new AddressDirectoryAVL();
        CityGraph cityMap = new CityGraph();

        // --- 2. AUTOMATIC INITIAL LOADING ---
        loadMapData(cityMap);
        loadPackageData(masterLog, intakeBuffer, addressDirectory);

        // --- 3. INTERACTIVE MENU ---
        while (true) {
            System.out.println("\n===============================================");
            System.out.println("   ENCODERS LOGISTICS & DISTRIBUTION SYSTEM");
            System.out.println("   'Operational Control Panel'");
            System.out.println("===============================================");
            System.out.println("1. Add New Package (Manual Entry)");
            System.out.println("2. Process Buffer (Move from DLL to Queue)");
            System.out.println("3. Load Truck (Move from Queue to Stack)");
            System.out.println("4. Show Audit Logs (Master Registry SLL)");
            System.out.println("5. Find Shortest Delivery Route (Dijkstra)");
            System.out.println("6. Show Infrastructure Network (Prim's MST)");
            System.out.println("7. Search Customer by Neighborhood (AVL Tree)");
            System.out.println("0. Exit System");
            System.out.print("\nSelect Operation: ");

            int choice;
            try {
                choice = Integer.parseInt(input.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.print("Enter Package ID (e.g., PKG101): ");
                    String id = input.nextLine();
                    System.out.print("Enter Destination (e.g., Talas): ");
                    String dest = input.nextLine();
                    Package newP = new Package(id, dest);
                    
                    masterLog.addRecord(newP);      // Part A: SLL
                    intakeBuffer.insertAtTail(newP); // Part A: DLL
                    addressDirectory.insert(dest, "ID_" + id); // Part B: AVL
                    System.out.println("--> Package registered successfully in SLL, DLL, and AVL.");
                    break;

                case 2:
                    // Removes from DLL head and moves to FIFO Queue
                    Package pFromBuffer = intakeBuffer.removeFromHead();
                    if (pFromBuffer != null) {
                        deliveryQueue.enqueue(pFromBuffer);
                        System.out.println("--> Moved " + pFromBuffer + " from DLL to Delivery Queue (FIFO).");
                    } else {
                        System.out.println("--> Intake Buffer is currently empty!");
                    }
                    break;

                case 3:
                    // Dequeues from Queue and pushes to LIFO Stack
                    Package pFromQueue = deliveryQueue.dequeue();
                    if (pFromQueue != null) {
                        truckStack.push(pFromQueue);
                        System.out.println("--> Pushed " + pFromQueue + " into Truck Stack (LIFO).");
                    } else {
                        System.out.println("--> Delivery Queue is empty!");
                    }
                    break;

                case 4:
                    masterLog.displayLog();
                    break;

                case 5:
                    System.out.print("Enter Start Point: ");
                    String start = input.nextLine();
                    System.out.print("Enter End Point: ");
                    String end = input.nextLine();
                    cityMap.calculateShortestPath(start, end);
                    break;

                case 6:
                    cityMap.calculateMST();
                    break;

                case 7:
                    System.out.print("Enter Neighborhood to Search: ");
                    String nh = input.nextLine();
                    String result = addressDirectory.search(nh);
                    System.out.println("Search Result: " + result);
                    break;

                case 0:
                    System.out.println("Shutting down... Goodbye!");
                    return;

                default:
                    System.out.println("Invalid selection. Please try again.");
            }
        }
    }

    // --- HELPER METHODS FOR LOADING DATA ---
    private static void loadMapData(CityGraph graph) {
        try (Scanner sc = new Scanner(new File("mapData.txt"))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.startsWith("#") || line.isEmpty()) continue;
                String[] p = line.split("\\s+");
                if (p.length >= 3) {
                    graph.addEdge(p[0], p[1], Integer.parseInt(p[2]));
                }
            }
            System.out.println("--> City Map Infrastructure Loaded.");
        } catch (Exception e) {
            System.out.println("Error loading map data: " + e.getMessage());
        }
    }

    private static void loadPackageData(MasterRegistrySLL sll, IntakeBufferDLL dll, AddressDirectoryAVL avl) {
        try (Scanner sc = new Scanner(new File("packageData.txt"))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.startsWith("#") || line.isEmpty() || !line.contains(" ")) continue;
                String[] p = line.split("\\s+");
                if (p.length >= 2) {
                    Package pkg = new Package(p[0], p[1]);
                    sll.addRecord(pkg);
                    dll.insertAtTail(pkg);
                    avl.insert(p[1], "ID_" + p[0]);
                }
            }
            System.out.println("--> Daily Package Records Loaded.");
        } catch (Exception e) {
            System.out.println("Error loading package data: " + e.getMessage());
        }
    }
}