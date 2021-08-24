package de.mariushubatschek.is.scheduling.modeling.test;

import de.mariushubatschek.is.scheduling.modeling.EdgeType;

public class Edge2 {

    public int first;
    public int second;
    public EdgeType edgeType;

    public Edge2() {

    }

    public Edge2(final Edge2 edge2) {
        this.first = edge2.first;
        this.second = edge2.second;
        this.edgeType = edge2.edgeType;
    }

    @Override
    public String toString() {
        return "Edge{" + "first=" + first + ", second=" + second + ", edgeType=" + edgeType + '}';
    }

}
