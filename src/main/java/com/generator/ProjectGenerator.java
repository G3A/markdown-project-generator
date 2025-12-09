// src/main/java/com/generator/ProjectGenerator.java

package com.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class ProjectGenerator {

    private final Path outputDirectory;
    private final List<CodeBlock> blocks;
    private int filesCreated = 0;
    private int directoriesCreated = 0;

    public ProjectGenerator(Path outputDirectory, List<CodeBlock> blocks) {
        this.outputDirectory = outputDirectory;
        this.blocks = blocks;
    }

    public GenerationResult generateAll() throws IOException {
        for (CodeBlock block : blocks) {
            generateFile(block);
        }
        return new GenerationResult(filesCreated, directoriesCreated);
    }

    public void generateFile(int index) throws IOException {
        if (index < 0 || index >= blocks.size()) {
            throw new IllegalArgumentException("Índice fuera de rango: " + index);
        }
        generateFile(blocks.get(index));
    }

    public void generateFile(CodeBlock block) throws IOException {
        if (block.getFilePath() == null || block.getFilePath().isEmpty()) {
            return;
        }

        Path filePath = outputDirectory.resolve(block.getFilePath());

        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
            directoriesCreated++;
        }

        String contentToWrite = prepareContent(block);

        Files.writeString(filePath, contentToWrite,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        filesCreated++;
    }

    /**
     * Genera el archivo README.md con el contenido de la guía original.
     */
    public void generateReadme(Path sourceMarkdownFile) throws IOException {
        // Verificar que el archivo fuente existe
        if (!Files.exists(sourceMarkdownFile)) {
            throw new IOException("Archivo fuente no encontrado: " + sourceMarkdownFile);
        }

        // Leer contenido
        String content = Files.readString(sourceMarkdownFile);

        // Crear el README
        Path readmePath = outputDirectory.resolve("README.md");

        // Asegurar que el directorio existe
        if (!Files.exists(outputDirectory)) {
            Files.createDirectories(outputDirectory);
        }

        String fileName = sourceMarkdownFile.getFileName().toString();

        StringBuilder header = new StringBuilder();
        header.append("<!-- \n");
        header.append("  ╔═══════════════════════════════════════════════════════════════════╗\n");
        header.append("  ║  Este archivo fue generado automáticamente desde la guía fuente.  ║\n");
        header.append("  ║  Fuente: ").append(padRight(fileName, 52)).append(" ║\n");
        header.append("  ╚═══════════════════════════════════════════════════════════════════╝\n");
        header.append("-->\n\n");

        String fullContent = header.toString() + content;

        Files.writeString(readmePath, fullContent,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        filesCreated++;

        // Debug: confirmar que se creó
        if (Files.exists(readmePath)) {
            System.out.println("  [DEBUG] README.md creado en: " + readmePath.toAbsolutePath());
            System.out.println("  [DEBUG] Tamaño: " + Files.size(readmePath) + " bytes");
        }
    }

    private String padRight(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) {
            return text.substring(0, length);
        }
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() < length) {
            sb.append(" ");
        }
        return sb.toString();
    }

    private String prepareContent(CodeBlock block) {
        String content = block.getContent();
        String filePath = block.getFilePath();

        if (filePath.endsWith(".java")) {
            content = ensurePackageStatement(filePath, content);
        }

        if (filePath.endsWith(".xml") && !content.trim().startsWith("<?xml") &&
                !content.trim().startsWith("<project")) {
            if (content.contains("<beans") || content.contains("<configuration")) {
                content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + content;
            }
        }

        return content;
    }

    private String ensurePackageStatement(String filePath, String content) {
        if (content.trim().startsWith("package ")) {
            return content;
        }

        String packageName = extractPackageFromPath(filePath);

        if (packageName != null && !packageName.isEmpty()) {
            return "package " + packageName + ";\n\n" + content;
        }

        return content;
    }

    private String extractPackageFromPath(String filePath) {
        int javaIndex = filePath.indexOf("/java/");
        if (javaIndex == -1) return null;

        String packagePart = filePath.substring(javaIndex + 6);
        int lastSlash = packagePart.lastIndexOf('/');
        if (lastSlash == -1) return null;

        return packagePart.substring(0, lastSlash).replace('/', '.');
    }

    public void generateProjectStructure() throws IOException {
        String[] baseDirs = {
                "src/main/java",
                "src/main/resources",
                "src/test/java",
                "src/test/resources"
        };

        for (String dir : baseDirs) {
            Path dirPath = outputDirectory.resolve(dir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                directoriesCreated++;
            }
        }
    }

    public boolean fileExists(CodeBlock block) {
        if (block.getFilePath() == null) return false;
        return Files.exists(outputDirectory.resolve(block.getFilePath()));
    }

    public String getExistingContent(CodeBlock block) throws IOException {
        if (block.getFilePath() == null) return null;

        Path filePath = outputDirectory.resolve(block.getFilePath());
        if (Files.exists(filePath)) {
            return Files.readString(filePath);
        }
        return null;
    }

    public int getFilesCreated() {
        return filesCreated;
    }

    public int getDirectoriesCreated() {
        return directoriesCreated;
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }
}