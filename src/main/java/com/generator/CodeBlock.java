// src/main/java/com/generator/CodeBlock.java

package com.generator;

/**
 * Representa un bloque de código extraído del Markdown.
 */
public class CodeBlock {
    
    private final int order;
    private final String filePath;
    private final String language;
    private final String content;
    private final String phase;
    private final String description;
    private final BlockType type;

    public enum BlockType {
        PRODUCTION_CODE("Código de Producción", "PROD"),
        TEST_CODE("Código de Test", "TEST"),
        CONFIGURATION("Configuración", "CONF"),
        RESOURCE("Recurso", "RES"),
        SCRIPT("Script", "SCR"),
        OTHER("Otro", "OTRO");

        private final String displayName;
        private final String shortName;

        BlockType(String displayName, String shortName) {
            this.displayName = displayName;
            this.shortName = shortName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getShortName() {
            return shortName;
        }
    }

    private CodeBlock(Builder builder) {
        this.order = builder.order;
        this.filePath = builder.filePath;
        this.language = builder.language;
        this.content = builder.content;
        this.phase = builder.phase;
        this.description = builder.description;
        this.type = determineType(builder.filePath, builder.language);
    }

    private static BlockType determineType(String filePath, String language) {
        if (filePath == null) return BlockType.OTHER;
        
        String path = filePath.toLowerCase();
        
        // Tests
        if (path.contains("/test/") || path.contains("test.java") || 
            path.contains("spec.java") || path.contains("tests.java")) {
            return BlockType.TEST_CODE;
        }
        
        // Configuración
        if (path.endsWith(".xml") || path.endsWith(".yml") || 
            path.endsWith(".yaml") || path.endsWith(".properties") ||
            path.endsWith(".json")) {
            return BlockType.CONFIGURATION;
        }
        
        // Scripts
        if (path.endsWith(".sh") || path.endsWith(".bat") || 
            path.equals("dockerfile") || path.contains("docker-compose")) {
            return BlockType.SCRIPT;
        }
        
        // Recursos
        if (path.contains("/resources/") && !path.endsWith(".java")) {
            return BlockType.RESOURCE;
        }
        
        // Código de producción Java
        if (path.endsWith(".java") && path.contains("/main/")) {
            return BlockType.PRODUCTION_CODE;
        }
        
        // Por defecto, código de producción
        if (path.endsWith(".java")) {
            return BlockType.PRODUCTION_CODE;
        }
        
        return BlockType.OTHER;
    }

    // Getters
    public int getOrder() { return order; }
    public String getFilePath() { return filePath; }
    public String getLanguage() { return language; }
    public String getContent() { return content; }
    public String getPhase() { return phase; }
    public String getDescription() { return description; }
    public BlockType getType() { return type; }

    public boolean isJavaFile() {
        return filePath != null && filePath.endsWith(".java");
    }

    public boolean isTestFile() {
        return type == BlockType.TEST_CODE;
    }

    public boolean isConfigFile() {
        return type == BlockType.CONFIGURATION;
    }

    public boolean isResourceFile() {
        return type == BlockType.RESOURCE || type == BlockType.CONFIGURATION;
    }

    public String getFileName() {
        if (filePath == null) return "unknown";
        int lastSlash = filePath.lastIndexOf('/');
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }

    public String getPackagePath() {
        if (filePath == null) return "";
        return filePath;
    }

    public String getFileExtension() {
        if (filePath == null) return "";
        int lastDot = filePath.lastIndexOf('.');
        return lastDot >= 0 ? filePath.substring(lastDot + 1) : "";
    }

    @Override
    public String toString() {
        return String.format("[%d] %s - %s (%s)", 
            order, phase, getFileName(), type.getShortName());
    }

    // Builder Pattern
    public static class Builder {
        private int order;
        private String filePath;
        private String language = "text";
        private String content;
        private String phase = "General";
        private String description = "";

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder phase(String phase) {
            this.phase = phase;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public CodeBlock build() {
            return new CodeBlock(this);
        }
    }
}