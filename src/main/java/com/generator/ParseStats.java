// src/main/java/com/generator/ParseStats.java

package com.generator;

/**
 * Estadísticas del parsing del Markdown.
 */
public class ParseStats {
    
    private final int totalBlocks;
    private final int javaFiles;
    private final int testFiles;
    private final int configFiles;
    private final int otherFiles;
    private final int totalLines;

    public ParseStats(int totalBlocks, int javaFiles, int testFiles, 
                     int configFiles, int otherFiles, int totalLines) {
        this.totalBlocks = totalBlocks;
        this.javaFiles = javaFiles;
        this.testFiles = testFiles;
        this.configFiles = configFiles;
        this.otherFiles = otherFiles;
        this.totalLines = totalLines;
    }

    public int getTotalBlocks() { return totalBlocks; }
    public int getJavaFiles() { return javaFiles; }
    public int getTestFiles() { return testFiles; }
    public int getConfigFiles() { return configFiles; }
    public int getOtherFiles() { return otherFiles; }
    public int getTotalLines() { return totalLines; }

    @Override
    public String toString() {
        return String.format(
            "Total: %d | Java: %d (Tests: %d) | Config: %d | Otros: %d | Líneas: %d",
            totalBlocks, javaFiles, testFiles, configFiles, otherFiles, totalLines);
    }
}