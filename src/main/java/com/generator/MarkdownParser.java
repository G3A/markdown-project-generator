// src/main/java/com/generator/MarkdownParser.java

package com.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser de archivos Markdown que extrae bloques de código.
 */
public class MarkdownParser {

    // Patrones para extraer rutas de archivos
    private static final Pattern[] FILE_PATH_PATTERNS = {
        Pattern.compile("(src/(?:main|test)/(?:java|resources)/[^\\s`\"'<>]+\\.[a-zA-Z]+)"),
        Pattern.compile("\\b(pom\\.xml|build\\.gradle(?:\\.kts)?|settings\\.gradle(?:\\.kts)?|Dockerfile|docker-compose\\.ya?ml|\\.gitignore)\\b"),
        Pattern.compile("(application(?:-[a-zA-Z]+)?\\.(?:yml|yaml|properties))"),
        Pattern.compile("\\b([a-zA-Z][a-zA-Z0-9_-]*\\.(?:java|xml|yml|yaml|properties|json|sql|sh|md))\\b")
    };

    private static final Pattern PACKAGE_PATTERN = Pattern.compile(
        "^package\\s+([a-zA-Z_][a-zA-Z0-9_.]*);", Pattern.MULTILINE);

    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile(
        "(?:public\\s+)?(?:abstract\\s+)?(?:class|interface|enum|record)\\s+([A-Z][a-zA-Z0-9_]*)");

    private final Path markdownFile;
    private boolean debugMode = false;

