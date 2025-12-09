// src/main/java/com/generator/GenerationResult.java

package com.generator;

/**
 * Resultado de la generación del proyecto.
 */
public class GenerationResult {
    
    private final int filesCreated;
    private final int directoriesCreated;

    public GenerationResult(int filesCreated, int directoriesCreated) {
        this.filesCreated = filesCreated;
        this.directoriesCreated = directoriesCreated;
    }

    public int getFilesCreated() { return filesCreated; }
    public int getDirectoriesCreated() { return directoriesCreated; }

    @Override
    public String toString() {
        return String.format("Archivos creados: %d | Directorios creados: %d", 
            filesCreated, directoriesCreated);
    }
}