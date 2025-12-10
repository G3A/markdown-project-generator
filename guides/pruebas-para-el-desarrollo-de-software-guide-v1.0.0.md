# Diferencias entre BDD, ATDD y TDD en Java: Guía para Juniors versión 1.0.0 🚀

**Con Vertical Slice Architecture, TestContainers y el enfoque de Dave Farley y J.B. Rainsberger**

## 📚 Antes de Empezar: ¿Por qué deberías leer esto?

Imagina que estás construyendo una casa:
*   ¿Empezarías a poner ladrillos sin un plano? Probablemente no.
*   ¿Construirías todo y al final revisarías si funciona? Eso sería muy arriesgado.

En el desarrollo de software pasa lo mismo. Esta guía te enseña tres formas de asegurarte de que tu código funciona ANTES de tener problemas:

| Metodología | Analogía con construir una casa |
| :--- | :--- |
| **ATDD** | Asegurarte de que la casa cumple con lo que el cliente pidió. |
| **BDD** | Describir cómo se comportará la casa en lenguaje que todos entiendan. |
| **TDD** | Verificar que cada ladrillo esté bien puesto antes de continuar. |

## 🎯 Resumen Ejecutivo (La versión corta)

| Característica | TDD (Test-Driven Development) | ATDD (Acceptance Test-Driven Development) | BDD (Behavior-Driven Development) |
| :--- | :--- | :--- | :--- |
| **Pregunta principal** | "¿Funciona mi código?" | "¿Construí lo que me pidieron?" | "¿Se comporta como dijimos?" |
| **Nivel** | Bajo (una función, una clase) | Alto (el sistema completo) | Medio (comportamiento) |
| **¿Quién lo entiende?** | Solo desarrolladores | Todos (dev, QA, negocio) | Todos (dev, QA, negocio) |
| **Velocidad** | ⚡ Milisegundos | 🐢 Segundos | Depende de la implementación |

💡 **Tip para recordar:**
*   **TDD:** "¿Mi calculadora suma bien 2+2?"
*   **ATDD:** "¿El cliente puede comprar un producto en la tienda?"
*   **BDD:** "DADO un cliente VIP, CUANDO compra $200, ENTONCES recibe 10% de descuento"

---

## 🏪 Nuestro Escenario de Negocio (El problema que vamos a resolver)

Vamos a construir una funcionalidad para una tienda online:

> **Historia de Usuario:**
> Como cliente VIP de la tienda, quiero recibir un 10% de descuento cuando mi compra supere los $100, para ahorrar dinero en mis compras grandes.

### 📋 Criterios de Aceptación (Las reglas del juego)

Piensa en esto como las "reglas" que definen cuándo nuestro código está correcto:

```text
┌─────────────────────────────────────────────────────────────────┐
│  REGLA 1: Si es VIP Y compra más de $100 → 10% de descuento     │
│  REGLA 2: Si es VIP Y compra $100 o menos → SIN descuento       │
│  REGLA 3: Si NO es VIP → NUNCA hay descuento                    │
└─────────────────────────────────────────────────────────────────┘
```

### 🧮 Ejemplos concretos

| Cliente | Monto | ¿Descuento? | Total a pagar |
| :--- | :--- | :--- | :--- |
| VIP | $200 | Sí (10% = $20) | $180 |
| VIP | $100 | No | $100 |
| VIP | $99.99 | No | $99.99 |
| Normal | $500 | No | $500 |

---

## 📁 Estructura del Proyecto: ¿Dónde va cada cosa?

### 🤔 ¿Qué es "Vertical Slice Architecture"?

Es una forma de organizar tu código por funcionalidad en lugar de por tipo de archivo.

#### ❌ LA FORMA TRADICIONAL (que puede volverse confusa):

```text
src/
├── controllers/           ← Todos los controllers juntos
│   ├── CompraController.java
│   ├── UsuarioController.java
│   └── ProductoController.java
├── services/              ← Todos los services juntos
│   ├── CompraService.java
│   ├── UsuarioService.java
│   └── ProductoService.java
└── repositories/          ← Todos los repositories juntos
    ├── CompraRepository.java
    └── UsuarioRepository.java
```
*😰 Problema: Para entender "Compras" tienes que saltar entre 3 carpetas.*

#### ✅ VERTICAL SLICE (lo que usaremos):

```text
src/
├── features/
│   ├── procesarcompra/           ← TODO sobre compras está JUNTO
│   │   ├── ProcesarCompraEndpoint.java
│   │   ├── ProcesarCompraUseCase.java
│   │   ├── ClienteRepository.java
│   │   └── ... (más archivos de esta feature)
│   │
│   └── registrarusuario/         ← TODO sobre usuarios está JUNTO
│       ├── RegistrarUsuarioEndpoint.java
│       └── ... 
└── shared/                       ← Cosas que se comparten
    └── config/
```
*😊 Ventaja: Abres UNA carpeta y entiendes TODO sobre esa funcionalidad.*

### 📂 Estructura completa de nuestro proyecto

```text
src/
├── main/java/com/tienda/
│   ├── TiendaApplication.java          ← Punto de entrada (Spring Boot)
│   │
│   ├── features/
│   │   └── procesarcompra/             ← Nuestra funcionalidad
│   │       ├── ProcesarCompraEndpoint.java   (Recibe peticiones HTTP)
│   │       ├── ProcesarCompraUseCase.java    (Lógica de negocio)
│   │       ├── ClienteRepository.java        (Contrato/Interfaz)
│   │       ├── JpaClienteRepository.java     (Implementación con BD)
│   │       ├── Cliente.java                  (Entidad)
│   │       ├── SolicitudCompra.java          (Datos de entrada)
│   │       ├── Ticket.java                   (Datos de salida)
│   │       └── ClienteNoEncontradoException.java
│   │
│   └── shared/
│       └── config/
│           └── JpaConfig.java
│
└── test/java/com/tienda/
    ├── acceptance/                     ← Tests ATDD (sistema completo)
    │   ├── specs/                      (Las especificaciones)
    │   ├── dsl/                        (Lenguaje de dominio)
    │   ├── drivers/                    (Comunicación HTTP)
    │   └── infrastructure/             (Configuración)
    │
    └── features/
        └── procesarcompra/             
            ├── ProcesarCompraUseCaseTest.java ← Tests TDD (unitarios/colaboración)
            └── JpaClienteRepositoryContractTest.java ← Tests TDD (Por contrato)
```

---

## 🎭 PARTE 1: Las 4 Capas de Dave Farley para ATDD

**🤷 ¿Quién es Dave Farley?**
Dave Farley es uno de los autores de "Continuous Delivery". Su propuesta para tests de aceptación es separar en capas para que sean fáciles de mantener.

### 🧅 Las 4 Capas (como una cebolla)

Imagina que cada capa "envuelve" a la siguiente:

```text
┌─────────────────────────────────────────────────────────────────────────┐
│  CAPA 1: SPECIFICATION (Especificación) 📝                              │
│  ════════════════════════════════════════                               │
│  "Dado un cliente VIP, cuando compra $200, entonces paga $180"          │
│                                                                         │
│  👤 ¿Quién la lee? Product Owner, QA, cualquier persona                 │
│  🚫 NO menciona: HTTP, JSON, bases de datos, código técnico             │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  CAPA 2: DSL (Domain Specific Language) 🗣️                              │
│  ══════════════════════════════════════════                             │
│  clienteVipExiste("CLI-001")                                            │
│  realizarCompra("CLI-001", 200.00)                                      │
│  verificarTotalEs(180.00)                                               │
│                                                                         │
│  👤 ¿Quién la lee? Desarrolladores escribiendo tests                    │
│  ✅ Acciones de ALTO NIVEL, oculta detalles técnicos                    │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  CAPA 3: PROTOCOL DRIVER (Controlador de Protocolo) 🔌                  │
│  ═══════════════════════════════════════════════════                    │
│  POST /api/compras                                                      │
│  Body: {"clienteId": "CLI-001", "monto": 200}                          │
│  Headers: Content-Type: application/json                                │
│                                                                         │
│  👤 ¿Quién la lee? Desarrolladores                                      │
│  ⚙️ Conoce HTTP, JSON, URLs, pero NO la lógica de negocio               │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  CAPA 4: SYSTEM UNDER TEST (Sistema Bajo Prueba) 💻                     │
│  ══════════════════════════════════════════════════                     │
│  Spring Boot + PostgreSQL (en Docker con TestContainers)                │
│                                                                         │
│  🏃 El sistema REAL ejecutándose                                        │
└─────────────────────────────────────────────────────────────────────────┘
```

