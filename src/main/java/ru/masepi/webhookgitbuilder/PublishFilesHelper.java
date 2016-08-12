package ru.masepi.webhookgitbuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;

/**
 * Created by masepi on 11.08.16.
 */
public class PublishFilesHelper {
    static PublishFilesHelper publishFilesHelper;

    public static PublishFilesHelper getInstance() {
        if (publishFilesHelper == null) {
            publishFilesHelper = new PublishFilesHelper();
        }
        return publishFilesHelper;
    }

    public void addBuildResultRecord(String revision, List<Path> files, boolean isSuccess) throws IOException {
        Path dataFilePath = getDataFilePath();

        StringBuilder stringBuilder = new StringBuilder();
        for (Path file: files) {
            String fileName = file.getFileName().toString();
            String oneFileRecord = String.format("<a href=\"/run/%s\">%s</>;", fileName, fileName);
            stringBuilder.append(oneFileRecord);
        }

        String currentTime = Instant.now().toString();

        String buildStatus = isSuccess ? "SUCCESS" : "FAILED";
        String text = stringBuilder.toString() + ", " + currentTime + ", " + buildStatus + "\r\n";
        Files.write(dataFilePath, text.getBytes(), StandardOpenOption.APPEND);
    }

    public Path getDataFilePath() throws IOException {
        String appFolder = new File(".").getCanonicalPath();
        Path dataFilePath = Paths.get(appFolder, "src/main/resources/public/data.txt");
        return dataFilePath;
    }
}
