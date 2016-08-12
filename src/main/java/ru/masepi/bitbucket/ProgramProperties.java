package ru.masepi.bitbucket;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created by masepi on 12.08.16.
 */
public class ProgramProperties {
    public String repositoryURI;
    public String sshKeyFile;
    public String buildScript;
    public String[] publishFiles;
    public int port;

    public ProgramProperties() throws IOException {
        String appFolder = new File(".").getCanonicalPath();
        Path propertiesFile = Paths.get(appFolder, "/src/main/resources/local.properties");
        FileInputStream fileInputStream = new FileInputStream(propertiesFile.toString());
        Properties properties = new Properties();
        properties.load(fileInputStream);

        this.repositoryURI = properties.getProperty("repositoryURI");
        this.sshKeyFile = properties.getProperty("sshKeyFile");
        this.buildScript = properties.getProperty("buildScript");

        String publishFilesArray = properties.getProperty("publishFiles");
        this.publishFiles = publishFilesArray.split(";");

        this.port = Integer.parseInt(properties.getProperty("port"));
    }
}
