package de.mariushubatschek.is.scheduling.importing;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProblemData implements Serializable
{

    @SerializedName("resources")
    @Expose
    private List<ResourceData> resources = null;
    @SerializedName("jobs")
    @Expose
    private List<JobData> jobs = null;
    private final static long serialVersionUID = 6693234676539216606L;

    public List<ResourceData> getResources() {
        return resources;
    }

    public void setResources(List<ResourceData> resources) {
        this.resources = resources;
    }

    public List<JobData> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobData> jobs) {
        this.jobs = jobs;
    }

    @Override
    public String toString() {
        return "ProblemData{" +
                "resources=" + resources +
                ", jobs=" + jobs +
                '}';
    }
}