public class MasterRegistrySLL {
    class Node {
        Package pkg;
        Node next;
        Node(Package pkg) { 
            this.pkg = pkg; 
            this.next = null; 
        }
    }
    
    private Node head;

    public Node getHead() { return head; }

    // Appends a package to the end of the log. 
    public void addRecord(Package pkg) { 
        Node newNode = new Node(pkg);
        if (head == null) {
            head = newNode;
        } else {
            Node current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
    }

    // Traverses the SLL to print the daily record. 
    public void displayLog() { 
        System.out.println("--- Master Registry Log ---");
        Node current = head;
        while (current != null) {
            System.out.println(current.pkg);
            current = current.next;
        }
    }
}