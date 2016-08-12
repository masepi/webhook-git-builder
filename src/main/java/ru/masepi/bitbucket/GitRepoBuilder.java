package ru.masepi.bitbucket;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.util.FS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

/**
 * Created by masepi on 10.08.16.
 */
public class GitRepoBuilder implements Runnable {
    private String gitRepositoryPath;
    private String sshKeyPath;
    private String buildCommand;
    private String[] publishFiles;
    private String buildResultsFolder;

    private class LocalRepository {
        Path dir;
        String revision;
    }

    GitRepoBuilder(String repository, String sshKey, String buildCommand, String[] publishFiles, String buildResultsFolder) {
        this.gitRepositoryPath = repository;
        this.sshKeyPath = sshKey;
        this.buildCommand = buildCommand;
        this.publishFiles = publishFiles;
        this.buildResultsFolder = buildResultsFolder;
    }

    @Override
    public void run() {

        try {
            synchronized (this) {
                LocalRepository localRepository = cloneRepository();
                Runtime runtime = Runtime.getRuntime();
                Process process = runtime.exec("/bin/bash " + buildCommand, new String[]{}, localRepository.dir.toFile());
                process.waitFor();
                publishResults(localRepository);
            }
        } catch (Exception e) {
            // TODO add logging
        }
    }

    private LocalRepository cloneRepository() throws GitAPIException, IOException {
        Path tempDir = Files.createTempDirectory("bitbucket-run-temp");

        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session ) {
                // do nothing
            }

            @Override
            protected JSch createDefaultJSch(FS fs ) throws JSchException {
                JSch defaultJSch = super.createDefaultJSch( fs );
                defaultJSch.addIdentity(sshKeyPath);
                return defaultJSch;
            }

            @Override
            protected JSch getJSch(final OpenSshConfig.Host hc, FS fs) throws JSchException {
                JSch jsch = super.getJSch(hc, fs);
                jsch.removeAllIdentity();
                jsch.addIdentity(sshKeyPath);
                return jsch;
            }
        };

        CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand.setURI(gitRepositoryPath);
        cloneCommand.setTransportConfigCallback( (transport) -> {
            SshTransport sshTransport = (SshTransport)transport;
            sshTransport.setSshSessionFactory(sshSessionFactory);
        });

        cloneCommand.setDirectory(tempDir.toFile());
        cloneCommand.call();

        LocalRepository localRepository = new LocalRepository();
        localRepository.dir = tempDir;
       // localRepository.revision = cloneCommand.getRepository().getRef("HEAD").getName();
        localRepository.revision = "";
        return localRepository;
    }

    private void publishResults(LocalRepository localRepository) throws IOException {
        ArrayList<Path> fileNames = new ArrayList<Path>();
        boolean isSucceded = true;
        for(String file: publishFiles) {
            Path fullPath = Paths.get(localRepository.dir.toString(), file);
            if (Files.notExists(fullPath)) {
                isSucceded = false;
            }

            String fileName = getUniqueFileName(getFileExt(file));
            Path outputPath = Paths.get(buildResultsFolder, fileName);
            Files.copy(fullPath, outputPath);

            fileNames.add(outputPath);
        }

        PublishFilesHelper.getInstance().addBuildResultRecord(localRepository.revision, fileNames, isSucceded);
    }

    private static String getUniqueFileName(String ext) {
        return Long.toString(System.nanoTime()) + "." + ext;
    }

    private static String getFileExt(String name) {
        int pos = name.lastIndexOf(".");
        if (pos != -1 && pos != 0) {
            return name.substring(pos + 1);
        }
        return "";
    }
}