💡 **¿Por qué separar en capas?**
Imagina que cambias de HTTP a WebSockets:
*   **Sin capas:** Tendrías que modificar TODOS tus tests.
*   **Con capas:** Solo modificas la Capa 3 (Protocol Driver).

---

## 🧪 PARTE 2: TDD al estilo J.B. Rainsberger

**🤷 ¿Quién es J.B. Rainsberger?**
Es un experto en TDD que advierte sobre la "Trampa de los Tests Integrados": cuando TODOS tus tests necesitan base de datos, se vuelven lentos y frágiles.

### ⚡ Su solución: Dos tipos de tests

```text
┌─────────────────────────────────────────────────────────────────────────┐
│                       COLLABORATION TEST 🤝                             │
│  ═══════════════════════════════════════                                │
│                                                                         │
│  📌 ¿Qué prueba? La LÓGICA DE NEGOCIO                                   │
│  🎭 Dependencias: MOCKS (objetos simulados)                             │
│  ⚡ Velocidad: MILISEGUNDOS (súper rápido)                              │
│  🚫 NO usa: Base de datos, HTTP, archivos                               │
│                                                                         │
│  Ejemplo de pregunta que responde:                                      │
│  "¿Calcula bien el 10% de descuento para un VIP con $200?"              │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                        CONTRACT TEST 📜                                 │
│  ══════════════════════════════════════                                 │
│                                                                         │
│  📌 ¿Qué prueba? Que la IMPLEMENTACIÓN cumple el CONTRATO               │
│  🐘 Dependencias: Base de datos REAL (en Docker)                        │
│  🐢 Velocidad: SEGUNDOS (porque levanta Docker)                         │
│  ✅ SÍ usa: PostgreSQL real con TestContainers                          │
│                                                                         │
│  Ejemplo de pregunta que responde:                                      │
│  "¿Puedo guardar un cliente y recuperarlo por ID?"                      │
└─────────────────────────────────────────────────────────────────────────┘
```

#### 🤔 ¿Por qué dos tipos?

| Situación | Collaboration Test | Contract Test |
| :--- | :--- | :--- |
| Lógica incorrecta (mal cálculo) | ✅ Lo detecta | ❌ No lo detecta |
| Query SQL mal escrita | ❌ No lo detecta | ✅ Lo detecta |
| Mapeo JPA incorrecto | ❌ No lo detecta | ✅ Lo detecta |
| Velocidad de ejecución | ⚡ 100+ tests/segundo | 🐢 ~1 test/segundo |

---

## ⚙️ FASE 1: Configuración del Proyecto

### 1.1 📦 Dependencias (pom.xml)

💡 **¿Qué es un pom.xml?** Es el archivo donde Maven (nuestra herramienta de construcción) sabe qué librerías necesita tu proyecto.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <!-- 👆 Heredamos configuración de Spring Boot -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.0</version>
        <relativePath/>
    </parent>
    
    <!-- 📝 Información de nuestro proyecto -->
    <groupId>com.tienda</groupId>
    <artifactId>tienda-vip</artifactId>
    <version>1.0.0</version>
    <name>tienda-vip</name>
    <description>Ejemplo de BDD, ATDD y TDD en Java</description>
    
    <properties>
        <java.version>21</java.version>
    </properties>
    
    <dependencies>
        <!-- 🌐 SPRING BOOT: El framework web que usaremos -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- 🐘 POSTGRESQL: Nuestra base de datos -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- 📅 JACKSON: Para manejar fechas en JSON -->
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
        </dependency>

        <!-- 🧪 TESTING: Todo lo relacionado con tests -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test-classic</artifactId>
            <scope>test</scope>
        </dependency>
        
        <!-- 🐳 TestContainers: Levanta Docker automáticamente para tests -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-testcontainers</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers-junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers-postgresql</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- ✅ AssertJ: Para escribir assertions más legibles -->
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### 1.2 🏠 Clase Base para Tests de Aceptación

💡 **¿Qué es una clase base?** Es una clase de la que otras clases "heredan". Así no repetimos código en cada test.

```java
// 📁 src/test/java/com/tienda/acceptance/infrastructure/AcceptanceTestBase.java

package com.tienda.acceptance.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 🏗️ CLASE BASE PARA TESTS DE ACEPTACIÓN
 * 
 * Esta clase se encarga de:
 * 1. Levantar un PostgreSQL en Docker (automáticamente)
 * 2. Iniciar nuestra aplicación Spring Boot
 * 3. Configurar un cliente HTTP para hacer peticiones
 */
@Testcontainers  // 👈 Le dice a JUnit que use TestContainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// 👆 Levanta Spring Boot en un puerto aleatorio (evita conflictos)
@ActiveProfiles("test")  // 👈 Usa application-test.yml para configuración
public abstract class AcceptanceTestBase {

    /**
     * 🐳 CONTENEDOR DE POSTGRESQL
     * TestContainers levantará este contenedor Docker automáticamente.
     */
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:alpine")
            .withDatabaseName("tienda_test")  // Nombre de la base de datos
            .withUsername("test")              // Usuario
            .withPassword("test");             // Contraseña

    /**
     * 🔧 CONFIGURACIÓN DINÁMICA
     * Le dice a Spring Boot cómo conectarse al PostgreSQL que TestContainers acaba de levantar.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @LocalServerPort
    int port;

    protected RestClient restClient;

    @BeforeEach
    void setupClient() {
        this.restClient = RestClient.create("http://localhost:" + port);
    }
}
```

### 1.3 🚀 Clase Principal de la Aplicación

```java
// 📁 src/main/java/com/tienda/TiendaApplication.java

package com.tienda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TiendaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TiendaApplication.class, args);
    }
}
```

---

## 🎬 FASE 2: ATDD - Tests de Aceptación (4 Capas de Dave Farley)

### CAPA 1: Specification (La que lee el Product Owner) 📝

💡 **Regla de oro:** Esta clase NO debe tener NINGÚN detalle técnico. Si le muestras este código a alguien de negocio, debería entenderlo.

