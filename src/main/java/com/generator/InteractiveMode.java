// src/main/java/com/generator/InteractiveMode.java

package com.generator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Modo interactivo paso a paso para generar el proyecto.
 */
public class InteractiveMode {

    private final List<CodeBlock> blocks;
    private final ProjectGenerator generator;
    private final Path markdownFile;
    private final Scanner scanner;
    private int currentIndex = 0;
    private final Set<Integer> generatedBlocks = new HashSet<>();
    private boolean readmeGenerated = false;

    public InteractiveMode(List<CodeBlock> blocks, Path outputDirectory, Path markdownFile) {
        this.blocks = blocks;
        this.generator = new ProjectGenerator(outputDirectory, blocks);
        this.markdownFile = markdownFile;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        ConsoleUtils.printHeader("GENERADOR DE PROYECTO - MODO INTERACTIVO");

        showStats();

        while (true) {
            showMainMenu();
            String input = readInput();

            switch (input.toLowerCase()) {
                case "1":
                case "n":
                case "next":
                    nextBlock();
                    break;
                case "2":
                case "p":
                case "prev":
                    previousBlock();
                    break;
                case "3":
                case "g":
                case "goto":
                    goToBlock();
                    break;
                case "4":
                case "c":
                case "current":
                    showCurrentBlock();
                    break;
                case "5":
                case "a":
                case "all":
                    generateAll();
                    break;
                case "6":
                case "r":
                case "remaining":
                    generateRemaining();
                    break;
                case "7":
                case "l":
                case "list":
                    listAllBlocks();
                    break;
                case "8":
                case "f":
                case "filter":
                    filterBlocks();
                    break;
                case "9":
                case "s":
                case "status":
                    showStatus();
                    break;
                case "10":
                case "readme":
                    generateReadme();
                    break;
                case "0":
                case "q":
                case "quit":
                case "exit":
                    ConsoleUtils.printInfo("¡Hasta luego!");
                    return;
                case "h":
                case "help":
                    showHelp();
                    break;
                default:
                    ConsoleUtils.printWarning("Opción no válida. Escribe 'h' para ayuda.");
            }
        }
    }

    private void showStats() {
        ConsoleUtils.printInfo("Archivo cargado exitosamente.");
        System.out.println();

        Map<String, List<CodeBlock>> byPhase = new LinkedHashMap<>();
        for (CodeBlock block : blocks) {
            byPhase.computeIfAbsent(block.getPhase(), k -> new ArrayList<>()).add(block);
        }

        System.out.println("📊 Resumen del proyecto:");
        System.out.println();

        for (Map.Entry<String, List<CodeBlock>> entry : byPhase.entrySet()) {
            long tests = entry.getValue().stream().filter(CodeBlock::isTestFile).count();
            long prod = entry.getValue().stream().filter(b -> b.isJavaFile() && !b.isTestFile()).count();
            long config = entry.getValue().stream().filter(CodeBlock::isConfigFile).count();

            System.out.printf("  %-40s │ Prod: %2d │ Test: %2d │ Config: %2d%n",
                    truncate(entry.getKey(), 40), prod, tests, config);
        }

        System.out.println();
        System.out.printf("  Total: %d bloques de código%n", blocks.size());
    }

    private void showMainMenu() {
        System.out.println();
        System.out.println("┌──────────────────────────────────────────────────┐");
        System.out.printf("│  Bloque actual: %d de %d                          │%n",
                currentIndex + 1, blocks.size());
        System.out.println("├──────────────────────────────────────────────────┤");
        System.out.println("│  [1] Siguiente       [2] Anterior                │");
        System.out.println("│  [3] Ir a...         [4] Ver actual              │");
        System.out.println("│  [5] Generar TODO    [6] Generar restantes       │");
        System.out.println("│  [7] Listar          [8] Filtrar                 │");
        System.out.println("│  [9] Estado          [10] Generar README         │");
        System.out.println("│  [0] Salir           [h] Ayuda                   │");
        System.out.println("└──────────────────────────────────────────────────┘");
        System.out.print("Opción: ");
    }

    private void nextBlock() {
        if (currentIndex >= blocks.size() - 1) {
            ConsoleUtils.printWarning("Ya estás en el último bloque.");
            return;
        }

        currentIndex++;
        processCurrentBlock();
    }

    private void previousBlock() {
        if (currentIndex <= 0) {
            ConsoleUtils.printWarning("Ya estás en el primer bloque.");
            return;
        }

        currentIndex--;
        showCurrentBlock();
    }

    private void goToBlock() {
        System.out.print("Número de bloque (1-" + blocks.size() + "): ");
        try {
            int num = Integer.parseInt(readInput());
            if (num < 1 || num > blocks.size()) {
                ConsoleUtils.printError("Número fuera de rango.");
                return;
            }
            currentIndex = num - 1;
            showCurrentBlock();
        } catch (NumberFormatException e) {
            ConsoleUtils.printError("Número inválido.");
        }
    }

