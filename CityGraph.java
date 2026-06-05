import java.util.*;

public class CityGraph {
    
    // Helper class to represent edges
    class Edge {
        String target;
        int weight;
        
        Edge(String target, int weight) {
            this.target = target;
            this.weight = weight;
        }
    }

    private Map<String, List<Edge>> adjList;

    public Map<String, List<Edge>> getAdjList() { return adjList; }

    public CityGraph() {
        adjList = new HashMap<>();
    }

    // Builds the city map network
    public void addEdge(String source, String destination, int weight) {
        adjList.putIfAbsent(source, new ArrayList<>());
        adjList.putIfAbsent(destination, new ArrayList<>());
        
        // Assuming roads are bidirectional (undirected graph)
        adjList.get(source).add(new Edge(destination, weight));
        adjList.get(destination).add(new Edge(source, weight));
    }

    // Implementation of Dijkstra's Algorithm
    public void calculateShortestPath(String start, String end) {
        if (!adjList.containsKey(start) || !adjList.containsKey(end)) {
            System.out.println("Error: Start or end location not found in the map.");
            return;
        }

        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.distance));
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        Set<String> visited = new HashSet<>();

        // Initialize distances to infinity
        for (String node : adjList.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
        }

        distances.put(start, 0);
        pq.add(new NodeDistance(start, 0));

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            String currentNode = current.node;

            if (visited.contains(currentNode)) continue;
            visited.add(currentNode);

            if (currentNode.equals(end)) break; // Reached the destination

            for (Edge edge : adjList.get(currentNode)) {
                if (!visited.contains(edge.target)) {
                    int newDist = distances.get(currentNode) + edge.weight;
                    if (newDist < distances.get(edge.target)) {
                        distances.put(edge.target, newDist);
                        previous.put(edge.target, currentNode);
                        pq.add(new NodeDistance(edge.target, newDist));
                    }
                }
            }
        }

        // Reconstruct and print the path
        if (distances.get(end) == Integer.MAX_VALUE) {
            System.out.println("No path found from " + start + " to " + end);
            return;
        }

        List<String> path = new ArrayList<>();
        String curr = end;
        while (curr != null) {
            path.add(curr);
            curr = previous.get(curr);
        }
        Collections.reverse(path);
        
        System.out.println("Shortest path from " + start + " to " + end + ":");
        System.out.println(String.join(" -> ", path) + " | Total Distance: " + distances.get(end) + " KM");
    }

    // Implementation of Prim's Algorithm for Minimum Spanning Tree
    public void calculateMST() {
        if (adjList.isEmpty()) return;

        PriorityQueue<MSTEdge> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e.weight));
        Set<String> visited = new HashSet<>();
        List<MSTEdge> mstEdges = new ArrayList<>();
        int totalWeight = 0;

        // Start from any arbitrary node
        String startNode = adjList.keySet().iterator().next();
        visited.add(startNode);

        for (Edge edge : adjList.get(startNode)) {
            pq.add(new MSTEdge(startNode, edge.target, edge.weight));
        }

        while (!pq.isEmpty() && visited.size() < adjList.size()) {
            MSTEdge currentEdge = pq.poll();

            if (visited.contains(currentEdge.destination)) continue;

            visited.add(currentEdge.destination);
            mstEdges.add(currentEdge);
            totalWeight += currentEdge.weight;

            for (Edge edge : adjList.get(currentEdge.destination)) {
                if (!visited.contains(edge.target)) {
                    pq.add(new MSTEdge(currentEdge.destination, edge.target, edge.weight));
                }
            }
        }

        System.out.println("\n--- Minimum Spanning Tree (City Network Efficiency) ---");
        for (MSTEdge edge : mstEdges) {
            System.out.println(edge.source + " <---> " + edge.destination + " : " + edge.weight + " KM");
        }
        System.out.println("Total Optimal Network Distance: " + totalWeight + " KM");
    }

    // Method to get path as a list of strings
    public List<String> getShortestPathList(String start, String end) {
        if (!adjList.containsKey(start) || !adjList.containsKey(end)) {
            return null;
        }

        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.distance));
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        Set<String> visited = new HashSet<>();

        for (String node : adjList.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
        }

        distances.put(start, 0);
        pq.add(new NodeDistance(start, 0));

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            String currentNode = current.node;

            if (visited.contains(currentNode)) continue;
            visited.add(currentNode);

            if (currentNode.equals(end)) break;

            for (Edge edge : adjList.get(currentNode)) {
                if (!visited.contains(edge.target)) {
                    int newDist = distances.get(currentNode) + edge.weight;
                    if (newDist < distances.get(edge.target)) {
                        distances.put(edge.target, newDist);
                        previous.put(edge.target, currentNode);
                        pq.add(new NodeDistance(edge.target, newDist));
                    }
                }
            }
        }

        if (distances.get(end) == Integer.MAX_VALUE) {
            return null;
        }

        List<String> path = new ArrayList<>();
        String curr = end;
        while (curr != null) {
            path.add(curr);
            curr = previous.get(curr);
        }
        Collections.reverse(path);
        return path;
    }

    // Method to get MST edges
    public List<MSTEdge> getMSTEdges() {
        List<MSTEdge> mstEdges = new ArrayList<>();
        if (adjList.isEmpty()) return mstEdges;

        PriorityQueue<MSTEdge> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e.weight));
        Set<String> visited = new HashSet<>();

        String startNode = adjList.keySet().iterator().next();
        visited.add(startNode);

        for (Edge edge : adjList.get(startNode)) {
            pq.add(new MSTEdge(startNode, edge.target, edge.weight));
        }

        while (!pq.isEmpty() && visited.size() < adjList.size()) {
            MSTEdge currentEdge = pq.poll();

            if (visited.contains(currentEdge.destination)) continue;

            visited.add(currentEdge.destination);
            mstEdges.add(currentEdge);

            for (Edge edge : adjList.get(currentEdge.destination)) {
                if (!visited.contains(edge.target)) {
                    pq.add(new MSTEdge(currentEdge.destination, edge.target, edge.weight));
                }
            }
        }
        return mstEdges;
    }

    // --- Helper classes for Priority Queues ---
    class NodeDistance {
        String node;
        int distance;
        NodeDistance(String node, int distance) {
            this.node = node;
            this.distance = distance;
        }
    }

    class MSTEdge {
        String source, destination;
        int weight;
        MSTEdge(String source, String destination, int weight) {
            this.source = source;
            this.destination = destination;
            this.weight = weight;
        }
    }
}