```java
// 📁 src/test/java/com/tienda/acceptance/specs/ProcesarCompraSpec.java

package com.tienda.acceptance.specs;

import com.tienda.acceptance.drivers.CompraHttpDriver;
import com.tienda.acceptance.dsl.CompraDsl;
import com.tienda.acceptance.infrastructure.AcceptanceTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 📋 CAPA 1: SPECIFICATION (Especificación)
 *
 * 👤 ¿Quién la lee? Product Owner, QA, stakeholders.
 * 🎯 REGLA DE ORO: No debe haber NINGÚN detalle técnico aquí.
 * ❌ NO: HTTP, JSON, SQL, endpoints.
 * ✅ SÍ: Lenguaje de NEGOCIO puro (Given/When/Then).
 */
@DisplayName("Feature: Procesar Compra con Descuento VIP")
class ProcesarCompraSpecTest extends AcceptanceTestBase {

    private CompraDsl compra;

    @BeforeEach
    void setUp() {
        CompraHttpDriver driver = new CompraHttpDriver(restClient);
        compra = new CompraDsl(driver);
        compra.dadoQueNoHayClientes();
    }

    // 📌 ESCENARIO 1: Cliente VIP compra más de $100
    @Nested
    @DisplayName("Escenario: Cliente VIP compra más de $100")
    class ClienteVipCompraGrande {

        @Test
        @DisplayName("Entonces recibe 10% de descuento")
        void debeAplicarDescuento() {
            // GIVEN
            compra.dadoUnClienteVip("CLI-001", "Juan Pérez");

            // WHEN
            compra.cuandoRealizaUnaCompraDe("CLI-001", 200.00);

            // THEN
            compra
                    .entoncesLaCompraEsExitosa()
                    .entoncesSeAplicoDescuento()
                    .entoncesElTotalEs(180.00);  // $200 - 10% = $180
        }
    }

    // 📌 ESCENARIO 2: Cliente VIP con compra pequeña
    @Nested
    @DisplayName("Escenario: Cliente VIP compra $100 o menos")
    class ClienteVipCompraPequena {

        @Test
        @DisplayName("Entonces NO recibe descuento")
        void noDebeAplicarDescuento() {
            compra.dadoUnClienteVip("CLI-002", "María García");
            compra.cuandoRealizaUnaCompraDe("CLI-002", 100.00);
            compra
                    .entoncesLaCompraEsExitosa()
                    .entoncesNoSeAplicoDescuento()
                    .entoncesElDescuentoEs(0.00)
                    .entoncesElTotalEs(100.00);
        }

        @Test
        @DisplayName("Caso borde: $99.99 no recibe descuento")
        void casoBordeNoRecibeDescuento() {
            compra.dadoUnClienteVip("CLI-003", "Pedro López");
            compra.cuandoRealizaUnaCompraDe("CLI-003", 99.99);
            compra
                    .entoncesLaCompraEsExitosa()
                    .entoncesNoSeAplicoDescuento()
                    .entoncesElTotalEs(99.99);
        }
    }

    // 📌 ESCENARIO 3: Cliente Normal
    @Nested
    @DisplayName("Escenario: Cliente Normal (no VIP)")
    class ClienteNormal {

        @Test
        @DisplayName("Nunca recibe descuento, sin importar el monto")
        void nuncaRecibeDescuento() {
            compra.dadoUnClienteNormal("CLI-004", "Paula Torres");
            compra.cuandoRealizaUnaCompraDe("CLI-004", 500.00);
            compra
                    .entoncesLaCompraEsExitosa()
                    .entoncesNoSeAplicoDescuento()
                    .entoncesElTotalEs(500.00);
        }
    }

    // 📌 ESCENARIO 4: Casos de Error
    @Nested
    @DisplayName("Escenario: Casos de Error")
    class CasosDeError {

        @Test
        @DisplayName("Cliente no existe en el sistema")
        void clienteNoExiste() {
            compra.cuandoRealizaUnaCompraDe("CLIENTE-FANTASMA", 200.00);
            compra.entoncesElClienteNoFueEncontrado();
        }

        @Test
        @DisplayName("Monto de compra inválido (negativo)")
        void montoInvalido() {
            compra.dadoUnClienteVip("CLI-005", "Luis Ramírez");
            compra.cuandoRealizaUnaCompraDe("CLI-005", -50.00);
            compra.entoncesHayUnErrorDeValidacion();
        }
    }
}
```

🔍 **¿Notaste algo?**
El código de arriba no menciona HTTP, REST, JSON ni SQL. Solo usa lenguaje de negocio.

### CAPA 2: DSL (Domain Specific Language) 🗣️

💡 **¿Qué es un DSL?** Es un "mini-lenguaje" creado específicamente para nuestro dominio de negocio.

```java
// 📁 src/test/java/com/tienda/acceptance/dsl/CompraDsl.java

package com.tienda.acceptance.dsl;

import com.tienda.acceptance.drivers.CompraHttpDriver;
import com.tienda.features.procesarcompra.Cliente;
import com.tienda.features.procesarcompra.Ticket;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

public class CompraDsl {

    private final CompraHttpDriver driver;

    public CompraDsl(CompraHttpDriver driver) {
        this.driver = driver;
    }

    // 📖 GIVEN
    public CompraDsl dadoQueNoHayClientes() {
        driver.limpiarClientes();
        return this;
    }

    public CompraDsl dadoUnClienteVip(String id, String nombre) {
        Cliente cliente = new Cliente(id, nombre, true);
        driver.crearCliente(cliente);
        return this;
    }

    public CompraDsl dadoUnClienteNormal(String id, String nombre) {
        Cliente cliente = new Cliente(id, nombre, false);
        driver.crearCliente(cliente);
        return this;
    }

    // 📖 WHEN
    public CompraDsl cuandoRealizaUnaCompraDe(String clienteId, double monto) {
        driver.realizarCompra(clienteId, monto);
        return this;
    }

    // 📖 THEN
    public CompraDsl entoncesLaCompraEsExitosa() {
        assertTrue(driver.fueExitoso(), "La compra debería ser exitosa");
        return this;
    }

    public CompraDsl entoncesElTotalEs(double totalEsperado) {
        Ticket ticket = driver.obtenerTicket();
        assertEquals(totalEsperado, ticket.getTotal(), 0.01, "El total del ticket no coincide");
        return this;
    }

    public CompraDsl entoncesElDescuentoEs(double descuentoEsperado) {
        Ticket ticket = driver.obtenerTicket();
        assertEquals(descuentoEsperado, ticket.getDescuento(), 0.01, "El descuento no coincide");
        return this;
    }

    public CompraDsl entoncesSeAplicoDescuento() {
        Ticket ticket = driver.obtenerTicket();
        assertTrue(ticket.isDescuentoAplicado(), "Debería haberse aplicado descuento");
        return this;
    }

    public CompraDsl entoncesNoSeAplicoDescuento() {
        Ticket ticket = driver.obtenerTicket();
        assertFalse(ticket.isDescuentoAplicado(), "No debería haberse aplicado descuento");
        return this;
    }

    public CompraDsl entoncesElClienteNoFueEncontrado() {
        assertEquals(HttpStatus.NOT_FOUND, driver.obtenerCodigoEstado(), "Debería retornar 404 Not Found");
        return this;
    }

    public CompraDsl entoncesHayUnErrorDeValidacion() {
        assertEquals(HttpStatus.BAD_REQUEST, driver.obtenerCodigoEstado(), "Debería retornar 400 Bad Request");
        return this;
    }
}
```

💡 **Patrón "Fluent Interface":** Permite encadenar llamadas (`.dado().cuando().entonces()`).

### CAPA 3: Protocol Driver (El que hace HTTP) 🔌

💡 Esta es la capa "sucia": aquí sí hay detalles técnicos como URLs, JSON, headers.

```java
// 📁 src/test/java/com/tienda/acceptance/drivers/CompraHttpDriver.java

package com.tienda.acceptance.drivers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tienda.features.procesarcompra.Cliente;
import com.tienda.features.procesarcompra.SolicitudCompra;
import com.tienda.features.procesarcompra.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

public class CompraHttpDriver {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private ResponseEntity<String> ultimaRespuesta;

    public CompraHttpDriver(RestClient restClient) {
        this.restClient = restClient;
        this.objectMapper = new ObjectMapper();
    }

    // 🔧 CONFIGURACIÓN
    public void crearCliente(Cliente cliente) {
        restClient.post()
                .uri("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(cliente)
                .retrieve()
                .toBodilessEntity();
    }

    public void limpiarClientes() {
        restClient.delete()
                .uri("/api/clientes")
                .retrieve()
                .toBodilessEntity();
    }

    // 💼 NEGOCIO
    public void realizarCompra(String clienteId, double monto) {
        SolicitudCompra solicitud = new SolicitudCompra(clienteId, monto);

        this.ultimaRespuesta = restClient.post()
                .uri("/api/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .body(solicitud)
                .retrieve()
                .onStatus(status -> true, (req, resp) -> { }) // Ignorar errores
                .toEntity(String.class);
    }

    // 🔍 VERIFICACIÓN
    public Ticket obtenerTicket() {
        if (ultimaRespuesta.getStatusCode().is2xxSuccessful() && ultimaRespuesta.getBody() != null) {
            try {
                return objectMapper.readValue(ultimaRespuesta.getBody(), Ticket.class);
            } catch (Exception e) {
                throw new RuntimeException("Error al leer el Ticket", e);
            }
        }
        return null;
    }

    public HttpStatus obtenerCodigoEstado() {
        return (HttpStatus) ultimaRespuesta.getStatusCode();
    }

    public boolean fueExitoso() {
        return ultimaRespuesta.getStatusCode().is2xxSuccessful();
    }
}
```

### CAPA 4: System Under Test (Nuestro código real) 💻

Definimos las clases del dominio:

