package ru.masepi.webhookgitbuilder;

import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static spark.Spark.*;

/**
 * Created by masepi on 12.08.16.
 */
public class MainApplication {
    private static String BUILD_RESULTS_FOLDER = "src/main/resources/public/run";
    private ProgramProperties programProperties;

    public MainApplication() throws IOException {
        this.programProperties = new ProgramProperties();
    }

    void run() throws InterruptedException, GitAPIException, IOException {
        port(programProperties.port);
        staticFileLocation("/public");

        post("/", (req, res) -> {
            performBuild();
            return "Build started\r\n";
        });
    }

    static private void performBuild() throws IOException, GitAPIException, InterruptedException {
        String appFolder = new File(".").getCanonicalPath();
        String buildResultsFolder = Paths.get(appFolder, BUILD_RESULTS_FOLDER).toString();

        ProgramProperties properties = new ProgramProperties();

        GitRepoBuilder gitRepoBuilder = new GitRepoBuilder(
                properties.repositoryURI,
                properties.sshKeyFile,
                properties.buildScript,
                properties.publishFiles,
                buildResultsFolder);

        Thread newThread = new Thread(gitRepoBuilder);
        newThread.run();
    }


}
