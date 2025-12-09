// src/main/java/com/generator/ConsoleUtils.java

package com.generator;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Utilidades para la consola con soporte de colores.
 * Usa solo caracteres ASCII para compatibilidad.
 */
public class ConsoleUtils {

    private static boolean initialized = false;

    public static void init() {
        if (!initialized) {
            AnsiConsole.systemInstall();
            initialized = true;
        }
    }

    public static void cleanup() {
        if (initialized) {
            AnsiConsole.systemUninstall();
            initialized = false;
        }
    }

    public static void printHeader(String text) {
        System.out.println();
        System.out.println(ansi().fgBrightCyan().a(repeat("=", 70)).reset());
        System.out.println(ansi().fgBrightCyan().bold().a("  " + text).reset());
        System.out.println(ansi().fgBrightCyan().a(repeat("=", 70)).reset());
        System.out.println();
    }

    public static void printSubHeader(String text) {
        System.out.println();
        System.out.println(ansi().fgYellow().a(repeat("-", 50)).reset());
        System.out.println(ansi().fgYellow().bold().a("  " + text).reset());
        System.out.println(ansi().fgYellow().a(repeat("-", 50)).reset());
    }

    public static void printSuccess(String text) {
        System.out.println(ansi().fgBrightGreen().a("[OK] ").reset().a(text));
    }

    public static void printError(String text) {
        System.out.println(ansi().fgBrightRed().a("[ERROR] ").reset().a(text));
    }

    public static void printWarning(String text) {
        System.out.println(ansi().fgBrightYellow().a("[WARN] ").reset().a(text));
    }

    public static void printInfo(String text) {
        System.out.println(ansi().fgBrightBlue().a("[INFO] ").reset().a(text));
    }

    public static void printPhase(String phase) {
        System.out.println();
        System.out.println(ansi().fgBrightMagenta().bold().a(">> " + phase).reset());
    }

    public static void printCodeBlock(CodeBlock block) {
        Ansi.Color typeColor;
        switch (block.getType()) {
            case TEST_CODE:
                typeColor = Ansi.Color.GREEN;
                break;
            case PRODUCTION_CODE:
                typeColor = Ansi.Color.BLUE;
                break;
            case CONFIGURATION:
                typeColor = Ansi.Color.YELLOW;
                break;
            case RESOURCE:
                typeColor = Ansi.Color.CYAN;
                break;
            case SCRIPT:
                typeColor = Ansi.Color.MAGENTA;
                break;
            case OTHER:
            default:
                typeColor = Ansi.Color.WHITE;
                break;
        }

        System.out.println();
        System.out.println(ansi()
                .fg(typeColor)
                .a("+-- ")
                .bold()
                .a(block.getFileName())
                .reset()
                .fg(typeColor)
                .a(" [" + block.getType().getDisplayName() + "]")
                .reset());

        System.out.println(ansi()
                .fgBrightBlack()
                .a("|   Path: " + block.getPackagePath())
                .reset());

        if (!block.getDescription().isEmpty()) {
            System.out.println(ansi()
                    .fgBrightBlack()
                    .a("|   Desc: " + block.getDescription())
                    .reset());
        }

        System.out.println(ansi()
                .fg(typeColor)
                .a("+" + repeat("-", 60))
                .reset());
    }

    public static void printCodePreview(String code, int maxLines) {
        String[] lines = code.split("\n");
        int linesToShow = Math.min(lines.length, maxLines);

        System.out.println(ansi().fgBrightBlack().a("    +-- Codigo --").reset());

        for (int i = 0; i < linesToShow; i++) {
            String lineNum = String.format("%3d", i + 1);
            System.out.println(ansi()
                    .fgBrightBlack()
                    .a("    | " + lineNum + " | ")
                    .reset()
                    .a(truncate(lines[i], 55)));
        }

        if (lines.length > maxLines) {
            System.out.println(ansi()
                    .fgBrightBlack()
                    .a("    | ... (" + (lines.length - maxLines) + " lineas mas)")
                    .reset());
        }

        System.out.println(ansi().fgBrightBlack().a("    +" + repeat("-", 15)).reset());
    }

    public static void printProgress(int current, int total) {
        int percentage = (int) ((current / (double) total) * 100);
        int filled = percentage / 2;
        int empty = 50 - filled;

        StringBuilder bar = new StringBuilder();
        bar.append("[");
        for (int i = 0; i < filled; i++) bar.append("#");
        for (int i = 0; i < empty; i++) bar.append(".");
        bar.append("]");

        System.out.print(ansi()
                .saveCursorPosition()
                .eraseLine()
                .fgBrightBlue()
                .a("\r" + bar.toString() + " ")
                .reset()
                .a(String.format("%d%% (%d/%d)", percentage, current, total))
                .restoreCursorPosition());
    }

    public static void printMenu(String... options) {
        System.out.println();
        System.out.println(ansi().fgBright(Ansi.Color.WHITE).bold().a("Opciones:").reset());
        for (int i = 0; i < options.length; i++) {
            System.out.println(ansi()
                    .fgBrightCyan()
                    .a("  [" + (i + 1) + "] ")
                    .reset()
                    .a(options[i]));
        }
        System.out.println();
    }

    public static void printDiff(String existing, String newContent) {
        System.out.println(ansi()
                .fgBrightYellow()
                .a("    [WARN] El archivo ya existe. Diferencias:")
                .reset());

        String[] existingLines = existing.split("\n");
        String[] newLines = newContent.split("\n");

        int maxLines = Math.max(existingLines.length, newLines.length);
        int diffCount = 0;

        for (int i = 0; i < Math.min(maxLines, 5); i++) {
            String existLine = i < existingLines.length ? existingLines[i] : "";
            String newLine = i < newLines.length ? newLines[i] : "";

            if (!existLine.equals(newLine)) {
                diffCount++;
                System.out.println(ansi().fgRed().a("    - " + truncate(existLine, 50)).reset());
                System.out.println(ansi().fgGreen().a("    + " + truncate(newLine, 50)).reset());
            }
        }

        if (diffCount == 0) {
            System.out.println(ansi().fgGreen().a("    (Sin diferencias)").reset());
        } else if (maxLines > 5) {
            System.out.println(ansi().fgBrightBlack().a("    ... (mas diferencias)").reset());
        }
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    private static String repeat(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    public static void clearScreen() {
        System.out.print(ansi().eraseScreen().cursor(1, 1));
    }

    public static void pause() {
        System.out.println();
        System.out.print(ansi()
                .fgBrightBlack()
                .a("Presiona ENTER para continuar...")
                .reset());
        try {
            System.in.read();
        } catch (Exception ignored) {}
    }
}