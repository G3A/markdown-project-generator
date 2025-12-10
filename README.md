## Cómo Usar el Generador

### 1. Compilar el proyecto

```bash
mvn clean package
```

### 2. Ejemplo de preparación del archivo Markdown

Guarda la guía como `pruebas-para-el-desarrollo-de-software-guide-v1.0.1.md` en el directorio `guides/`.

### 2.1. Partes del comando

```bash
java -jar generator.jar guia.md ./proyecto-a-generar
```

### 3. Ejemplo de cómo ejecutar en modo debug

```bash
java -jar target/markdown-project-generator-1.0.0.jar guides/pruebas-para-el-desarrollo-de-software-guide-v1.0.1.md --debug
```

### 4. Ejemplo de cómo ejecutar en modo interactivo

```bash
java -jar target/markdown-project-generator-1.0.0.jar guides/pruebas-para-el-desarrollo-de-software-guide-v1.0.1.md ../pruebas-para-el-desarrollo-de-software-guide
```

### 5. Ejemplo de cómo ejecutar TODO TODO TODO en modo automático

```bash
java -jar target/markdown-project-generator-1.0.0.jar guides/pruebas-para-el-desarrollo-de-software-guide-v1.0.1.md ../pruebas-para-el-desarrollo-de-software-guide --auto
```


### 6. Ejemplo de cómo ejecutar otras opciones relacionadas con el archivo README.md

```bash
# Con README (por defecto)
java -jar target/markdown-project-generator-1.0.0.jar guides/pruebas-para-el-desarrollo-de-software-guide-v1.0.1.md ../pruebas-para-el-desarrollo-de-software-guide --auto
```

```bash
# Sin README
java -jar target/markdown-project-generator-1.0.0.jar guides/pruebas-para-el-desarrollo-de-software-guide-v1.0.1.md ../pruebas-para-el-desarrollo-de-software-guide --auto --no-readme
```

```bash
# Modo interactivo (pregunta si quieres generar README al final)
java -jar target/markdown-project-generator-1.0.0.jar guides/pruebas-para-el-desarrollo-de-software-guide-v1.0.1.md ../pruebas-para-el-desarrollo-de-software-guide

```

---

## Ejemplo de Sesión Interactiva

```
═══════════════════════════════════════════════════════════════════════
  MARKDOWN PROJECT GENERATOR v1.0.1
═══════════════════════════════════════════════════════════════════════

ℹ Procesando: /home/user/guides/pruebas-para-el-desarrollo-de-software-guide-v1.0.1.md
ℹ Salida: /home/user/pruebas-para-el-desarrollo-de-software-guide

📊 Resumen del proyecto:

  FASE 1: Configuración Base con TestContainers   │ Prod:  0 │ Test:  1 │ Config:  2
  FASE 2: ATDD con las 4 Capas de Dave Farley     │ Prod:  5 │ Test:  4 │ Config:  0
  FASE 3: TDD al estilo J.B. Rainsberger          │ Prod:  3 │ Test:  2 │ Config:  0
  FASE 4: Conectar Todo (El Endpoint)             │ Prod:  3 │ Test:  0 │ Config:  2

  Total: 22 bloques de código

┌─────────────────────────────────────────────┐
│  Bloque actual: 1 de 22                     │
├─────────────────────────────────────────────┤
│  [1] Siguiente    [2] Anterior              │
│  [3] Ir a...      [4] Ver actual            │
│  [5] Generar TODO [6] Generar restantes     │
│  [7] Listar       [8] Filtrar               │
│  [9] Estado       [0] Salir                 │
└─────────────────────────────────────────────┘
Opción: 1

▶ FASE 1: Configuración Base con TestContainers

┌─ pom.xml [Configuración]
│  Path: pom.xml
│  Desc: 1.1 Dependencias (pom.xml)
└────────────────────────────────────────────────────────────

    ┌── Código ──
    │   1 │ <?xml version="1.0" encoding="UTF-8"?>
    │   2 │ <project xmlns="http://maven.apache.org/POM/4.0.0"
    │   3 │          xmlns:xsi="http://www.w3.org/2001/XMLSchema-ins...
    │   4 │          xsi:schemaLocation="http://maven.apache.org/POM...
    │ ... (45 líneas más)
    └───────────────

¿Qué deseas hacer?
  [g] Generar este archivo
  [s] Saltar
  [v] Ver código completo
  [b] Volver al menú
Opción: g

✓ Archivo generado: pom.xml
```

---

## Script de Instalación Rápida

```bash
#!/bin/bash
# install.sh - Instala el generador

echo "📦 Instalando Markdown Project Generator..."

# Clonar o crear estructura
mkdir -p markdown-project-generator/src/main/java/com/generator
cd markdown-project-generator

# Crear archivos (asumiendo que ya tienes el contenido)
# ...

# Compilar
mvn clean package -q

# Crear alias
echo 'alias mdgen="java -jar '$PWD'/target/markdown-project-generator-1.0.0.jar"' >> ~/.bashrc

echo "✅ Instalación completa!"
echo "   Uso: mdgen <archivo.md> [directorio-salida]"
```

---

## Mejoras Opcionales

### Agregar soporte para más formatos

```java
// En MarkdownParser.java, agregar patrones para:
// - Archivos Kotlin (.kt)
// - Archivos Gradle (build.gradle)
// - Dockerfiles
// - Scripts SQL

private static final Map<String, String> LANGUAGE_EXTENSIONS = Map.of(
    "java", ".java",
    "kotlin", ".kt",
    "xml", ".xml",
    "yaml", ".yml",
    "sql", ".sql",
    "dockerfile", "Dockerfile"
);
```

### Agregar validación de sintaxis

```java
// Validar que el código Java compila antes de escribirlo
// Usar javax.tools.JavaCompiler
```

### Agregar plantillas personalizables

```java
// Permitir plantillas para código repetitivo
// ${package}, ${className}, ${date}, etc.
```