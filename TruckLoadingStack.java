public class TruckLoadingStack {
    class Node {
        Package pkg;
        Node next;
        Node(Package pkg) { this.pkg = pkg; }
    }
    
    private Node top;

    public Node getTop() { return top; }

    // loads a package into the van
    public void push(Package pkg) { 
        Node newNode = new Node(pkg);
        newNode.next = top;
        top = newNode;
    }

    // unloads the most recently loaded package 
    public Package pop() { 
        if (top == null) return null;
        
        Package removedPkg = top.pkg;
        top = top.next;
        return removedPkg;
    }
}