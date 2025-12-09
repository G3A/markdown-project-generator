// src/main/java/com/generator/App.java

package com.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class App {

    private static final String VERSION = "1.0.0";

    public static void main(String[] args) {
        ConsoleUtils.init();

        try {
            run(args);
        } catch (Exception e) {
            ConsoleUtils.printError("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            ConsoleUtils.cleanup();
        }
    }

    private static void run(String[] args) throws IOException {
        if (args.length == 0 || args[0].equals("--help") || args[0].equals("-h")) {
            printUsage();
            return;
        }

        if (args[0].equals("--version") || args[0].equals("-v")) {
            System.out.println("Markdown Project Generator v" + VERSION);
            return;
        }

        // Parsear argumentos
        Path markdownFile = Paths.get(args[0]).toAbsolutePath();

        Path outputDirectory;
        if (args.length > 1 && !args[1].startsWith("--")) {
            outputDirectory = Paths.get(args[1]).toAbsolutePath();
        } else {
            outputDirectory = Paths.get("generated-project").toAbsolutePath();
        }

        boolean autoMode = hasFlag(args, "--auto", "-a");
        boolean forceMode = hasFlag(args, "--force", "-f");
        boolean debugMode = hasFlag(args, "--debug", "-d");
        boolean noReadme = hasFlag(args, "--no-readme");

        // Validar archivo de entrada
        if (!Files.exists(markdownFile)) {
            throw new IOException("Archivo no encontrado: " + markdownFile);
        }

        // Crear directorio de salida
        if (!Files.exists(outputDirectory)) {
            Files.createDirectories(outputDirectory);
            ConsoleUtils.printInfo("Directorio creado: " + outputDirectory);
        } else if (!forceMode) {
            try {
                if (Files.list(outputDirectory).findAny().isPresent()) {
                    ConsoleUtils.printWarning("El directorio no esta vacio: " + outputDirectory);
                    System.out.print("Continuar? (s/n): ");
                    Scanner scanner = new Scanner(System.in);
                    String input = scanner.nextLine();
                    if (!input.toLowerCase().startsWith("s")) {
                        ConsoleUtils.printInfo("Cancelado.");
                        return;
                    }
                }
            } catch (IOException e) {
                // Ignorar error al listar
            }
        }

        // Parsear Markdown
        ConsoleUtils.printHeader("MARKDOWN PROJECT GENERATOR v" + VERSION);
        ConsoleUtils.printInfo("Archivo fuente: " + markdownFile);
        ConsoleUtils.printInfo("Directorio destino: " + outputDirectory);
        ConsoleUtils.printInfo("Generar README: " + (noReadme ? "NO" : "SI"));

        if (debugMode) {
            ConsoleUtils.printWarning("Modo DEBUG activado");
        }
        System.out.println();

        MarkdownParser parser = new MarkdownParser(markdownFile);
        parser.setDebugMode(debugMode);

        List<CodeBlock> blocks = parser.parse();

        if (blocks.isEmpty()) {
            ConsoleUtils.printWarning("No se encontraron bloques de codigo con rutas de archivo.");
            return;
        }

        ParseStats stats = parser.getStats(blocks);
        ConsoleUtils.printSuccess("Bloques encontrados: " + blocks.size());
        ConsoleUtils.printInfo("Estadisticas: " + stats);
        System.out.println();

        // Modo automático o interactivo
        if (autoMode) {
            runAutoMode(blocks, outputDirectory, markdownFile, noReadme);
        } else {
            runInteractiveMode(blocks, outputDirectory, markdownFile);
        }
    }

    private static void runAutoMode(List<CodeBlock> blocks, Path outputDirectory,
                                    Path markdownFile, boolean noReadme) throws IOException {
        ConsoleUtils.printSubHeader("Modo Automatico");

        ProjectGenerator generator = new ProjectGenerator(outputDirectory, blocks);

        // Crear estructura base
        generator.generateProjectStructure();
        ConsoleUtils.printSuccess("Estructura de directorios creada");

        // Generar todos los archivos de código
        GenerationResult result = generator.generateAll();
        ConsoleUtils.printSuccess("Archivos de codigo generados: " + result.getFilesCreated());

        // Generar README.md con el contenido de la guía
        if (!noReadme) {
            System.out.println();
            ConsoleUtils.printInfo("Generando README.md...");
            try {
                generator.generateReadme(markdownFile);

                // Verificar que se creó
                Path readmePath = outputDirectory.resolve("README.md");
                if (Files.exists(readmePath)) {
                    ConsoleUtils.printSuccess("README.md generado (" + Files.size(readmePath) + " bytes)");
                } else {
                    ConsoleUtils.printError("README.md NO fue creado (razon desconocida)");
                }
            } catch (IOException e) {
                ConsoleUtils.printError("Error al generar README.md: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            ConsoleUtils.printInfo("README.md omitido (--no-readme)");
        }

        // Resumen final
        System.out.println();
        System.out.println("==================================================");
        ConsoleUtils.printSuccess("Proyecto generado exitosamente!");
        System.out.println("==================================================");
        System.out.println();
        System.out.println("Archivos creados: " + generator.getFilesCreated());
        System.out.println("Directorios creados: " + generator.getDirectoriesCreated());
        System.out.println("Ubicacion: " + outputDirectory);

        // Listar archivos generados
        System.out.println();
        System.out.println("Estructura del proyecto:");
        System.out.println();
        printDirectoryTree(outputDirectory, "", true);

        // Mostrar siguiente paso
        System.out.println();
        System.out.println("Proximos pasos:");
        System.out.println("  cd " + outputDirectory);
        System.out.println("  cat README.md");
        System.out.println("  mvn clean test");
    }

    /**
     * Imprime el árbol de directorios usando caracteres ASCII.
     */
    private static void printDirectoryTree(Path directory, String prefix, boolean isRoot) {
        try {
            if (isRoot) {
                System.out.println(directory.getFileName() + "/");
            }

            List<Path> entries = Files.list(directory)
                    .filter(p -> !p.getFileName().toString().startsWith("."))
                    .filter(p -> !p.getFileName().toString().equals("target"))
                    .sorted((a, b) -> {
                        // Directorios primero, luego archivos
                        boolean aIsDir = Files.isDirectory(a);
                        boolean bIsDir = Files.isDirectory(b);
                        if (aIsDir && !bIsDir) return -1;
                        if (!aIsDir && bIsDir) return 1;
                        return a.getFileName().toString().compareTo(b.getFileName().toString());
                    })
                    .collect(Collectors.toList());

            for (int i = 0; i < entries.size(); i++) {
                Path entry = entries.get(i);
                boolean isLast = (i == entries.size() - 1);
                String connector = isLast ? "+-- " : "|-- ";
                String childPrefix = isLast ? "    " : "|   ";

                String name = entry.getFileName().toString();

                if (Files.isDirectory(entry)) {
                    System.out.println(prefix + connector + "[DIR] " + name);
                    printDirectoryTree(entry, prefix + childPrefix, false);
                } else {
                    String type = getFileType(name);
                    System.out.println(prefix + connector + type + " " + name);
                }
            }
        } catch (IOException e) {
            System.out.println(prefix + "+-- (error al leer directorio)");
        }
    }

    /**
     * Obtiene el tipo de archivo para mostrar.
     */
    private static String getFileType(String fileName) {
        if (fileName.endsWith(".java")) {
            return "[JAVA]";
        } else if (fileName.endsWith(".xml")) {
            return "[XML] ";
        } else if (fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
            return "[YAML]";
        } else if (fileName.endsWith(".properties")) {
            return "[PROP]";
        } else if (fileName.endsWith(".md")) {
            return "[MD]  ";
        } else if (fileName.endsWith(".sql")) {
            return "[SQL] ";
        } else if (fileName.endsWith(".sh")) {
            return "[SH]  ";
        } else {
            return "[FILE]";
        }
    }

    private static void runInteractiveMode(List<CodeBlock> blocks, Path outputDirectory,
                                           Path markdownFile) {
        InteractiveMode interactive = new InteractiveMode(blocks, outputDirectory, markdownFile);
        interactive.start();
    }

    private static boolean hasFlag(String[] args, String... flags) {
        for (String arg : args) {
            for (String flag : flags) {
                if (arg.equals(flag)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void printUsage() {
        System.out.println();
        System.out.println("+===================================================================+");
        System.out.println("|          MARKDOWN PROJECT GENERATOR                              |");
        System.out.println("|          Genera proyectos Java desde archivos Markdown           |");
        System.out.println("+===================================================================+");
        System.out.println();
        System.out.println("USO:");
        System.out.println("  java -jar generator.jar <archivo.md> [directorio-salida] [opciones]");
        System.out.println();
        System.out.println("ARGUMENTOS:");
        System.out.println("  archivo.md          Archivo Markdown con los bloques de codigo");
        System.out.println("  directorio-salida   Directorio donde generar (default: ./generated-project)");
        System.out.println();
        System.out.println("OPCIONES:");
        System.out.println("  --auto, -a          Modo automatico (genera todo sin preguntar)");
        System.out.println("  --force, -f         No preguntar si el directorio existe");
        System.out.println("  --debug, -d         Mostrar informacion de debug");
        System.out.println("  --no-readme         No generar README.md");
        System.out.println("  --help, -h          Mostrar esta ayuda");
        System.out.println();
        System.out.println("EJEMPLOS:");
        System.out.println("  java -jar generator.jar guia.md ./mi-proyecto --auto");
        System.out.println("  java -jar generator.jar guia.md ./mi-proyecto --auto --no-readme");
        System.out.println();
    }
}