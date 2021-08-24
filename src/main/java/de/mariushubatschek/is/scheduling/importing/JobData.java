package de.mariushubatschek.is.scheduling.importing;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class JobData implements Serializable
{

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("operations")
    @Expose
    private List<OperationData> operations = null;
    private final static long serialVersionUID = 325852060089758655L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<OperationData> getOperations() {
        return operations;
    }

    public void setOperations(List<OperationData> operations) {
        this.operations = operations;
    }

    @Override
    public String toString() {
        return "JobData{" +
                "id=" + id +
                ", operations=" + operations +
                '}';
    }
}