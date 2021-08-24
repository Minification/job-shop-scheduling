package de.mariushubatschek.is.scheduling.modeling;

import de.mariushubatschek.is.scheduling.importing.OperationData;

public class Vertex {

    public int job;
    public int machine;
    public int duration;
    public int index;
    public VertexType vertexType;

    public Vertex(final VertexType vertexType) {
        this.vertexType = vertexType;
        this.job = -1;
        this.machine = -1;
        this.duration = 0;
        this.index = -1;
    }

    public Vertex(final int job, final OperationData operationData) {
        this.vertexType = VertexType.OPERATION;
        this.machine = operationData.getResource();
        this.duration = operationData.getDuration();
        this.index = operationData.getIndex();
        this.job = job;
    }

    public Vertex(final Vertex vertex) {
        this.vertexType = vertex.vertexType;
        this.machine = vertex.machine;
        this.duration = vertex.duration;
        this.index = vertex.index;
        this.job = vertex.job;
    }

    @Override
    public String toString() {
        return "Vertex{" + "job=" + job + ", machine=" + machine + ", duration=" + duration
            + ", index=" + index + ", vertexType=" + vertexType + '}';
    }
}
