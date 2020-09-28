package riddles;

import core.SearchTree;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

public class TimeTravelRiddle {

    //https://www.youtube.com/watch?v=ukUPojrPFPA&list=PLJicmE8fK0EiFRt1Hm5a_7SJFaikIFW30&index=33
    /*
     * Graph problem
     * infinite options -> partially computable
     * Only one solution
     *
     * 1. The graph is fully connected.
     * 2. The graph is bi-colored.
     * 3. The color of an edge is random.
     * 4. Th graph needs to have a circle of size 3 were every edge has the same color
     *
     * 1. Generate fully connected graph with k vertices
     * 2. Create every possible coloring
     * 3. If every coloring yields a same-colored-three-circle return Solution
     */

    public static void main(String[] args) {
        // Number of vertices
        Set<Node> graph = new HashSet<>();
        addNextNode(graph, 0);
        for (long k = 0; k <= 1000; ++k) {
            if (tryEveryColoring(graph)) {
                break;
            }
            addNextNode(graph, k + 1);
        }
        System.out.println("No Solution found");
    }

    private static void addNextNode(Set<Node> graph, long k) {
        Node newNode = new Node(k);
        for (Node node : graph) {
            Edge e = new Edge(newNode, node);
            newNode.edges.add(e);
            node.edges.add(e);
        }
        graph.add(newNode);
    }

    private static boolean tryEveryColoring(Set<Node> graph) {
        Set<Node> currGraph = new HashSet<>(graph);
        Set<Edge> edges = new HashSet<>();
        for (Node node : graph) {
            edges.addAll(node.edges);
        }
        SearchTree<Edge, Color> searchTree = new SearchTree<>(
            edges,
            new ArrayDeque<>(Arrays.asList(Color.values())),
            new ArrayList<>());// no constraints

        long triedColoring = 0;
        System.out.println(
            "Nodes = " + graph.size() + ", Edges: " + edges.size() + ", Testing max: " + Math
                .pow(2.0, edges.size()));
        while (searchTree.hasNext()) {
            var sol = searchTree.testNext();
            if (sol.isPresent()) {
                triedColoring++;
                var coloredGraph = colorGraph(currGraph, sol.get());
                if (!testColoring(coloredGraph)) {
                    System.out.println("Tried " + triedColoring + " colorings before false.");
                    return false;
                }
            }
        }
        System.out.println("Tried " + triedColoring + " all true.");
        return triedColoring > 0;
    }

    private static boolean testColoring(Set<Node> coloredGraph) {
        for (Node node : coloredGraph) {
            boolean foundTriangle;
            for (Edge edge : node.edges) {
                Node end = edge.x.equals(node) ? edge.y : edge.x;
                foundTriangle =
                    searchRecursiveSameColorTriangle(edge.color, node, new HashSet<>(), end, 1);
                if (foundTriangle) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean searchRecursiveSameColorTriangle(
        final Color currColor, final Node start,
        final Set<Node> visited, final Node curr, long lengthOfPath) {
        for (Edge e : curr.edges) {
            Node end = e.x.equals(curr) ? e.y : e.x;
            assert e.color != null;
            if (e.color == currColor) {
                if (end.equals(start)) {
                    if (lengthOfPath + 1 == 3) { // +1 for current path
                        return true;
                    }
                } else if (!visited.contains(end) && lengthOfPath + 1 < 3) {
                    Set<Node> visitedNext = new HashSet<>(visited);
                    visitedNext.add(curr);
                    boolean pathHadTriangle = searchRecursiveSameColorTriangle(
                        currColor, start, visitedNext, end, lengthOfPath + 1);
                    if (pathHadTriangle) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static Set<Node> colorGraph(Set<Node> currGraph, Map<Edge, Color> edgeColorMap) {
        Map<Node, Node> map = new HashMap<>();
        currGraph.forEach(n -> map.put(n, new Node(n.id)));
        Set<Edge> coloredEdges = new HashSet<>();
        for (Entry<Edge, Color> entry : edgeColorMap.entrySet()) {
            coloredEdges.add(new Edge(
                map.get(entry.getKey().x),
                map.get(entry.getKey().y),
                entry.getValue()));
        }
        for (Edge e : coloredEdges) {
            map.get(e.x).edges.add(e);
            map.get(e.y).edges.add(e);
        }
        return new HashSet<>(map.values());
    }

    private enum Color {
        RED,
        BLUE
    }

    private static class Edge {

        final Node x;
        final Node y;
        Color color;

        public Edge(final Node x, final Node y) {
            this.x = x;
            this.y = y;
        }

        public Edge(final Node x, final Node y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Edge edge = (Edge) o;
            return x.equals(edge.x) &&
                y.equals(edge.y) &&
                color == edge.color;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, color);
        }

        @Override
        public String toString() {
            return "Edge{" +
                x +
                " <->" +
                y +
                ", color=" + color +
                '}';
        }
    }

    private static class Node {

        long id;
        Set<Edge> edges;

        public Node(long id) {
            this.id = id;
            this.edges = new HashSet<>();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Node node = (Node) o;
            return id == node.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return "Node{" +
                "id=" + id +
                '}';
        }
    }
}
