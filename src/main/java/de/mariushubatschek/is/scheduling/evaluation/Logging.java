package de.mariushubatschek.is.scheduling.evaluation;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Logging {

    public static void log(final List<Integer> data, final String outputName, final Path path) throws IOException {
        Path path1 = path.resolve(outputName + ".csv");
        File file = path1.toFile();
        if (file.exists()) {
            file.delete();
        }
        file.getParentFile().mkdirs();
        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            for (int i = 0; i < data.size(); i++) {
                writer.writeNext(new String[]{String.valueOf(i + 1), String.valueOf(data.get(i))});
            }
        }
    }

}
