package de.mariushubatschek.is.scheduling.modeling;

public class Edge {

    public Vertex first;
    public Vertex second;
    public EdgeType edgeType;

    @Override
    public String toString() {
        return "Edge{" + "first=" + first + ", second=" + second + ", edgeType=" + edgeType + '}';
    }
}
