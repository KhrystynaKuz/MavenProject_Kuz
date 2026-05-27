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
import java.util.ArrayList;
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

        List<String> mergedLines = new ArrayList<>();
        List<File> javaFiles = new ArrayList<>();

        for (String root : sourceRoots) {
            File rootDir = new File(root);
            if (rootDir.exists() && rootDir.isDirectory()) {
                findJavaFiles(rootDir, javaFiles);
            }
        }

        for (File file : javaFiles) {
            try {
                mergedLines.add("");
                mergedLines.add("// ------------------------------------------");
                mergedLines.add("// FILE: " + file.getName());
                mergedLines.add("// ------------------------------------------");
                mergedLines.add("");

                List<String> fileLines = Files.readAllLines(file.toPath());
                mergedLines.addAll(fileLines);

            } catch (IOException e) {
                getLog().error("Could not read file: " + file.getName(), e);
            }
        }

        try {
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            Files.write(outputFile.toPath(), mergedLines);
            getLog().info("BUILD SUCCESS: " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            throw new MojoExecutionException("ERROR", e);
        }
    }

    private void findJavaFiles(File dir, List<File> fileList) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                findJavaFiles(file, fileList);
            } else if (file.getName().endsWith(".java")) {
                fileList.add(file);
            }
        }
    }
}