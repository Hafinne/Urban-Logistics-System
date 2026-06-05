public class AddressDirectoryAVL {
    class Node {
        String neighborhood;
        String customerIDs; // Multiple customers can be in the same neighborhood
        int height;
        Node left, right;

        Node(String neighborhood, String customerID) {
            this.neighborhood = neighborhood;
            this.customerIDs = customerID;
            this.height = 1;
        }
    }

    private Node root;

    public Node getRoot() { return root; }

    // --- HELPER FUNCTIONS ---
    private int height(Node N) {
        if (N == null) return 0;
        return N.height;
    }

    private int getBalanceFactor(Node N) {
        if (N == null) return 0;
        return height(N.left) - height(N.right);
    }

    // Right Rotation
    private Node rotateRight(Node y) {
        Node x = y.left;
        Node T2 = x.right;

        x.right = y;
        y.left = T2;

        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;

        return x;
    }

    // Left Rotation
    private Node rotateLeft(Node x) {
        Node y = x.right;
        Node T2 = y.left;

        y.left = x;
        x.right = T2;

        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;

        return y;
    }

    // Balancing Function
    private Node balance(Node node) {
        int balanceFactor = getBalanceFactor(node);

        // Left Heavy
        if (balanceFactor > 1) {
            if (getBalanceFactor(node.left) < 0) {
                node.left = rotateLeft(node.left); // Left-Right Case
            }
            return rotateRight(node); // Left-Left Case
        }
        
        // Right Heavy
        if (balanceFactor < -1) {
            if (getBalanceFactor(node.right) > 0) {
                node.right = rotateRight(node.right); // Right-Left Case
            }
            return rotateLeft(node); // Right-Right Case
        }

        return node; // Already balanced
    }

    // --- CORE FUNCTIONS ---

    // Insert new address
    public void insert(String neighborhood, String customerID) {
        root = insertRec(root, neighborhood, customerID);
    }

    private Node insertRec(Node node, String neighborhood, String customerID) {
        // 1. Normal BST Insertion
        if (node == null)
            return new Node(neighborhood, customerID);

        int cmp = neighborhood.compareToIgnoreCase(node.neighborhood);
        
        if (cmp < 0)
            node.left = insertRec(node.left, neighborhood, customerID);
        else if (cmp > 0)
            node.right = insertRec(node.right, neighborhood, customerID);
        else {
            // If neighborhood already exists, append the customerID with a comma
            node.customerIDs = node.customerIDs + ", " + customerID;
            return node;
        }

        // 2. Update Height
        node.height = 1 + Math.max(height(node.left), height(node.right));

        // 3. Balance and Return
        return balance(node);
    }

    // Search for Address/Neighborhood
    public String search(String neighborhood) {
        Node result = searchRec(root, neighborhood);
        if (result != null) {
            return result.customerIDs;
        }
        return "Address not found.";
    }

    private Node searchRec(Node node, String neighborhood) {
        if (node == null) return null;

        int cmp = neighborhood.compareToIgnoreCase(node.neighborhood);
        
        if (cmp == 0) return node; // Found
        
        if (cmp < 0) return searchRec(node.left, neighborhood);
        return searchRec(node.right, neighborhood);
    }
}