public class IntakeBufferDLL {
    class Node {
        Package pkg;
        Node next, prev;
        Node(Package pkg) { this.pkg = pkg; }
    }
    
    private Node head, tail;

    public Node getHead() { return head; }
    public Node getTail() { return tail; }

    // Quickly add a newly arrived package to the end of the buffer using the tail pointer. 
    public void insertAtTail(Package pkg) { 
        Node newNode = new Node(pkg);
        if (tail == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
    }

    // Removes a package from the buffer so it can be moved to the dispatch preparation area. 
    public Package removeFromHead() { 
        if (head == null) return null;
        
        Package removedPkg = head.pkg;
        head = head.next;
        
        if (head != null) {
            head.prev = null;
        } else {
            tail = null;
        }
        return removedPkg;
    }
}