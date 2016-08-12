package ru.masepi.webhookgitbuilder;

import java.io.IOException;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Created by masepi on 07.08.16.
 */
public class StartPoint {

    public static void main(String[] args) throws IOException, GitAPIException, InterruptedException {
        MainApplication mainApplication = new MainApplication();
        mainApplication.run();
    }

}