**Cliente (Entidad JPA)**
```java
// 📁 src/main/java/com/tienda/features/procesarcompra/Cliente.java
package com.tienda.features.procesarcompra;
import jakarta.persistence.*;

@Entity
@Table(name = "clientes")
public class Cliente {
    @Id private String id;
    @Column(nullable = false) private String nombre;
    @Column(nullable = false) private boolean vip;

    protected Cliente() {}

    public Cliente(String id, String nombre, boolean vip) {
        this.id = id;
        this.nombre = nombre;
        this.vip = vip;
    }
    // Getters...
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public boolean isVip() { return vip; }
}
```

**SolicitudCompra y Ticket (DTOs)**
```java
// 📁 src/main/java/com/tienda/features/procesarcompra/SolicitudCompra.java
package com.tienda.features.procesarcompra;

public class SolicitudCompra {
    private String clienteId;
    private double monto;

    public SolicitudCompra() {}
    public SolicitudCompra(String clienteId, double monto) {
        this.clienteId = clienteId;
        this.monto = monto;
    }
    // Getters y Setters...
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
}
```

```java
// 📁 src/main/java/com/tienda/features/procesarcompra/Ticket.java
package com.tienda.features.procesarcompra;

public class Ticket {

    private String clienteId;
    private double montoOriginal;
    private double descuento;
    private double total;
    private boolean descuentoAplicado;

    public Ticket() {}

    public Ticket(String clienteId, double montoOriginal, double descuento, double total) {
        this.clienteId = clienteId;
        this.montoOriginal = montoOriginal;
        this.descuento = descuento;
        this.total = total;
        this.descuentoAplicado = descuento > 0;
    }

    // Getters
    public String getClienteId() { return clienteId; }
    public double getMontoOriginal() { return montoOriginal; }
    public double getDescuento() { return descuento; }
    public double getTotal() { return total; }
    public boolean isDescuentoAplicado() { return descuentoAplicado; }

    // Setters para Jackson
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public void setMontoOriginal(double montoOriginal) { this.montoOriginal = montoOriginal; }
    public void setDescuento(double descuento) { this.descuento = descuento; }
    public void setTotal(double total) { this.total = total; }
    public void setDescuentoAplicado(boolean descuentoAplicado) { this.descuentoAplicado = descuentoAplicado; }
}
```

🔴 **Estado Actual: TODOS LOS TESTS FALLAN**

```text
ProcesarCompraSpec
├── Cliente VIP compra más de $100        ❌ 404 Not Found
├── Cliente VIP compra $100 o menos       ❌ 404 Not Found
├── Cliente Normal (no VIP)               ❌ 404 Not Found
└── Casos de Error                        ❌ 404 Not Found
```
💡 *¡Esto es NORMAL! En ATDD, primero escribimos una prueba de aceptación a la vez que falla.*

---

## 🧪 FASE 3: TDD - Collaboration Tests (Lógica de Negocio)

### 3.1 El Contrato (La Interfaz del Repositorio)

```java
// 📁 src/main/java/com/tienda/features/procesarcompra/ClienteRepository.java
package com.tienda.features.procesarcompra;

import java.util.Optional;

/**
 * CONTRATO del Repositorio.
 *
 * El Use Case depende de esta INTERFAZ, no de la implementación JPA.
 * Esto permite:
 * - Mockear en Collaboration Tests
 * - Cambiar la implementación (JPA, MongoDB, In-Memory) sin tocar la lógica
 */
public interface ClienteRepository {

    void guardar(Cliente cliente);

    Optional<Cliente> buscarPorId(String id);

    void eliminarTodos();
}
```

### 3.2 Collaboration Test (Probando la lógica aislada)

💡 Este test NO usa base de datos. Usa "mocks".

```java
// 📁 src/test/java/com/tienda/features/procesarcompra/ProcesarCompraUseCaseTest.java
package com.tienda.features.procesarcompra;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * COLLABORATION TEST
 *
 * Prueba la LÓGICA DE NEGOCIO del Use Case.
 *
 * - NO hay I/O real (no base de datos, no HTTP)
 * - Usamos MOCKS para simular las dependencias
 * - Ejecuta en MILISEGUNDOS
 *
 * Pregunta: "¿La lógica del cálculo de descuento es correcta?"
 */
@DisplayName("ProcesarCompraUseCase - Collaboration Tests")
class ProcesarCompraUseCaseTest {

    private ClienteRepository repositoryMock;
    private ProcesarCompraUseCase useCase;

    @BeforeEach
    void setUp() {
        repositoryMock = mock(ClienteRepository.class);
        useCase = new ProcesarCompraUseCase(repositoryMock);
    }

    // ══════════════════════════════════════════════════════════════════
    // REGLA: VIP + monto > 100 = 10% descuento
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Cuando el cliente es VIP y compra más de $100")
    class ClienteVipCompraGrande {

        @Test
        @DisplayName("Debe calcular 10% de descuento")
        void debeCalcularDescuento() {
            // ARRANGE
            Cliente vip = new Cliente("VIP-001", "Juan", true);
            when(repositoryMock.buscarPorId("VIP-001")).thenReturn(Optional.of(vip));

            // ACT
            Ticket ticket = useCase.ejecutar("VIP-001", 200.00);

            // ASSERT
            assertAll(
                    () -> assertEquals(200.00, ticket.getMontoOriginal()),
                    () -> assertEquals(20.00, ticket.getDescuento()),
                    () -> assertEquals(180.00, ticket.getTotal()),
                    () -> assertTrue(ticket.isDescuentoAplicado())
            );
        }

        @Test
        @DisplayName("Caso borde: $100.01 SÍ recibe descuento")
        void casoBordeSiRecibeDescuento() {
            Cliente vip = new Cliente("VIP-002", "María", true);
            when(repositoryMock.buscarPorId("VIP-002")).thenReturn(Optional.of(vip));

            Ticket ticket = useCase.ejecutar("VIP-002", 100.01);

            assertTrue(ticket.isDescuentoAplicado());

            assertEquals(10.001, ticket.getDescuento(), 0.001);
        }

        @Test
        @DisplayName("Monto grande: $10,000 = $1,000 de descuento")
        void montoMuyGrande() {
            Cliente vip = new Cliente("VIP-003", "Pedro", true);
            when(repositoryMock.buscarPorId("VIP-003")).thenReturn(Optional.of(vip));

            Ticket ticket = useCase.ejecutar("VIP-003", 10000.00);

            assertEquals(1000.00, ticket.getDescuento());
            assertEquals(9000.00, ticket.getTotal());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // REGLA: VIP + monto <= 100 = SIN descuento
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Cuando el cliente es VIP pero compra $100 o menos")
    class ClienteVipCompraPequena {

        @Test
        @DisplayName("NO debe aplicar descuento")
        void noDebeAplicarDescuento() {
            Cliente vip = new Cliente("VIP-004", "Paula", true);
            when(repositoryMock.buscarPorId("VIP-004")).thenReturn(Optional.of(vip));

            Ticket ticket = useCase.ejecutar("VIP-004", 100.00);

            assertAll(
                    () -> assertEquals(100.00, ticket.getTotal()),
                    () -> assertEquals(0.00, ticket.getDescuento()),
                    () -> assertFalse(ticket.isDescuentoAplicado())
            );
        }

        @Test
        @DisplayName("Caso borde: $99.99 NO recibe descuento")
        void casoBordeNoRecibeDescuento() {
            Cliente vip = new Cliente("VIP-005", "Luis", true);
            when(repositoryMock.buscarPorId("VIP-005")).thenReturn(Optional.of(vip));

            Ticket ticket = useCase.ejecutar("VIP-005", 99.99);

            assertFalse(ticket.isDescuentoAplicado());
            assertEquals(99.99, ticket.getTotal());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // REGLA: NO VIP = NUNCA descuento
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Cuando el cliente NO es VIP")
    class ClienteNormal {

        @Test
        @DisplayName("Nunca recibe descuento, sin importar el monto")
        void nuncaRecibeDescuento() {
            Cliente normal = new Cliente("NOR-001", "Carlos", false);
            when(repositoryMock.buscarPorId("NOR-001")).thenReturn(Optional.of(normal));

            Ticket ticket = useCase.ejecutar("NOR-001", 1000.00);

            assertAll(
                    () -> assertEquals(1000.00, ticket.getTotal()),
                    () -> assertEquals(0.00, ticket.getDescuento()),
                    () -> assertFalse(ticket.isDescuentoAplicado())
            );
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // VALIDACIONES Y ERRORES
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Validaciones de entrada")
    class Validaciones {

        @Test
        @DisplayName("Cliente no existe: lanza excepción")
        void clienteNoExiste() {
            when(repositoryMock.buscarPorId("FANTASMA")).thenReturn(Optional.empty());

            assertThrows(ClienteNoEncontradoException.class, () -> {
                useCase.ejecutar("FANTASMA", 100.00);
            });
        }

        @Test
        @DisplayName("Monto negativo: lanza excepción")
        void montoNegativo() {
            Cliente cliente = new Cliente("CLI-001", "Test", true);
            when(repositoryMock.buscarPorId("CLI-001")).thenReturn(Optional.of(cliente));

            assertThrows(IllegalArgumentException.class, () -> {
                useCase.ejecutar("CLI-001", -50.00);
            });
        }

        @Test
        @DisplayName("Monto cero: lanza excepción")
        void montoCero() {
            Cliente cliente = new Cliente("CLI-002", "Test", true);
            when(repositoryMock.buscarPorId("CLI-002")).thenReturn(Optional.of(cliente));

            assertThrows(IllegalArgumentException.class, () -> {
                useCase.ejecutar("CLI-002", 0.00);
            });
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // COLABORACIONES (Verificar interacciones con el mock)
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Colaboraciones con el repositorio")
    class Colaboraciones {

        @Test
        @DisplayName("Debe buscar el cliente por ID")
        void debeBuscarCliente() {
            Cliente cliente = new Cliente("CLI-X", "Test", true);
            when(repositoryMock.buscarPorId("CLI-X")).thenReturn(Optional.of(cliente));

            useCase.ejecutar("CLI-X", 150.00);

            verify(repositoryMock).buscarPorId("CLI-X");
        }

        @Test
        @DisplayName("Si el cliente no existe, NO debe intentar calcular")
        void noDebeCalcularSiNoExiste() {
            when(repositoryMock.buscarPorId("NO-EXISTE")).thenReturn(Optional.empty());

            assertThrows(ClienteNoEncontradoException.class, () -> {
                useCase.ejecutar("NO-EXISTE", 100.00);
            });

            // Verificar que se intentó buscar
            verify(repositoryMock).buscarPorId("NO-EXISTE");
            // Verificar que no se hizo nada más
            verifyNoMoreInteractions(repositoryMock);
        }
    }
}
```

