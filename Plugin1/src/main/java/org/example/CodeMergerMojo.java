package org.example;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Mojo(name = "merge", defaultPhase = LifecyclePhase.PACKAGE)
public class CodeMergerMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}/merged-code.java", property = "outputFile")
    private File outputFile;

    @Override
    public void execute() throws MojoExecutionException {

        List<String> sourceRoots = project.getCompileSourceRoots();

        if (sourceRoots == null || sourceRoots.isEmpty()) {
            getLog().warn("Java files are not found");
            return;
        }

        try {
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }

            Files.deleteIfExists(outputFile.toPath());
            Files.createFile(outputFile.toPath());

            for (String root : sourceRoots) {
                File rootDir = new File(root);
                if (rootDir.exists() && rootDir.isDirectory()) {
                    mergeDirectory(rootDir);
                }
            }

            getLog().info(" BUILD SUCCESS " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            throw new MojoExecutionException(" ERROR ", e);
        }
    }

    private void mergeDirectory(File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                mergeDirectory(file);

            } else if (file.getName().endsWith(".java")) {
                List<String> lines = Files.readAllLines(file.toPath());

                String separator = "\n\n" +
                        "// ------------------------------------------\n" +
                        "// FILE: " + file.getName() + "\n" +
                        "// ------------------------------------------\n\n";

                Files.write(outputFile.toPath(), separator.getBytes(), StandardOpenOption.APPEND);
                Files.write(outputFile.toPath(), lines, StandardOpenOption.APPEND);
            }
        }
    }
}