    public MarkdownParser(Path markdownFile) {
        this.markdownFile = markdownFile;
    }

    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
    }

    public List<CodeBlock> parse() throws IOException {
        String content = Files.readString(markdownFile);
        List<CodeBlock> blocks = new ArrayList<>();

        String currentPhase = "Inicio";
        String currentSection = "";
        String previousLine = "";
        String previousNonEmptyLine = "";
        int order = 0;

        String[] lines = content.split("\n");
        StringBuilder currentCodeBlock = new StringBuilder();
        String currentLanguage = null;
        boolean insideCodeBlock = false;
        int codeBlockStartLine = 0;

        for (int lineNum = 0; lineNum < lines.length; lineNum++) {
            String line = lines[lineNum];
            String trimmedLine = line.trim();

            // Detectar inicio de fase
            if (trimmedLine.startsWith("## ")) {
                String phaseName = trimmedLine.substring(3).trim();
                if (containsPhaseKeyword(phaseName)) {
                    currentPhase = phaseName;
                    debug("Fase detectada: " + currentPhase);
                }
            }

            // Detectar sección
            if (trimmedLine.startsWith("### ")) {
                currentSection = trimmedLine.substring(4).trim();
                debug("Sección detectada: " + currentSection);
            }

            // Detectar inicio de bloque de código
            if (trimmedLine.startsWith("```") && !insideCodeBlock) {
                insideCodeBlock = true;
                codeBlockStartLine = lineNum;
                currentLanguage = trimmedLine.length() > 3 
                    ? trimmedLine.substring(3).trim() 
                    : "text";
                currentCodeBlock = new StringBuilder();
                debug("Inicio bloque " + currentLanguage + " en línea " + lineNum);
                continue;
            }

            // Detectar fin de bloque de código
            if (trimmedLine.equals("```") && insideCodeBlock) {
                insideCodeBlock = false;
                String code = currentCodeBlock.toString();
                
                debug("Fin bloque en línea " + lineNum + ", " + code.split("\n").length + " líneas");

                SearchContext context = new SearchContext(
                    code, currentLanguage, currentSection,
                    previousLine, previousNonEmptyLine,
                    getLinesBefore(lines, codeBlockStartLine, 5)
                );

                String filePath = extractFilePath(context);
                
                if (filePath != null) {
                    order++;
                    
                    CodeBlock block = new CodeBlock.Builder()
                        .order(order)
                        .filePath(normalizePath(filePath))
                        .language(currentLanguage)
                        .content(cleanCode(code, filePath))
                        .phase(currentPhase)
                        .description(currentSection)
                        .build();
                    
                    blocks.add(block);
                    debug("✓ Bloque agregado: " + filePath);
                } else {
                    debug("✗ No se pudo determinar ruta para bloque " + currentLanguage);
                }
                
                continue;
            }

            if (insideCodeBlock) {
                currentCodeBlock.append(line).append("\n");
            }

            previousLine = line;
            if (!trimmedLine.isEmpty()) {
                previousNonEmptyLine = trimmedLine;
            }
        }

        return blocks;
    }

    private boolean containsPhaseKeyword(String text) {
        String upper = text.toUpperCase();
        return upper.contains("FASE") || upper.contains("PARTE") || 
               upper.contains("PHASE") || upper.contains("STEP");
    }

    private String getLinesBefore(String[] lines, int currentLine, int count) {
        StringBuilder sb = new StringBuilder();
        int start = Math.max(0, currentLine - count);
        for (int i = start; i < currentLine; i++) {
            sb.append(lines[i]).append("\n");
        }
        return sb.toString();
    }

    private String extractFilePath(SearchContext ctx) {
        String path = null;

        path = findPathInCode(ctx.code);
        if (path != null) {
            debug("  Ruta encontrada en código: " + path);
            return path;
        }

        path = findPathInText(ctx.section);
        if (path != null) {
            debug("  Ruta encontrada en sección: " + path);
            return path;
        }

        path = findPathInText(ctx.linesBefore);
        if (path != null) {
            debug("  Ruta encontrada en contexto: " + path);
            return path;
        }

        if (ctx.language.equals("java")) {
            path = inferJavaPath(ctx.code);
            if (path != null) {
                debug("  Ruta inferida de Java: " + path);
                return path;
            }
        }

        path = inferConfigPath(ctx);
        if (path != null) {
            debug("  Ruta inferida de config: " + path);
            return path;
        }

        return null;
    }

    private String findPathInCode(String code) {
        String[] lines = code.split("\n");
        
        for (int i = 0; i < Math.min(5, lines.length); i++) {
            String line = lines[i].trim();
            
            if (line.startsWith("//")) {
                String comment = line.substring(2).trim();
                String path = extractPathFromText(comment);
                if (path != null) return path;
            }
            
            if (line.startsWith("#") && !line.startsWith("#!")) {
                String comment = line.substring(1).trim();
                String path = extractPathFromText(comment);
                if (path != null) return path;
            }
            
            if (line.contains("<!--")) {
                String path = extractPathFromText(line);
                if (path != null) return path;
            }
        }
        
        return null;
    }

    private String findPathInText(String text) {
        if (text == null || text.isEmpty()) return null;
        return extractPathFromText(text);
    }

    private String extractPathFromText(String text) {
        for (Pattern pattern : FILE_PATH_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String match = matcher.group(1);
                if (isValidPath(match)) {
                    return match;
                }
            }
        }
        return null;
    }

    private boolean isValidPath(String path) {
        if (!path.contains(".")) return false;
        if (path.startsWith(".")) return false;
        
        String[] validExtensions = {
            ".java", ".xml", ".yml", ".yaml", ".properties", 
            ".json", ".sql", ".sh", ".md", ".gradle", ".kts",
            ".html", ".css", ".js", ".ts"
        };
        
        for (String ext : validExtensions) {
            if (path.endsWith(ext)) return true;
        }
        
        String[] specialFiles = {"Dockerfile", ".gitignore", "Makefile"};
        for (String special : specialFiles) {
            if (path.endsWith(special)) return true;
        }
        
        return false;
    }

    private String inferJavaPath(String code) {
        String packageName = null;
        String className = null;

        Matcher packageMatcher = PACKAGE_PATTERN.matcher(code);
        if (packageMatcher.find()) {
            packageName = packageMatcher.group(1);
        }

        Matcher classMatcher = CLASS_NAME_PATTERN.matcher(code);
        if (classMatcher.find()) {
            className = classMatcher.group(1);
        }

        if (packageName != null && className != null) {
            String packagePath = packageName.replace('.', '/');
            String sourceFolder = isTestClass(className, code) 
                ? "src/test/java" 
                : "src/main/java";
            
            return sourceFolder + "/" + packagePath + "/" + className + ".java";
        }

        return null;
    }

    private boolean isTestClass(String className, String code) {
        if (className.endsWith("Test") || className.endsWith("Tests") ||
            className.endsWith("Spec") || className.contains("Contract") ||
            className.endsWith("IT")) {
            return true;
        }
        
        if (code.contains("@Test") || code.contains("@SpringBootTest") ||
            code.contains("@DataJpaTest") || code.contains("@WebMvcTest") ||
            code.contains("@ExtendWith") || code.contains("@Testcontainers")) {
            return true;
        }
        
        return false;
    }

    private String inferConfigPath(SearchContext ctx) {
        String lang = ctx.language;
        String code = ctx.code;
        String section = ctx.section.toLowerCase();

        if (lang.equals("xml") && code.contains("<project")) {
            return "pom.xml";
        }

        if (lang.equals("xml") && (code.contains("<beans") || code.contains("<configuration"))) {
            return "src/main/resources/application-context.xml";
        }

        if (lang.equals("yaml") || lang.equals("yml")) {
            if (code.contains("spring:") || code.contains("server:") || 
                code.contains("datasource:") || code.contains("jpa:")) {
                
                if (section.contains("test") || ctx.linesBefore.toLowerCase().contains("test")) {
                    return "src/test/resources/application-test.yml";
                }
                return "src/main/resources/application.yml";
            }
            
            if (code.contains("services:") && code.contains("image:")) {
                return "docker-compose.yml";
            }
        }

        if (lang.equals("properties")) {
            if (section.contains("test") || ctx.linesBefore.toLowerCase().contains("test")) {
                return "src/test/resources/application-test.properties";
            }
            return "src/main/resources/application.properties";
        }

        if (lang.equals("sql")) {
            if (code.toUpperCase().contains("CREATE TABLE")) {
                return "src/main/resources/schema.sql";
            }
            if (code.toUpperCase().contains("INSERT INTO")) {
                return "src/main/resources/data.sql";
            }
            return "src/main/resources/script.sql";
        }

        if (lang.equals("dockerfile") || code.trim().startsWith("FROM ")) {
            return "Dockerfile";
        }

        if (lang.equals("bash") || lang.equals("sh") || code.trim().startsWith("#!/bin/")) {
            String scriptName = extractScriptName(ctx.section);
            return scriptName != null ? scriptName : "scripts/script.sh";
        }

        if (lang.equals("gradle") || lang.equals("groovy")) {
            if (code.contains("plugins {") || code.contains("dependencies {")) {
                return "build.gradle";
            }
        }

        if (lang.equals("kotlin") || lang.equals("kts")) {
            if (code.contains("plugins {") || code.contains("dependencies {")) {
                return "build.gradle.kts";
            }
        }

        return null;
    }

    private String extractScriptName(String section) {
        Pattern scriptPattern = Pattern.compile("([a-zA-Z][a-zA-Z0-9_-]*\\.sh)");
        Matcher matcher = scriptPattern.matcher(section);
        if (matcher.find()) {
            return "scripts/" + matcher.group(1);
        }
        return null;
    }

    private String normalizePath(String path) {
        path = path.trim();
        path = path.replaceAll("[`'\"<>]", "");
        
        if (path.equals("application.yml") || path.equals("application.yaml")) {
            return "src/main/resources/application.yml";
        }
        if (path.equals("application.properties")) {
            return "src/main/resources/application.properties";
        }
        if (path.equals("application-test.yml") || path.equals("application-test.yaml")) {
            return "src/test/resources/application-test.yml";
        }
        if (path.equals("application-test.properties")) {
            return "src/test/resources/application-test.properties";
        }
        
        return path;
    }

    private String cleanCode(String code, String filePath) {
        if (code == null) return "";
        
        String[] lines = code.split("\n");
        StringBuilder cleaned = new StringBuilder();
        boolean skipFirst = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();
            
            if (i == 0 && containsPath(trimmed, filePath)) {
                skipFirst = true;
                continue;
            }
            
            if (skipFirst && i == 1 && (trimmed.isEmpty() || trimmed.equals("//") || trimmed.equals("#"))) {
                continue;
            }
            
            cleaned.append(line).append("\n");
        }
        
        return cleaned.toString().trim();
    }

    private boolean containsPath(String line, String filePath) {
        if (filePath == null) return false;
        
        String fileName = filePath.contains("/") 
            ? filePath.substring(filePath.lastIndexOf('/') + 1) 
            : filePath;
        
        return line.contains(filePath) || line.contains(fileName);
    }

    private void debug(String message) {
        if (debugMode) {
            System.out.println("[DEBUG] " + message);
        }
    }

    // Clase interna para contexto de búsqueda
    private static class SearchContext {
        final String code;
        final String language;
        final String section;
        final String previousLine;
        final String previousNonEmptyLine;
        final String linesBefore;

        SearchContext(String code, String language, String section, 
                     String previousLine, String previousNonEmptyLine, String linesBefore) {
            this.code = code;
            this.language = language;
            this.section = section;
            this.previousLine = previousLine;
            this.previousNonEmptyLine = previousNonEmptyLine;
            this.linesBefore = linesBefore;
        }
    }

    /**
     * Obtiene estadísticas del parsing.
     */
    public ParseStats getStats(List<CodeBlock> blocks) {
        int javaFiles = 0;
        int testFiles = 0;
        int configFiles = 0;
        int otherFiles = 0;
        int totalLines = 0;

        for (CodeBlock block : blocks) {
            String path = block.getFilePath();
            totalLines += block.getContent().split("\n").length;
            
            if (path.endsWith(".java")) {
                javaFiles++;
                if (path.contains("/test/")) {
                    testFiles++;
                }
            } else if (path.endsWith(".xml") || path.endsWith(".yml") || 
                       path.endsWith(".yaml") || path.endsWith(".properties")) {
                configFiles++;
            } else {
                otherFiles++;
            }
        }

        return new ParseStats(blocks.size(), javaFiles, testFiles, configFiles, otherFiles, totalLines);
    }
}