### 3.3 Implementación del Use Case 🟢

```java
// 📁 src/main/java/com/tienda/features/procesarcompra/ProcesarCompraUseCase.java
package com.tienda.features.procesarcompra;

import org.springframework.stereotype.Service;

@Service
public class ProcesarCompraUseCase {

    private static final double PORCENTAJE_DESCUENTO = 0.10;
    private static final double MONTO_MINIMO_PARA_DESCUENTO = 100.00;
    private final ClienteRepository clienteRepository;

    public ProcesarCompraUseCase(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Ticket ejecutar(String clienteId, double monto) {
        validarMonto(monto);
        Cliente cliente = buscarCliente(clienteId);
        double descuento = calcularDescuento(cliente, monto);
        double total = monto - descuento;
        return new Ticket(clienteId, monto, descuento, total);
    }

    private void validarMonto(double monto) {
        if (monto <= 0) throw new IllegalArgumentException("El monto debe ser mayor a cero");
    }

    private Cliente buscarCliente(String clienteId) {
        return clienteRepository.buscarPorId(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoException(clienteId));
    }

    private double calcularDescuento(Cliente cliente, double monto) {
        if (cliente.isVip() && monto > MONTO_MINIMO_PARA_DESCUENTO) {
            return monto * PORCENTAJE_DESCUENTO;
        }
        return 0.00;
    }
}
```
*Necesitas también la clase `ClienteNoEncontradoException`.*

🟢 **Estado: Collaboration Tests PASAN**

### 3.4 Contract Test (Repositorio con Base de Datos Real)

💡 Verifica que el código JPA funciona contra PostgreSQL real.

#### Paso 1: El Contrato Abstracto

```java
// 📁 src/test/java/com/tienda/features/procesarcompra/ClienteRepositoryContract.java
package com.tienda.features.procesarcompra;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CONTRACT TEST (Abstracto)
 *
 * Define QUÉ debe cumplir CUALQUIER implementación de ClienteRepository.
 *
 * Las subclases proporcionan la implementación concreta:
 * - JpaClienteRepositoryTest → Usa PostgreSQL real
 * - InMemoryClienteRepositoryTest → Usa HashMap (para tests rápidos)
 */
public abstract class ClienteRepositoryContract {

    protected abstract ClienteRepository crearRepositorio();

    protected abstract void limpiarDatos();

    protected ClienteRepository repository;

    @BeforeEach
    void setUp() {
        repository = crearRepositorio();
        limpiarDatos();
    }

    // ══════════════════════════════════════════════════════════════════
    // CONTRATO: Operación GUARDAR
    // ══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Debe guardar un cliente y recuperarlo por ID")
    void debeGuardarYRecuperar() {
        // GIVEN
        Cliente cliente = new Cliente("CLI-001", "Juan Pérez", true);

        // WHEN
        repository.guardar(cliente);
        Optional<Cliente> encontrado = repository.buscarPorId("CLI-001");

        // THEN
        assertTrue(encontrado.isPresent(), "El cliente debería existir");
        assertEquals("Juan Pérez", encontrado.get().getNombre());
        assertTrue(encontrado.get().isVip());
    }

    @Test
    @DisplayName("Debe guardar múltiples clientes")
    void debeGuardarMultiples() {
        repository.guardar(new Cliente("CLI-001", "Juan", true));
        repository.guardar(new Cliente("CLI-002", "María", false));

        assertTrue(repository.buscarPorId("CLI-001").isPresent());
        assertTrue(repository.buscarPorId("CLI-002").isPresent());
    }

    // ══════════════════════════════════════════════════════════════════
    // CONTRATO: Operación BUSCAR
    // ══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Debe retornar Optional vacío si el cliente no existe")
    void debeRetornarVacioSiNoExiste() {
        Optional<Cliente> resultado = repository.buscarPorId("NO-EXISTE");

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Debe distinguir entre clientes VIP y normales")
    void debeDistinguirVip() {
        repository.guardar(new Cliente("VIP-001", "VIP User", true));
        repository.guardar(new Cliente("NOR-001", "Normal User", false));

        Cliente vip = repository.buscarPorId("VIP-001").orElseThrow();
        Cliente normal = repository.buscarPorId("NOR-001").orElseThrow();

        assertTrue(vip.isVip());
        assertFalse(normal.isVip());
    }

    // ══════════════════════════════════════════════════════════════════
    // CONTRATO: Operación ELIMINAR TODOS
    // ══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Debe eliminar todos los clientes")
    void debeEliminarTodos() {
        repository.guardar(new Cliente("CLI-001", "Juan", true));
        repository.guardar(new Cliente("CLI-002", "María", false));

        repository.eliminarTodos();

        assertTrue(repository.buscarPorId("CLI-001").isEmpty());
        assertTrue(repository.buscarPorId("CLI-002").isEmpty());
    }
}
```

#### Paso 2: Test Concreto con TestContainers

