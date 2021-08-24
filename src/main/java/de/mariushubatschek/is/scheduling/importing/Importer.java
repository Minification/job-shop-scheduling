package de.mariushubatschek.is.scheduling.importing;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Importer {

    public ProblemData load(final Path path) throws IOException {
        Gson gson = new Gson();
        String jsonString = Files.readString(path, StandardCharsets.UTF_8);
        return gson.fromJson(jsonString, ProblemData.class);
    }

}
