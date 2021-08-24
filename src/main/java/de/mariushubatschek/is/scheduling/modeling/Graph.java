package de.mariushubatschek.is.scheduling.modeling;

import de.mariushubatschek.is.scheduling.importing.JobData;
import de.mariushubatschek.is.scheduling.importing.OperationData;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class Graph {

    private List<Vertex> vertices = new ArrayList<>(
        Arrays.asList(new Vertex(VertexType.SOURCE), new Vertex(VertexType.SINK)));
    private List<Edge> conjunctions = new ArrayList<>();
    private List<Edge> disjunctions = new ArrayList<>();
    private Vertex source;
    private Vertex sink;
    private Map<Vertex, Deque<Edge>> longestPaths = new HashMap<>();
    private Map<Vertex, Integer> longestPathWeights = new HashMap<>();
    private Deque<Vertex> sort;

    public Graph() {
        source = vertices.stream().filter(v -> v.vertexType == VertexType.SOURCE).findFirst().get();
        sink = vertices.stream().filter(v -> v.vertexType == VertexType.SINK).findFirst().get();
    }

    public Graph(final Graph graph) {
        //We need to clone the vertices
        this.vertices = graph.vertices.stream().map(Vertex::new).collect(Collectors.toList());
        //And set source and sink
        this.source =
            this.vertices.stream().filter(v -> v.vertexType == VertexType.SOURCE).findFirst().get();
        this.sink =
            this.vertices.stream().filter(v -> v.vertexType == VertexType.SINK).findFirst().get();
        //Deep clone conjunctive arcs
        for (Edge otherEdge : graph.conjunctions) {
            Edge edge = new Edge();
            edge.edgeType = otherEdge.edgeType;
            edge.first =
                vertices.stream()
                    .filter(v -> v.job == otherEdge.first.job && v.index == otherEdge.first.index && v.vertexType == otherEdge.first.vertexType)
                    .findFirst()
                    .get();
            edge.second =
                vertices.stream()
                    .filter(v -> v.job == otherEdge.second.job && v.index == otherEdge.second.index && v.vertexType == otherEdge.second.vertexType)
                    .findFirst()
                    .get();
            conjunctions.add(edge);
        }
        //Deep clone disjunctive arcs
        for (Edge otherEdge : graph.disjunctions) {
            Edge edge = new Edge();
            edge.edgeType = otherEdge.edgeType;
            edge.first =
                vertices.stream()
                    .filter(v -> v.job == otherEdge.first.job && v.index == otherEdge.first.index && v.vertexType == otherEdge.first.vertexType)
                    .findFirst()
                    .get();
            edge.second =
                vertices.stream()
                    .filter(v -> v.job == otherEdge.second.job && v.index == otherEdge.second.index && v.vertexType == otherEdge.second.vertexType)
                    .findFirst()
                    .get();
            disjunctions.add(edge);
        }
        //And calculate info
        calculateInfo();
        //System.out.println(longestPaths);
    }

    public void insertJob(final JobData jobData) {
        Vertex previous = null;
        for (OperationData operationData : jobData.getOperations()) {
            Vertex vertex = new Vertex(jobData.getId(), operationData);
            //System.out.println("Adding vertex: " + vertex);
            vertices.add(vertex);
            Edge edge = new Edge();
            if (operationData.getIndex() == 0) {
                edge.edgeType = EdgeType.FORWARD;
                edge.first = source;
                edge.second = vertex;
            } else {
                edge.edgeType = EdgeType.FORWARD;
                edge.first = previous;
                edge.second = vertex;
            }
            //System.out.println("Adding edge : " + edge);
            conjunctions.add(edge);
            previous = vertex;
        }
        Edge edge = new Edge();
        edge.edgeType = EdgeType.FORWARD;
        edge.first = previous;
        edge.second = sink;
        //System.out.println("Adding edge: " + edge);
        conjunctions.add(edge);
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
                    Edge edge = new Edge();
                    edge.edgeType = EdgeType.UNDECIDED;
                    edge.first = one;
                    edge.second = other;
                    disjunctions.add(edge);
                }
            }
        }
        calculateInfo();
    }

    private Deque<Vertex> sort() {
        Deque<Vertex> sort = new ArrayDeque<>();
        Map<Vertex, Integer> color = new HashMap<>();
        for (Vertex vertex : vertices) {
            color.put(vertex, 1);
        }
        while (color.values().stream().anyMatch(v -> v < 3)) {
            boolean cycleFound = visit(
                color.keySet().stream().filter(integer -> color.get(integer) == 1).findFirst()
                    .get(), sort, color);
            if (cycleFound) {
                return null;
            }
        }
        return sort;
    }

    private boolean visit(final Vertex vertex, final Deque<Vertex> sort,
        final Map<Vertex, Integer> color) {
        if (color.get(vertex) == 3) {
            return false;
        }
        if (color.get(vertex) == 2) {
            return true;
        }
        color.put(vertex, 2);
        for (Edge edge : getSuccessors(vertex)) {
            if (edge.edgeType == EdgeType.FORWARD) {
                visit(edge.second, sort, color);
            }
            if (edge.edgeType == EdgeType.BACKWARD) {
                visit(edge.first, sort, color);
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
    private List<Edge> getSuccessors(final Vertex vertex) {
        List<Edge> conjunctionSuccessors = conjunctions.stream().filter(v -> v.first.equals(vertex))
            .collect(Collectors.toList());
        List<Edge> disjunctionSuccessors = disjunctions.stream().filter(
            v -> (v.edgeType == EdgeType.FORWARD && v.first.equals(vertex)) || (
                v.edgeType == EdgeType.BACKWARD && v.second.equals(vertex)))
            .collect(Collectors.toList());
        conjunctionSuccessors.addAll(disjunctionSuccessors);
        return conjunctionSuccessors;
    }

    /**
     * Get all predecessors of a node, disregarding undirected edges
     * @param vertex
     * @return
     */
    private List<Edge> getPredecessors(final Vertex vertex) {
        List<Edge> conjunctionPredecessors = conjunctions.stream()
            .filter(v -> v.second.equals(vertex)).collect(Collectors.toList());
        List<Edge> disjunctionPredecessors = disjunctions.stream().filter(
            v -> (v.edgeType == EdgeType.FORWARD && v.second.equals(vertex)) || (
                v.edgeType == EdgeType.BACKWARD && v.first.equals(vertex)))
            .collect(Collectors.toList());
        conjunctionPredecessors.addAll(disjunctionPredecessors);
        return conjunctionPredecessors;
    }

    /**
     * Find an initial direction for each undirected edge s.t. this graph is a DAG
     */
    public void findDirections() {
        for (Edge edge : disjunctions) {
            int idxFirst = 0;
            int idxSecond = 0;
            int i = 0;
            for (Vertex vertex : sort) {
                if (edge.first.equals(vertex)) {
                    idxFirst = i;
                }
                if (edge.second.equals(vertex)) {
                    idxSecond = i;
                }
                i++;
            }
            if (idxFirst < idxSecond) {
                edge.edgeType = EdgeType.FORWARD;
            } else {
                edge.edgeType = EdgeType.BACKWARD;
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
    public Graph flipEligibleEdge(final Random random) {
        Graph newGraph = new Graph(this);
        Deque<Edge> longestPath = newGraph.longestPaths.get(newGraph.sink);
        List<Edge> eligibleEdges =
            longestPath.stream().filter(e -> newGraph.disjunctions.contains(e)).collect(
            Collectors.toList());
        if (eligibleEdges.isEmpty()) {
            throw new RuntimeException("There are no eligible edges on the longest path!");
        }
        int randomIndex = random.nextInt(eligibleEdges.size());
        Edge chosenEdge = eligibleEdges.get(randomIndex);
        if (chosenEdge.edgeType == EdgeType.FORWARD) {
            chosenEdge.edgeType = EdgeType.BACKWARD;
        } else if (chosenEdge.edgeType == EdgeType.BACKWARD) {
            chosenEdge.edgeType = EdgeType.FORWARD;
        }
        newGraph.sort = newGraph.sort();
        newGraph.calculateLongestPathWeights();
        newGraph.calculateLongestPaths();
        return newGraph;
    }

    private void calculateInfo() {
        if (sort == null) {
            sort = sort();
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
        for (Vertex v : sort) {
            longestPaths.put(v, new ArrayDeque<>());
            List<Edge> predecessors = getPredecessors(v);
            int max = 0;
            for (Edge edge : predecessors) {
                Vertex predecessor;
                if (edge.edgeType == EdgeType.FORWARD) {
                    predecessor = edge.first;
                } else {
                    predecessor = edge.second;
                }
                int aLength = longestPathWeights.getOrDefault(predecessor, 0) + v.duration;
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
        //System.out.println("sort: " + sort);
        for (Vertex v : sort) { //vertices
            //System.out.println("Vertex " + v);
            Vertex currentVertex = v;
            while (currentVertex.vertexType != VertexType.SOURCE) {
                //System.out.println("Current vertex: " + currentVertex);
                List<Edge> predecessors = getPredecessors(currentVertex);
                //System.out.println("Done getting preds");
                int greatestWeight = Integer.MIN_VALUE;
                Edge greatestPredecessor = null;
                Vertex greatestPredecessorVertex = null;
                //System.out.println(predecessors);
                for (Edge predecessor : predecessors) {
                    //System.out.println("Pred: " + predecessor);
                    int weight = 0;
                    Vertex predecessorVertex = null;
                    if (predecessor.edgeType == EdgeType.FORWARD) {
                        weight = longestPathWeights.get(predecessor.first);
                        predecessorVertex = predecessor.first;
                    }
                    //System.out.println("Forward check done");
                    if (predecessor.edgeType == EdgeType.BACKWARD) {
                        weight = longestPathWeights.get(predecessor.second);
                        predecessorVertex = predecessor.second;
                    }
                    //System.out.println("Backward check done");
                    if (weight > greatestWeight) {
                        greatestWeight = weight;
                        greatestPredecessor = predecessor;
                        greatestPredecessorVertex = predecessorVertex;
                    }
                    //System.out.println("pred loop done");
                }
                //System.out.println("For vertex: " + v);
                //System.out.println("Current vertex: " + currentVertex);
                //System.out.println("Greatest predecessor is: " + greatestPredecessor);
                currentVertex = greatestPredecessorVertex;
                if (greatestPredecessor != null) {
                    longestPaths.get(v).addFirst(greatestPredecessor);
                }
            }
        }
        //System.out.println("Done getting longest paths");
    }

    @Override
    public String toString() {
        return "Graph{" + "vertices=" + vertices + ",\n conjunctions=" + conjunctions
            + ",\n disjunctions=" + disjunctions + ", source=" + source + ", sink=" + sink + '}';
    }

    public void orientByPermutation(List<Integer> permutation) {
        Deque<Vertex> sort = new ArrayDeque<>();
        sort.add(source);
        for (Integer permutationIndex : permutation) {
            int index = permutationIndex + 1;
            //System.out.println("index: " + index);
            sort.add(vertices.get(index));
        }
        sort.add(sink);
        this.sort = sort;
        for (Edge e : disjunctions) {
            e.edgeType = EdgeType.UNDECIDED;
        }
        //System.out.println("finding directions");
        findDirections();
        //System.out.println("finding longest path weights");
        calculateLongestPathWeights();
        //System.out.println("finding longest paths");
        calculateLongestPaths();
        //System.out.println("done finding things");
    }
}