```java
// 📁 src/test/java/com/tienda/features/procesarcompra/JpaClienteRepositoryContractTest.java
package com.tienda.features.procesarcompra;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Properties;

/**
 * CONTRACT TEST (Concreto)
 *
 * Prueba que JpaClienteRepository cumple el contrato de ClienteRepository.
 *
 * USA:
 * - PostgreSQL REAL en Docker (TestContainers)
 * - Hibernate/JPA real
 *
 * VERIFICA:
 * - Mapeo @Entity correcto
 * - Queries JPQL funcionan
 * - Transacciones funcionan
 */
@Testcontainers
@DisplayName("JpaClienteRepository - Contract Test")
class JpaClienteRepositoryContractTest extends ClienteRepositoryContract {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:alpine")
            .withDatabaseName("tienda_test")
            .withUsername("test")
            .withPassword("test");

    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    @BeforeAll
    static void initHibernate() {
        Properties props = new Properties();
        props.put("hibernate.connection.url", postgres.getJdbcUrl());
        props.put("hibernate.connection.username", postgres.getUsername());
        props.put("hibernate.connection.password", postgres.getPassword());
        props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.hbm2ddl.auto", "create-drop");
        props.put("hibernate.show_sql", "true");
        // 🔴 FIX CRÍTICO: Desactivar auto-commit para que la transacción manual no se cierre sola
        props.put("hibernate.connection.autocommit", "false");

        Configuration config = new Configuration();
        config.addProperties(props);
        config.addAnnotatedClass(Cliente.class);

        entityManagerFactory = config.buildSessionFactory();
    }

    @AfterAll
    static void closeFactory() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    @Override
    protected ClienteRepository crearRepositorio() {
        // 1. Creamos el EntityManager
        entityManager = entityManagerFactory.createEntityManager();

        // 2. IMPORTANTE: Abrimos la transacción MANUALMENTE.
        // Como JpaClienteRepository ya no lo hace (para ser compatible con Spring),
        // el test debe encargarse de proveer un entorno transaccional.
        entityManager.getTransaction().begin();

        return new JpaClienteRepository(entityManager);
    }

    @Override
    protected void limpiarDatos() {
        // Esta query requiere transacción. Como la abrimos en crearRepositorio(), funciona.
        entityManager.createQuery("DELETE FROM Cliente").executeUpdate();

        // Forzamos que se envíe a la DB y limpiamos memoria
        entityManager.flush();
        entityManager.clear();
    }

    @AfterEach
    void tearDown() {
        if (entityManager != null && entityManager.isOpen()) {
            // 3. Cerramos la transacción al finalizar cada test
            if (entityManager.getTransaction().isActive()) {
                // Hacemos rollback para dejar la DB limpia para el siguiente test
                // (Aunque limpiarDatos() ya se encarga, rollback es buena práctica en tests)
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }
}
```

#### Paso 3: Implementación del Repositorio JPA

```java
// 📁 src/main/java/com/tienda/features/procesarcompra/JpaClienteRepository.java
package com.tienda.features.procesarcompra;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación JPA del repositorio de clientes.
 *
 * Esta clase es el SUJETO del Contract Test.
 */
@Repository
public class JpaClienteRepository implements ClienteRepository {

    private final EntityManager entityManager;

    public JpaClienteRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void guardar(Cliente cliente) {
        entityManager.persist(cliente);
    }

    @Override
    public Optional<Cliente> buscarPorId(String id) {
        Cliente cliente = entityManager.find(Cliente.class, id);
        return Optional.ofNullable(cliente);
    }

    @Override
    @Transactional
    public void eliminarTodos() {
        // 1. FLUSH: Obliga a que los 'guardar()' pendientes viajen a la DB antes de borrar
        entityManager.flush();

        // 2. DELETE: Borra todo lo que hay en la DB
        entityManager.createQuery("DELETE FROM Cliente").executeUpdate();

        // 3. CLEAR: Borra la memoria de Hibernate para que no recuerde los objetos eliminados
        entityManager.clear();
    }
}
```

🟢 **Estado: Contract Tests PASAN**

```text
JpaClienteRepositoryTest
├── debeGuardarYRecuperar()          ✅ PASS
├── debeGuardarMultiples()           ✅ PASS
├── debeRetornarVacioSiNoExiste()    ✅ PASS
├── debeDistinguirVip()              ✅ PASS
└── debeEliminarTodos                ✅ PASS
```

# FASE 4: Conectando Todo (El Endpoint HTTP)

Ahora que tenemos:
- ✅ Tests de Aceptación (ATDD) que definen el comportamiento esperado
- ✅ Collaboration Tests que prueban la lógica de negocio
- ✅ Contract Tests que verifican el repositorio JPA

Es momento de **conectar todo** con el endpoint HTTP.

---

## 4.1 🌐 El Endpoint de Compras

```java
// 📁 src/main/java/com/tienda/features/procesarcompra/ProcesarCompraEndpoint.java

package com.tienda.features.procesarcompra;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 🌐 ENDPOINT HTTP PARA PROCESAR COMPRAS
 * 
 * Este es el punto de entrada HTTP. Su única responsabilidad es:
 * 1. Recibir la petición HTTP
 * 2. Delegarla al Use Case
 * 3. Convertir la respuesta a HTTP
 * 
 * ⚠️ NO debe tener lógica de negocio aquí.
 */
@RestController
@RequestMapping("/api/compras")
public class ProcesarCompraEndpoint {

    private final ProcesarCompraUseCase useCase;

    public ProcesarCompraEndpoint(ProcesarCompraUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * 🛒 POST /api/compras
     * Procesa una nueva compra y retorna el ticket con el total calculado.
     */
    @PostMapping
    public ResponseEntity<Ticket> procesarCompra(@RequestBody SolicitudCompra solicitud) {
        Ticket ticket = useCase.ejecutar(
                solicitud.getClienteId(),
                solicitud.getMonto()
        );
        return ResponseEntity.ok(ticket);
    }
}
```

---

## 4.2 🔧 El Endpoint de Clientes (Para Setup de Tests)

Este endpoint es necesario para que los tests de aceptación puedan crear clientes de prueba.

```java
// 📁 src/main/java/com/tienda/features/procesarcompra/ClienteEndpoint.java

package com.tienda.features.procesarcompra;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 🔧 ENDPOINT PARA GESTIÓN DE CLIENTES
 * 
 * ⚠️ En producción, estos endpoints probablemente tendrían 
 *    autenticación y más validaciones.
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteEndpoint {

    private final ClienteRepository clienteRepository;

    public ClienteEndpoint(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    /**
     * ➕ POST /api/clientes
     * Crea un nuevo cliente en el sistema.
     */
    @PostMapping
    public ResponseEntity<Void> crearCliente(@RequestBody Cliente cliente) {
        clienteRepository.guardar(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 🔍 GET /api/clientes/{id}
     * Busca un cliente por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarCliente(@PathVariable String id) {
        return clienteRepository.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 🗑️ DELETE /api/clientes
     * Elimina TODOS los clientes (solo para tests).
     */
    @DeleteMapping
    public ResponseEntity<Void> eliminarTodos() {
        clienteRepository.eliminarTodos();
        return ResponseEntity.noContent().build();
    }
}
```

---

## 4.3 ⚠️ Manejador Global de Excepciones

Para convertir nuestras excepciones de negocio en respuestas HTTP apropiadas:

```java
// 📁 src/main/java/com/tienda/features/procesarcompra/CompraExceptionHandler.java

package com.tienda.features.procesarcompra;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ⚠️ MANEJADOR GLOBAL DE EXCEPCIONES
 * 
 * Convierte las excepciones de Java en respuestas HTTP apropiadas.
 * Así mantenemos los endpoints limpios de try-catch.
 */
@RestControllerAdvice
public class CompraExceptionHandler {

    /**
     * 🚫 Cliente no encontrado → 404 NOT FOUND
     */
    @ExceptionHandler(ClienteNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleClienteNoEncontrado(
            ClienteNoEncontradoException ex) {
        
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "Cliente no encontrado",
                        "mensaje", ex.getMessage(),
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    /**
     * ❌ Validación fallida → 400 BAD REQUEST
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleValidacion(
            IllegalArgumentException ex) {
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "Error de validación",
                        "mensaje", ex.getMessage(),
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    /**
     * 💥 Cualquier otro error → 500 INTERNAL SERVER ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenerico(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "Error interno del servidor",
                        "mensaje", ex.getMessage(),
                        "timestamp", LocalDateTime.now().toString()
                ));
    }
}
```

---

## 4.4 🚨 La Excepción de Cliente No Encontrado

