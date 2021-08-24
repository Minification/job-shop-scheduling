package de.mariushubatschek.is.scheduling.importing;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResourceData implements Serializable
{

    @SerializedName("id")
    @Expose
    private Integer id;
    private final static long serialVersionUID = -2060267918163771083L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ResourceData{" +
                "id=" + id +
                '}';
    }
}