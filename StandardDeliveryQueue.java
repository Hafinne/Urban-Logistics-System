public class StandardDeliveryQueue {
    class Node {
        Package pkg;
        Node next;
        Node(Package pkg) { this.pkg = pkg; }
    }
    
    private Node front, rear;

    public Node getFront() { return front; }
    public Node getRear() { return rear; }

    // adds to the back of the line 
    public void enqueue(Package pkg) { 
        Node newNode = new Node(pkg);
        if (rear == null) {
            front = rear = newNode;
            return;
        }
        rear.next = newNode;
        rear = newNode;
    }

    // removes from the front 
    public Package dequeue() { 
        if (front == null) return null;
        
        Package removedPkg = front.pkg;
        front = front.next;
        
        if (front == null) rear = null;
        return removedPkg;
    }
}