```java
// 📁 src/main/java/com/tienda/features/procesarcompra/ClienteNoEncontradoException.java

package com.tienda.features.procesarcompra;

/**
 * 🚨 Excepción lanzada cuando se intenta procesar una compra
 *    para un cliente que no existe en el sistema.
 */
public class ClienteNoEncontradoException extends RuntimeException {

    private final String clienteId;

    public ClienteNoEncontradoException(String clienteId) {
        super("No se encontró el cliente con ID: " + clienteId);
        this.clienteId = clienteId;
    }

    public String getClienteId() {
        return clienteId;
    }
}
```

---

## 4.5 ⚙️ Configuración de la Aplicación

### application.yml (Producción)

```yaml
# 📁 src/main/resources/application.yml

spring:
  application:
    name: tienda-vip
  
  datasource:
    url: jdbc:postgresql://localhost:5432/tienda
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate  # En producción, usar migraciones (Flyway/Liquibase)
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

server:
  port: 8080
```

### application-test.yml (Tests)

```yaml
# 📁 src/test/resources/application-test.yml

spring:
  jpa:
    hibernate:
      ddl-auto: create-drop  # Crea y destruye tablas en cada test
    show-sql: true
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```
---

## 🟢 FASE 5: Ejecutando Todos los Tests

### 5.1 🎯 Resultado Final

Ahora que todo está conectado, ejecutamos los tests:

```bash
mvn clean test
```

### 📊 Resultado Esperado:

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------

[INFO] Running com.tienda.features.procesarcompra.ProcesarCompraUseCaseTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0

[INFO] Running com.tienda.features.procesarcompra.JpaClienteRepositoryTest
🐳 Starting PostgreSQL container...
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0

[INFO] Running com.tienda.acceptance.specs.ProcesarCompraSpec
🐳 Starting PostgreSQL container...
🚀 Starting Spring Boot application...
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0

[INFO] -------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] -------------------------------------------------------
[INFO] Total time: 28.456 s
[INFO] -------------------------------------------------------
```

### 📋 Resumen de Tests por Tipo:

```text
┌─────────────────────────────────────────────────────────────────────────┐
│                        RESUMEN DE TESTS                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  🧪 COLLABORATION TESTS (TDD - Lógica de Negocio)                       │
│  ├── ProcesarCompraUseCaseTest                                          │
│  │   ├── ✅ Cliente VIP + $200 → $180 (descuento aplicado)              │
│  │   ├── ✅ Cliente VIP + $100 → $100 (sin descuento)                   │
│  │   ├── ✅ Cliente VIP + $99.99 → $99.99 (sin descuento)               │
│  │   ├── ✅ Cliente Normal + $500 → $500 (sin descuento)                │
│  │   ├── ✅ Cliente no existe → Exception                               │
│  │   └── ✅ Monto negativo → Exception                                  │
│  │                                                                      │
│  ⚡ Tiempo: ~0.3 segundos                                               │
│                                                                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  📜 CONTRACT TESTS (TDD - Repositorio)                                  │
│  ├── JpaClienteRepositoryTest                                           │
│  │   ├── ✅ Debe guardar y recuperar cliente                            │
│  │   ├── ✅ Debe guardar múltiples clientes                             │
│  │   ├── ✅ Debe retornar vacío si no existe                            │
│  │   ├── ✅ Debe distinguir VIP de Normal                               │
│  │   └── ✅ Debe eliminar todos                                         │
│  │                                                                      │
│  🐢 Tiempo: ~5 segundos (incluye Docker)                                │
│                                                                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  📋 ACCEPTANCE TESTS (ATDD - Sistema Completo)                          │
│  ├── ProcesarCompraSpec                                                 │
│  │   ├── ✅ Cliente VIP compra más de $100 → 10% descuento              │
│  │   ├── ✅ Cliente VIP compra $100 → sin descuento                     │
│  │   ├── ✅ Cliente VIP compra $99.99 → sin descuento                   │
│  │   ├── ✅ Cliente Normal → nunca descuento                            │
│  │   ├── ✅ Cliente no existe → 404 Not Found                           │
│  │   └── ✅ Monto inválido → 400 Bad Request                            │
│  │                                                                      │
│  🐢 Tiempo: ~20 segundos (Docker + Spring Boot)                         │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 FASE 6: Comparación Final TDD vs ATDD vs BDD

### 6.1 🔄 El Flujo Completo que Seguimos

```text
┌─────────────────────────────────────────────────────────────────────────┐
│                    FLUJO DE DESARROLLO COMPLETO                         │
└─────────────────────────────────────────────────────────────────────────┘

     1️⃣ ATDD                    2️⃣ TDD                    3️⃣ INTEGRACIÓN
   (Aceptación)              (Unitario)                  (Conectar)

┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│  📝 Escribir    │      │  🧪 Escribir    │      │  🔌 Crear       │
│  Specification  │ ───▶ │  Collaboration  │ ───▶ │  Endpoints      │
│  (Test E2E)     │      │  Tests          │      │  HTTP           │
│                 │      │                 │      │                 │
│  ❌ FALLA       │      │  ❌ FALLA       │      │                 │
└─────────────────┘      └─────────────────┘      └─────────────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │  💻 Implementar │
                         │  Use Case       │
                         │  (Lógica)       │
                         │                 │
                         │  ✅ PASA        │
                         └─────────────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │  📜 Escribir    │
                         │  Contract       │
                         │  Tests          │
                         │                 │
                         │  ❌ FALLA       │
                         └─────────────────┘
                                  │
                                  ▼
                         ┌─────────────────┐      ┌─────────────────┐
                         │  💾 Implementar │      │  ✅ TODO        │
                         │  Repository     │ ───▶ │  FUNCIONA       │
                         │  (JPA)          │      │                 │
                         │                 │      │  Tests E2E      │
                         │  ✅ PASA        │      │  ahora PASAN    │
                         └─────────────────┘      └─────────────────┘
```

### 6.2 📊 Tabla Comparativa Completa

| Aspecto | TDD (Collaboration) | TDD (Contract) | ATDD | BDD |
|:--------|:--------------------|:---------------|:-----|:----|
| **Nivel** | Clase/Función | Integración | Sistema | Comportamiento |
| **Velocidad** | ⚡ ms | 🐢 segundos | 🐌 segundos | Depende |
| **Dependencias** | Mocks | DB Real | Todo Real | Depende |
| **¿Quién lo lee?** | Devs | Devs | Todos | Todos |
| **¿Qué valida?** | Lógica | Contratos | Requisitos | Comportamiento |
| **Framework** | JUnit + Mockito | JUnit + TestContainers | JUnit + Spring | Cucumber/JBehave |
| **Ejemplo** | `calcular()` retorna 180 | `save()` persiste en DB | POST /api retorna 200 | DADO/CUANDO/ENTONCES |

### 6.3 🎯 ¿Cuándo Usar Cada Uno?

```text
┌─────────────────────────────────────────────────────────────────────────┐
│  🤔 ¿CUÁNDO USAR CADA METODOLOGÍA?                                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  📌 USA TDD (Collaboration Tests) CUANDO:                               │
│     • Estás escribiendo lógica de negocio                               │
│     • Necesitas feedback rápido (milisegundos)                          │
│     • Quieres probar múltiples casos edge                               │
│     • El código tiene cálculos complejos                                │
│                                                                         │
│  📌 USA TDD (Contract Tests) CUANDO:                                    │
│     • Implementas un repositorio o adapter                              │
│     • Necesitas verificar SQL/JPA                                       │
│     • Integras con servicios externos                                   │
│                                                                         │
│  📌 USA ATDD CUANDO:                                                    │
│     • Necesitas validar requisitos de negocio                           │
│     • Quieres documentación ejecutable                                  │
│     • El equipo incluye QA y Product Owners                             │
│     • Necesitas tests de regresión de alto nivel                        │
│                                                                         │
│  📌 USA BDD CUANDO:                                                     │
│     • Quieres que NO-técnicos escriban escenarios                       │
│     • Necesitas Gherkin (Given/When/Then en archivos .feature)          │
│     • El dominio es complejo y requiere documentación legible           │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 🏆 FASE 7: Buenas Prácticas y Consejos Finales

### 7.1 📐 La Pirámide de Tests

```text
                          ╱╲
                         ╱  ╲
                        ╱ E2E╲         🐌 Pocos, lentos, frágiles
                       ╱ ATDD ╲            (5-10% de tests)
                      ╱────────╲
                     ╱          ╲
                    ╱ INTEGRACIÓN╲     🐢 Algunos, moderados
                   ╱   Contract   ╲        (15-20% de tests)
                  ╱────────────────╲
                 ╱                  ╲
                ╱     UNITARIOS      ╲  ⚡ Muchos, rápidos, estables
               ╱   Collaboration      ╲     (70-80% de tests)
              ╱────────────────────────╲
