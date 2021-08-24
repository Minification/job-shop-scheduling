package de.mariushubatschek.is.scheduling.modeling;

import de.mariushubatschek.is.scheduling.importing.OperationData;
import java.util.Spliterator.OfPrimitive;

public class Operation {

    private int jobIndex;

    private int index;

    private int duration;

    private int startTime;

    private int endTime;

    private int resource;

    private Operation previousOperation;

    public Operation() {

    }

    public int getResource() {
        return resource;
    }

    public Operation getPreviousOperation() {
        return previousOperation;
    }

    public Operation(final OperationData operationData, final Operation previousOperation) {
        this.index = operationData.getIndex();
        this.resource = operationData.getResource();
        this.duration = operationData.getDuration();
        this.previousOperation = previousOperation;
    }

    public void setJobIndex(final int jobIndex) {
        this.jobIndex = jobIndex;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public void setDuration(final int duration) {
        this.duration = duration;
    }

    public void setStartTime(final int startTime) {
        this.startTime = startTime;
        // Interval [startTime, endTime] has duration b - a + 1, since a and b are zero-based
        // indices
        this.endTime = startTime + duration - 1;
        if(this.endTime - this.startTime + 1 != duration) {
            throw new RuntimeException();
        }
    }

    public int getStartTime() {
        return startTime;
    }

    public int getJobIndex() {
        return jobIndex;
    }

    public int getIndex() {
        return index;
    }

    public int getEndTime() {
        return endTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setPreviousOperation(final Operation previousOperation) {
        this.previousOperation = previousOperation;
    }

    public void setResource(final int resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "Operation{" + "jobIndex=" + jobIndex + ", index=" + index + ", resourceIndex" + resource + ", duration=" + duration
            + ", interval=[" + startTime + "," + endTime +  "], previousIndex=" + ((previousOperation != null) ? previousOperation.getIndex() : null) +
        "}";
    }
}