    private void showCurrentBlock() {
        CodeBlock block = blocks.get(currentIndex);

        ConsoleUtils.printPhase(block.getPhase());
        ConsoleUtils.printCodeBlock(block);
        ConsoleUtils.printCodePreview(block.getContent(), 15);

        if (generatedBlocks.contains(currentIndex)) {
            ConsoleUtils.printSuccess("Este archivo ya fue generado.");
        } else if (generator.fileExists(block)) {
            ConsoleUtils.printWarning("El archivo ya existe en disco.");
        }
    }

    private void processCurrentBlock() {
        CodeBlock block = blocks.get(currentIndex);

        ConsoleUtils.printPhase(block.getPhase());
        ConsoleUtils.printCodeBlock(block);
        ConsoleUtils.printCodePreview(block.getContent(), 10);

        if (generator.fileExists(block)) {
            try {
                String existing = generator.getExistingContent(block);
                if (existing != null) {
                    ConsoleUtils.printDiff(existing, block.getContent());
                }
            } catch (IOException e) {
                // Ignorar
            }
        }

        System.out.println();
        System.out.println("¿Qué deseas hacer?");
        System.out.println("  [g] Generar este archivo");
        System.out.println("  [s] Saltar");
        System.out.println("  [v] Ver código completo");
        System.out.println("  [b] Volver al menú");
        System.out.print("Opción: ");

        String input = readInput().toLowerCase();

        switch (input) {
            case "g":
            case "generate":
                generateCurrentBlock();
                break;
            case "s":
            case "skip":
                ConsoleUtils.printInfo("Saltando...");
                break;
            case "v":
            case "view":
                System.out.println();
                System.out.println("─".repeat(60));
                System.out.println(block.getContent());
                System.out.println("─".repeat(60));
                ConsoleUtils.pause();
                break;
            case "b":
            case "back":
                // volver
                break;
            default:
                ConsoleUtils.printWarning("Opción no válida.");
        }
    }

    private void generateCurrentBlock() {
        CodeBlock block = blocks.get(currentIndex);

        try {
            generator.generateFile(currentIndex);
            generatedBlocks.add(currentIndex);
            ConsoleUtils.printSuccess("Archivo generado: " + block.getFilePath());
        } catch (IOException e) {
            ConsoleUtils.printError("Error al generar: " + e.getMessage());
        }
    }

    private void generateAll() {
        System.out.print("¿Generar TODOS los archivos? (s/n): ");
        if (!readInput().toLowerCase().startsWith("s")) {
            ConsoleUtils.printInfo("Cancelado.");
            return;
        }

        ConsoleUtils.printSubHeader("Generando todos los archivos...");

        int success = 0;
        int skipped = 0;
        int errors = 0;

        for (int i = 0; i < blocks.size(); i++) {
            CodeBlock block = blocks.get(i);
            ConsoleUtils.printProgress(i + 1, blocks.size());

            if (block.getFilePath() == null) {
                skipped++;
                continue;
            }

            try {
                generator.generateFile(i);
                generatedBlocks.add(i);
                success++;
            } catch (IOException e) {
                errors++;
            }
        }

        System.out.println();
        System.out.println();
        ConsoleUtils.printSuccess(String.format(
                "Completado: %d generados, %d saltados, %d errores",
                success, skipped, errors));

        // Preguntar si generar README
        if (!readmeGenerated) {
            System.out.println();
            System.out.print("¿Generar README.md con la guía completa? (s/n): ");
            if (readInput().toLowerCase().startsWith("s")) {
                generateReadme();
            }
        }
    }

    private void generateRemaining() {
        List<Integer> remaining = new ArrayList<>();
        for (int i = 0; i < blocks.size(); i++) {
            if (!generatedBlocks.contains(i) && blocks.get(i).getFilePath() != null) {
                remaining.add(i);
            }
        }

        if (remaining.isEmpty()) {
            ConsoleUtils.printInfo("No hay bloques pendientes.");
            return;
        }

        System.out.printf("Hay %d bloques pendientes. ¿Generar? (s/n): ", remaining.size());
        if (!readInput().toLowerCase().startsWith("s")) {
            ConsoleUtils.printInfo("Cancelado.");
            return;
        }

        for (int i : remaining) {
            try {
                generator.generateFile(i);
                generatedBlocks.add(i);
                ConsoleUtils.printSuccess("Generado: " + blocks.get(i).getFileName());
            } catch (IOException e) {
                ConsoleUtils.printError("Error: " + blocks.get(i).getFileName());
            }
        }
    }

    private void generateReadme() {
        if (readmeGenerated) {
            ConsoleUtils.printWarning("README.md ya fue generado.");
            System.out.print("¿Regenerar? (s/n): ");
            if (!readInput().toLowerCase().startsWith("s")) {
                return;
            }
        }

        try {
            generator.generateReadme(markdownFile);
            readmeGenerated = true;
            ConsoleUtils.printSuccess("README.md generado con el contenido de la guía");
        } catch (IOException e) {
            ConsoleUtils.printError("Error al generar README: " + e.getMessage());
        }
    }

