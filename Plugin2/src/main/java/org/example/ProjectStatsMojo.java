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

@Mojo(name = "project-stats", defaultPhase = LifecyclePhase.COMPILE)
public class ProjectStatsMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}/project-stats.txt", property = "reportOutputFile")
    private File outputFile;

    @Override
    public void execute() throws MojoExecutionException {

        List<String> sourceRoots = project.getCompileSourceRoots();

        if (sourceRoots == null || sourceRoots.isEmpty()) {
            getLog().warn("Java files are not found");
            return;
        }

        List<String> reportLines = new ArrayList<>();
        reportLines.add("----------------------------------------");
        reportLines.add("PROJECT CODE STATISTICS: " + project.getArtifactId());
        reportLines.add("----------------------------------------");

        int totalClasses = 0;
        int totalLines = 0;
        int totalComments = 0;
        long totalBytes = 0;

        for (String root : sourceRoots) {
            File sourceDir = new File(root);
            if (!sourceDir.exists()) continue;

            List<File> javaFiles = new ArrayList<>();

            findJavaFiles(sourceDir, javaFiles);

            for (File file : javaFiles) {
                try {
                    List<String> lines = Files.readAllLines(file.toPath());

                    int linesCount = lines.size();
                    int commentsCount = countComments(lines);
                    long bytesCount = file.length();

                    totalClasses++;
                    totalLines += linesCount;
                    totalComments += commentsCount;
                    totalBytes += bytesCount;

                    reportLines.add("File: " + file.getName());
                    reportLines.add("  Lines: " + linesCount);
                    reportLines.add("  Comments: " + commentsCount);
                    reportLines.add("  Size: " + bytesCount + " bytes");
                    reportLines.add("");

                } catch (IOException e) {
                    getLog().error("Could not read file: " + file.getName(), e);
                }
            }
        }

        reportLines.add("----------------------------------------");
        reportLines.add("TOTAL STATISTICS:");
        reportLines.add("----------------------------------------");

        reportLines.add("Java classes: " + totalClasses);
        reportLines.add("Lines of code: " + totalLines);
        reportLines.add("Comments: " + totalComments);
        reportLines.add("Project size: " + totalBytes + " bytes");

        saveReportToFile(reportLines);
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

    private int countComments(List<String> lines) {
        int n = 0;
        for (String line : lines) {
            if (line.trim().startsWith("//")) {
                n++;
            }
        }
        return n;
    }

    private void saveReportToFile(List<String> reportLines) throws MojoExecutionException {
        try {
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            Files.write(outputFile.toPath(), reportLines);
            getLog().info("Stats successfully saved to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            throw new MojoExecutionException("Error with writing to file", e);
        }
    }
}