package de.mariushubatschek.is.scheduling.modeling.test;

import de.mariushubatschek.is.scheduling.importing.JobData;
import de.mariushubatschek.is.scheduling.importing.OperationData;
import de.mariushubatschek.is.scheduling.modeling.Edge;
import de.mariushubatschek.is.scheduling.modeling.EdgeType;
import de.mariushubatschek.is.scheduling.modeling.Vertex;
import de.mariushubatschek.is.scheduling.modeling.VertexType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Graph2 {

    private List<Vertex> vertices = new ArrayList<>();

    private List<Edge2> edges = new ArrayList<>();

    private List<Integer> conjunctions = new ArrayList<>();
    private List<Integer> disjunctions = new ArrayList<>();

    private int source;
    private int sink;
    private Map<Integer, Deque<Integer>> longestPaths = new HashMap<>();
    private Map<Integer, Integer> longestPathWeights = new HashMap<>();
    private Deque<Integer> sort;

    public Graph2() {
        Vertex sourceVertex = new Vertex(VertexType.SOURCE);
        Vertex sinkVertex = new Vertex(VertexType.SINK);
        source = vertices.size();
        vertices.add(sourceVertex);
        sink = vertices.size();
        vertices.add(sinkVertex);
        //System.out.println("Vertices size: " + vertices.size());
    }

    public Graph2(final Graph2 graph) {
        //We need to clone the vertices
        this.vertices = graph.vertices.stream().map(Vertex::new).collect(Collectors.toList());
        this.edges = graph.edges.stream().map(Edge2::new).collect(Collectors.toList());
        //And set source and sink
        this.source = 0;
        this.sink = 1;

        this.conjunctions = new ArrayList<>(graph.conjunctions);
        this.disjunctions = new ArrayList<>(graph.disjunctions);

        this.longestPathWeights = new HashMap<>(graph.longestPathWeights);
        this.longestPaths = new HashMap<>(graph.longestPaths);
        this.sort = new ArrayDeque<>(graph.sort);
    }

    public void insertJob(final JobData jobData) {
        int previous = -1;
        for (OperationData operationData : jobData.getOperations()) {
            Vertex vertex = new Vertex(jobData.getId(), operationData);
            vertices.add(vertex);
            //System.out.println("Adding vertex: " + vertex);
            //System.out.println("Vertices size now: " + vertices.size());
            Edge2 edge = new Edge2();
            if (operationData.getIndex() == 0) {
                edge.edgeType = EdgeType.FORWARD;
                edge.first = 0;
                edge.second = vertices.size() - 1;
                //System.out.println("Edge forward from " + edge.first + " to " + edge.second);
            } else {
                edge.edgeType = EdgeType.FORWARD;
                edge.first = previous;
                edge.second = vertices.size() - 1;
                //System.out.println("Edge forward from " + edge.first + " to " + edge.second);
            }
            edges.add(edge);
            //System.out.println("Adding edge : " + edge);
            conjunctions.add(edges.size() - 1);
            //System.out.println("Conjunctions.add: " + (edges.size() - 1));
            previous = vertices.size() - 1;
        }
        Edge2 edge = new Edge2();
        edge.edgeType = EdgeType.FORWARD;
        edge.first = previous;
        edge.second = 1;
        //System.out.println("Adding edge: " + edge);
        edges.add(edge);
        conjunctions.add(edges.size() - 1);
    }

    public void insertDisjunctiveConstraints() {
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = i; j < vertices.size(); j++) {
                final Vertex one = vertices.get(i);
                final Vertex other = vertices.get(j);
                // Avoid loop
                if (one.job == other.job && one.index == other.index) {
                    continue;
                }
                if (one.machine == other.machine) {
                    Edge2 edge = new Edge2();
                    edge.edgeType = EdgeType.UNDECIDED;
                    edge.first = i;
                    edge.second = j;
                    edges.add(edge);
                    disjunctions.add(edges.size() - 1);
                }
            }
        }
        calculateInfo();
    }

    private Deque<Integer> sort() {
        Deque<Integer> sort = new ArrayDeque<>();
        Map<Integer, Integer> color = new HashMap<>();
        for (int vertex = 0; vertex < vertices.size(); vertex++) {
            color.put(vertex, 1);
        }
        while (color.values().stream().anyMatch(v -> v < 3)) {
            boolean cycleFound = visit(
                    IntStream.range(0, vertices.size()).filter(integer -> color.get(integer) == 1).findFirst().getAsInt(), sort, color);
            if (cycleFound) {
                return null;
            }
        }
        return sort;
    }

    private boolean visit(final int vertex, final Deque<Integer> sort,
                          final Map<Integer, Integer> color) {
        if (color.get(vertex) == 3) {
            return false;
        }
        if (color.get(vertex) == 2) {
            return true;
        }
        color.put(vertex, 2);
        for (int edge : getSuccessors(vertex)) {
            if (edges.get(edge).edgeType == EdgeType.FORWARD) {
                visit(edges.get(edge).second, sort, color);
            }
            if (edges.get(edge).edgeType == EdgeType.BACKWARD) {
                visit(edges.get(edge).first, sort, color);
            }
        }
        color.put(vertex, 3);
        sort.addFirst(vertex);
        return false;
    }

    /**
     * Get all successors of a node, disregarding undirected edges
     * @param vertex
     * @return
     */
    private List<Integer> getSuccessors(final int vertex) {
        List<Integer> conjunctionSuccessors = conjunctions.stream().filter(v -> edges.get(v).first == vertex)
                .collect(Collectors.toList());
        List<Integer> disjunctionSuccessors = disjunctions.stream().filter(
                v -> (edges.get(v).edgeType == EdgeType.FORWARD && edges.get(v).first == vertex) || (
                        edges.get(v).edgeType == EdgeType.BACKWARD && edges.get(v).second == vertex))
                .collect(Collectors.toList());
        conjunctionSuccessors.addAll(disjunctionSuccessors);
        return conjunctionSuccessors;
    }

    /**
     * Get all predecessors of a node, disregarding undirected edges
     * @param vertex
     * @return
     */
    private List<Integer> getPredecessors(final int vertex) {
        List<Integer> conjunctionPredecessors = conjunctions.stream()
                .filter(v -> edges.get(v).second == vertex).collect(Collectors.toList());
        List<Integer> disjunctionPredecessors = disjunctions.stream().filter(
                v -> (edges.get(v).edgeType == EdgeType.FORWARD && edges.get(v).second == vertex) || (
                        edges.get(v).edgeType == EdgeType.BACKWARD && edges.get(v).first == vertex))
                .collect(Collectors.toList());
        conjunctionPredecessors.addAll(disjunctionPredecessors);
        return conjunctionPredecessors;
    }

    /**
     * Find an initial direction for each undirected edge s.t. this graph is a DAG
     */
    public void findDirections() {
        for (int edge : disjunctions) {
            int idxFirst = 0;
            int idxSecond = 0;
            int i = 0;
            for (int vertex : sort) {
                if (edges.get(edge).first == vertex) {
                    idxFirst = i;
                }
                if (edges.get(edge).second == vertex) {
                    idxSecond = i;
                }
                i++;
            }
            if (idxFirst < idxSecond) {
                edges.get(edge).edgeType = EdgeType.FORWARD;
            } else {
                edges.get(edge).edgeType = EdgeType.BACKWARD;
            }
        }
    }

    public int makespan() {
        return longestPathWeights.get(sink);
    }

    /**
     * Flips the direction of an edge in the disjunctive set on a longest path
     * @return A copy of this graph with an eligible edge flipped
     */
    public List<Graph2> flipEligibleEdge(final Random random) {
        //System.out.println("Flipping edges");
        List<Graph2> newGraphs = new ArrayList<>();
        Deque<Integer> longestPath = longestPaths.get(sink);
        List<Integer> eligibleEdges =
                longestPath.stream().filter(e -> disjunctions.contains(e)).collect(
                        Collectors.toList());
        if (eligibleEdges.isEmpty()) {
            throw new RuntimeException("There are no eligible edges on the longest path!");
        }
        //System.out.println("Computed eligible edges");
        int randomIndex = Math.max(random.nextInt(eligibleEdges.size()), 1);
        Collections.shuffle(eligibleEdges, random);
        for (int i = 0; i < randomIndex; i++) {
            //System.out.println("Before flipping edges");
            Graph2 newGraph = new Graph2(this);
            newGraph.flipEdge(eligibleEdges.get(i));
            newGraphs.add(newGraph);
            //System.out.println("New graph");
        }
        return newGraphs;
    }

    private void flipEdge(final int k) {
        Edge2 chosenEdge = edges.get(k);
        if (chosenEdge.edgeType == EdgeType.FORWARD) {
            chosenEdge.edgeType = EdgeType.BACKWARD;
        } else if (chosenEdge.edgeType == EdgeType.BACKWARD) {
            chosenEdge.edgeType = EdgeType.FORWARD;
        }
        sort = sort();
        calculateLongestPathWeights();
        calculateLongestPaths();
    }

    private void calculateInfo() {
        if (sort == null) {
            sort = sort();
            //System.out.println(sort);
            if (sort == null) {
                // the graph is not a DAG
                throw new RuntimeException("Something is seriously wrong");
            }
            findDirections();
            calculateLongestPathWeights();
            calculateLongestPaths();
        }
    }

    /**
     * Calculate the longest path weight for each node
     */
    private void calculateLongestPathWeights() {
        longestPathWeights.clear();
        for (int v : sort) {
            longestPaths.put(v, new ArrayDeque<>());
            List<Integer> predecessors = getPredecessors(v);
            int max = 0;
            for (int edge : predecessors) {
                int predecessor;
                if (edges.get(edge).edgeType == EdgeType.FORWARD) {
                    predecessor = edges.get(edge).first;
                } else {
                    predecessor = edges.get(edge).second;
                }
                int aLength = longestPathWeights.getOrDefault(predecessor, 0) + vertices.get(v).duration;
                if (aLength > max) {
                    max = aLength;
                }
            }
            longestPathWeights.put(v, max);
        }
    }

    /**
     * Calculate for each node a longest path
     */
    private void calculateLongestPaths() {
        for (int v : sort) { //vertices
            int currentVertex = v;
            while (vertices.get(currentVertex).vertexType != VertexType.SOURCE) {
                List<Integer> predecessors = getPredecessors(currentVertex);
                int greatestWeight = Integer.MIN_VALUE;
                int greatestPredecessor = -1;
                int greatestPredecessorVertex = -1;
                //System.out.println(predecessors);
                for (int predecessor : predecessors) {
                    int weight = 0;
                    int predecessorVertex = -1;
                    if (edges.get(predecessor).edgeType == EdgeType.FORWARD) {
                        weight = longestPathWeights.get(edges.get(predecessor).first);
                        predecessorVertex = edges.get(predecessor).first;
                    }
                    if (edges.get(predecessor).edgeType == EdgeType.BACKWARD) {
                        weight = longestPathWeights.get(edges.get(predecessor).second);
                        predecessorVertex = edges.get(predecessor).second;
                    }
                    if (weight > greatestWeight) {
                        greatestWeight = weight;
                        greatestPredecessor = predecessor;
                        greatestPredecessorVertex = predecessorVertex;
                    }
                }
                //System.out.println("For vertex: " + v);
                //System.out.println("Current vertex: " + currentVertex);
                //System.out.println("Greatest predecessor is: " + greatestPredecessor);
                currentVertex = greatestPredecessorVertex;
                if (greatestPredecessor != -1) {
                    longestPaths.get(v).addFirst(greatestPredecessor);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Graph{" + "vertices=" + vertices + ",\n conjunctions=" + conjunctions
                + ",\n disjunctions=" + disjunctions + ", source=" + source + ", sink=" + sink + '}';
    }

    public void orientByPermutation(List<Integer> permutation) {
        Deque<Integer> sort = new ArrayDeque<>();
        sort.add(source);
        for (Integer permutationIndex : permutation) {
            int index = permutationIndex + 1;
            sort.add(index);
        }
        sort.add(sink);
        this.sort = sort;
        findDirections();
        calculateLongestPathWeights();
        calculateLongestPaths();
    }

    private List<List<Integer>> getPathsFromWithWeight(final int start, final int weight) {
        List<List<Integer>> paths = new ArrayList<>();
        Deque<List<Integer>> pendingPaths = new ArrayDeque<>();
        List<Integer> p = new ArrayList<>();
        p.add(start);
        pendingPaths.add(p);
        while (!pendingPaths.isEmpty()) {
            p = pendingPaths.removeFirst();
            if (p.get(p.size() - 1) == sink) {
                paths.add(p);
            }
            int last = p.get(p.size() - 1);
            for (int edge : getSuccessors(last)) {
                p.add(edge);
                pendingPaths.add(p);
            }
        }
        return paths;
    }

}