    private void listAllBlocks() {
        ConsoleUtils.printSubHeader("Lista de todos los bloques");

        String currentPhase = "";
        for (int i = 0; i < blocks.size(); i++) {
            CodeBlock block = blocks.get(i);

            if (!block.getPhase().equals(currentPhase)) {
                currentPhase = block.getPhase();
                System.out.println();
                ConsoleUtils.printPhase(currentPhase);
            }

            String status = generatedBlocks.contains(i) ? "✓" : " ";
            String type = block.getType().getShortName();

            System.out.printf("  [%s] %3d. [%s] %s%n",
                    status, i + 1, type, block.getFileName());
        }

        // Mostrar estado del README
        System.out.println();
        String readmeStatus = readmeGenerated ? "✓" : " ";
        System.out.printf("  [%s] README.md (guía completa)%n", readmeStatus);
    }

    private void filterBlocks() {
        System.out.println("Filtrar por:");
        System.out.println("  [1] Solo tests");
        System.out.println("  [2] Solo producción");
        System.out.println("  [3] Solo configuración");
        System.out.println("  [4] Por fase");
        System.out.print("Opción: ");

        String input = readInput();
        List<CodeBlock> filtered;

        switch (input) {
            case "1":
                filtered = new ArrayList<>();
                for (CodeBlock b : blocks) {
                    if (b.isTestFile()) filtered.add(b);
                }
                break;
            case "2":
                filtered = new ArrayList<>();
                for (CodeBlock b : blocks) {
                    if (b.isJavaFile() && !b.isTestFile()) filtered.add(b);
                }
                break;
            case "3":
                filtered = new ArrayList<>();
                for (CodeBlock b : blocks) {
                    if (b.isConfigFile()) filtered.add(b);
                }
                break;
            case "4":
                filtered = filterByPhase();
                break;
            default:
                ConsoleUtils.printWarning("Opción no válida.");
                filtered = new ArrayList<>();
        }

        if (!filtered.isEmpty()) {
            System.out.printf("%nEncontrados: %d bloques%n", filtered.size());
            for (CodeBlock block : filtered) {
                System.out.printf("  - %s (%s)%n", block.getFileName(), block.getPhase());
            }
        }
    }

    private List<CodeBlock> filterByPhase() {
        Set<String> phases = new LinkedHashSet<>();
        for (CodeBlock block : blocks) {
            phases.add(block.getPhase());
        }

        System.out.println("Fases disponibles:");
        List<String> phaseList = new ArrayList<>(phases);
        for (int i = 0; i < phaseList.size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, phaseList.get(i));
        }
        System.out.print("Número de fase: ");

        try {
            int num = Integer.parseInt(readInput());
            if (num < 1 || num > phaseList.size()) {
                return new ArrayList<>();
            }
            String selectedPhase = phaseList.get(num - 1);
            List<CodeBlock> result = new ArrayList<>();
            for (CodeBlock b : blocks) {
                if (b.getPhase().equals(selectedPhase)) {
                    result.add(b);
                }
            }
            return result;
        } catch (NumberFormatException e) {
            return new ArrayList<>();
        }
    }

    private void showStatus() {
        ConsoleUtils.printSubHeader("Estado del proyecto");

        int generated = generatedBlocks.size();
        int pending = 0;
        for (CodeBlock b : blocks) {
            if (b.getFilePath() != null) pending++;
        }
        pending -= generated;

        System.out.printf("  Archivos generados: %d%n", generated);
        System.out.printf("  Archivos pendientes: %d%n", pending);
        System.out.printf("  Total bloques: %d%n", blocks.size());
        System.out.printf("  README.md: %s%n", readmeGenerated ? "Generado ✓" : "Pendiente");
        System.out.println();

        // Mostrar por tipo
        Map<CodeBlock.BlockType, Integer> byType = new EnumMap<>(CodeBlock.BlockType.class);
        for (CodeBlock block : blocks) {
            byType.merge(block.getType(), 1, Integer::sum);
        }

        System.out.println("Por tipo:");
        for (Map.Entry<CodeBlock.BlockType, Integer> entry : byType.entrySet()) {
            System.out.printf("  %s: %d%n", entry.getKey().getDisplayName(), entry.getValue());
        }
    }

    private void showHelp() {
        ConsoleUtils.printSubHeader("Ayuda");
        System.out.println("""
            Este generador lee un archivo Markdown y extrae los bloques de código
            para generar la estructura del proyecto Java.
            
            Comandos rápidos:
              n, next       - Siguiente bloque
              p, prev       - Bloque anterior
              g, goto       - Ir a un bloque específico
              a, all        - Generar todos los archivos
              l, list       - Listar todos los bloques
              readme        - Generar README.md con la guía
              q, quit       - Salir
            
            El README.md contendrá el contenido completo del archivo Markdown
            original, sirviendo como documentación del proyecto.
            """);
    }

    private String readInput() {
        return scanner.nextLine().trim();
    }

    private static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}