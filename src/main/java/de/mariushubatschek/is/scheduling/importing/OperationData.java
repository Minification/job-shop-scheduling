package de.mariushubatschek.is.scheduling.importing;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OperationData implements Serializable
{

    @SerializedName("index")
    @Expose
    private Integer index;
    @SerializedName("duration")
    @Expose
    private Integer duration;
    @SerializedName("resource")
    @Expose
    private Integer resource;
    private final static long serialVersionUID = -1488823648590162916L;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getResource() {
        return resource;
    }

    public void setResource(Integer resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "OperationData{" +
                "index=" + index +
                ", duration=" + duration +
                ", resource=" + resource +
                '}';
    }
}