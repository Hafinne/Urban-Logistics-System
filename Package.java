public class Package {
    String packageID;
    String destination;

    public Package(String packageID, String destination) {
        this.packageID = packageID;
        this.destination = destination;
    }

    @Override
    public String toString() {
        return "[" + packageID + " -> " + destination + "]";
    }
}