```

### 7.2 ✅ Buenas Prácticas

```text
┌─────────────────────────────────────────────────────────────────────────┐
│                         BUENAS PRÁCTICAS                                │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ✅ HAZLO:                                                              │
│                                                                         │
│  1. Escribe el test ANTES que el código (Red → Green → Refactor)        │
│                                                                         │
│  2. Mantén los tests de aceptación en lenguaje de NEGOCIO               │
│     ❌ "POST /api/compras con body JSON..."                             │
│     ✅ "Cuando el cliente VIP realiza una compra de $200..."            │
│                                                                         │
│  3. Usa las 4 capas de Dave Farley para tests de aceptación             │
│     Specification → DSL → Driver → System                               │
│                                                                         │
│  4. Separa Collaboration Tests de Contract Tests                        │
│     (La trampa de los tests integrados de J.B. Rainsberger)             │
│                                                                         │
│  5. Usa TestContainers para bases de datos reales en tests              │
│     (Evita H2 u otras DBs in-memory que se comportan diferente)         │
│                                                                         │
│  6. Nombra tus tests describiendo el COMPORTAMIENTO                     │
│     ❌ testCalcular()                                                   │
│     ✅ debeAplicar10PorcientoDescuentoCuandoEsVipYMontoMayorA100()      │
│                                                                         │
│  7. Un test debe probar UNA sola cosa                                   │
│                                                                         │
│  8. Los tests deben ser INDEPENDIENTES entre sí                         │
│     (No deben depender del orden de ejecución)                          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  ❌ EVITA:                                                              │
│                                                                         │
│  1. Tests que dependen de datos externos o estado compartido            │
│                                                                         │
│  2. Tests que tardan más de 1 segundo (unitarios)                       │
│                                                                         │
│  3. Lógica de negocio en los endpoints                                  │
│                                                                         │
│  4. Mockear TODO (pierdes confianza en las integraciones)               │
│                                                                         │
│  5. No mockear NADA (tests lentos y frágiles)                           │
│                                                                         │
│  6. Tests que verifican implementación en lugar de comportamiento       │
│     ❌ verify(repo).save(any()) // Verifica que se llamó un método      │
│     ✅ assertEquals(180.00, ticket.getTotal()) // Verifica resultado    │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 7.3 🔧 Estructura Final del Proyecto

```text
src/
├── main/java/com/tienda/
│   ├── TiendaApplication.java
│   │
│   └── features/
│       └── procesarcompra/
│           ├── ProcesarCompraEndpoint.java      ← 🌐 HTTP entrada
│           ├── ClienteEndpoint.java             ← 🔧 HTTP (setup)
│           ├── ProcesarCompraUseCase.java       ← 💼 Lógica negocio
│           ├── ClienteRepository.java           ← 📜 Contrato
│           ├── JpaClienteRepository.java        ← 💾 Implementación
│           ├── Cliente.java                     ← 📦 Entidad
│           ├── SolicitudCompra.java             ← 📥 DTO entrada
│           ├── Ticket.java                      ← 📤 DTO salida
│           ├── ClienteNoEncontradoException.java
│           └── CompraExceptionHandler.java      ← ⚠️ Manejo errores
│
├── main/resources/
│   └── application.yml
│
└── test/
    ├── java/com/tienda/
    │   ├── acceptance/                          ← 📋 ATDD
    │   │   ├── specs/
    │   │   │   └── ProcesarCompraSpecTest.java      ← Capa 1
    │   │   ├── dsl/
    │   │   │   └── CompraDsl.java               ← Capa 2
    │   │   ├── drivers/
    │   │   │   └── CompraHttpDriver.java        ← Capa 3
    │   │   └── infrastructure/
    │   │       └── AcceptanceTestBase.java      ← Capa 4
    │   │
    │   └── features/
    │       └── procesarcompra/                  ← 🧪 TDD
    │           ├── ProcesarCompraUseCaseTest.java   ← Collaboration
    │           ├── ClienteRepositoryContract.java   ← Contract base
    │           └── JpaClienteRepositoryContractTest.java    ← Contract impl
    │
    └── resources/
        └── application-test.yml
```

---

## 🎓 Conclusión

### Lo que aprendiste:

| Concepto | Descripción |
|:---------|:------------|
| **TDD** | Escribir tests antes del código para guiar el diseño |
| **ATDD** | Tests de aceptación que validan requisitos de negocio |
| **BDD** | Describir comportamiento en lenguaje ubicuo (Given/When/Then) |
| **4 Capas de Dave Farley** | Specification → DSL → Driver → System |
| **Collaboration Tests** | Tests rápidos con mocks para lógica de negocio |
| **Contract Tests** | Tests de integración para verificar implementaciones |
| **Vertical Slice** | Organizar código por funcionalidad, no por capa técnica |
| **TestContainers** | Bases de datos reales en Docker para tests |

### 🚀 Próximos Pasos

1. **Practica** implementando otra feature (ej: "Registrar Usuario")
2. **Explora Cucumber** si quieres archivos `.feature` con Gherkin
3. **Añade más Contract Tests** para otros adapters (APIs externas, etc.)
4. **Integra con CI/CD** (GitHub Actions, Jenkins, etc.)

### 📚 Recursos Recomendados

| Recurso | Autor | Tema |
|:--------|:------|:-----|
| "Continuous Delivery" | Dave Farley & Jez Humble | ATDD, Deployment |
| "TDD by Example" | Kent Beck | TDD fundamentals |
| "Growing Object-Oriented Software" | Freeman & Pryce | TDD avanzado |
| Blog de J.B. Rainsberger | J.B. Rainsberger | Collaboration/Contract Tests |
| Canal YouTube Dave Farley | Dave Farley | Videos sobre testing |

---

### Ejecutar solo una sola vez para generar el wrapper de maven

```bash
# Genera los archivos mvnw y .mvn
mvn wrapper:wrapper
```
#### Qué son mvnw y .mvn?
- `mvnw (Linux/macOS) y mvnw.cmd (Windows)`: Son scripts que ejecutan Maven sin necesidad de tenerlo instalado globalmente en tu sistema, descargándolo si es necesario.
- `.mvn/wrapper/maven-wrapper .properties`: Este archivo de configuración define la versión de Maven que se debe usar.

#### Después de generar mvnw
Añádelo al control de versiones: Asegúrate de enviar los scripts mvnw, mvnw.cmd y la carpeta .mvn a tu repositorio (Git, SVN, etc.).

### Limpiar, empaquetar el componente y ejecuta todos los test

```bash
# Si pasan todos los tests se ejecuta empaquetamiento del binario 
./mvnw clean package
```
---

### Ejecutar todos los test

```bash
# Ejecutar todos los tests
./mvnw test
```

### Ejecutar las pruebas por separado

#### Asegúrate de tener Docker corriendo

```bash
# Solo unit tests (rápidos, sin Docker)
./mvnw test -Dgroups="unit"
```
```bash
# Solo contract tests (necesitan Docker)
./mvnw test -Dgroups="contract"
```
```bash
# Solo acceptance tests (necesitan Docker)
./mvnw test -Dgroups="acceptance"
```
---

```text
┌─────────────────────────────────────────────────────────────────────────┐
│                                                                         │
│   🎉 ¡FELICIDADES!                                                      │
│                                                                         │
│   Has completado la guía de TDD, ATDD y BDD en Java.                    │
│                                                                         │
│   Recuerda:                                                             │
│   • 🔴 Red    → Escribe un test que falle                               │
│   • 🟢 Green  → Escribe el código mínimo para que pase                   │
│   • 🔵 Refactor → Mejora el código sin romper tests                     │
│                                                                         │
│   "Los tests no son un costo, son una inversión a corto,                │
|    mediano y largo plazo."                                              │
│                                                                         │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```