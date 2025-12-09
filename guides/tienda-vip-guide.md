# Diferencias entre BDD, ATDD y TDD en Java: Guía Completa
## Con Vertical Slice Architecture, TestContainers y el enfoque de Dave Farlay y J.B. Rainsberger
---

## Resumen Ejecutivo

| Característica | **ATDD** (Acceptance)                | **BDD** (Behavior) | **TDD** (Test) |
| :--- |:-------------------------------------| :--- | :--- |
| **Pregunta** | "¿Construimos lo correcto?"          | "¿Se comporta como dijimos?" | "¿Está bien construido?" |
| **Nivel** | Alto nivel (End-to-End)              | Semántico (Lenguaje Negocio) | Bajo nivel (Unitario) |
| **Enfoque** | Caja Negra (Sistema completo)        | Given / When / Then | Caja Blanca (Implementación) |
| **Audiencia** | Tres amigos(Devs, QA, Product Owner) | Tres amigos(Devs, QA, Product Owner) | Solo Desarrolladores |

---

## Escenario de Negocio

> **Historia de Usuario:** Como cliente VIP de la tienda, quiero recibir un 10% de descuento cuando mi compra supere los $100, para ahorrar dinero en mis compras grandes.

> **Criterios de Aceptación:**
> - Si el cliente es VIP Y el monto > $100 → aplicar 10% de descuento
> - Si el cliente es VIP Y el monto ≤ $100 → no aplicar descuento
> - Si el cliente NO es VIP → no aplicar descuento (sin importar el monto)

---

## Estructura del Proyecto (Vertical Slice Architecture)

```
src/
├── main/java/com/tienda/
│   ├── TiendaApplication.java
│   │
│   ├── features/
│   │   └── procesarcompra/           ← Todo el código de esta feature junto
│   │       ├── ProcesarCompraEndpoint.java
│   │       ├── ProcesarCompraUseCase.java
│   │       ├── ClienteRepository.java          (Contrato/Interfaz)
│   │       ├── ProcesarCompraSpecTest.java .java       (Implementación)
│   │       ├── Cliente.java
│   │       ├── SolicitudCompra.java
│   │       ├── Ticket.java
│   │       └── ClienteNoEncontradoException.java
│   │
│   └── shared/
│       └── config/
│           └── JpaConfig.java
│
└── test/java/com/tienda/
    ├── acceptance/                    ← ATDD (4 Capas de Dave Farley)
    │   ├── specs/
    │   │   └── ProcesarCompraSpecTest.java           (Capa 1: Specification)
    │   ├── dsl/
    │   │   └── CompraDsl.java                    (Capa 2: DSL)
    │   ├── drivers/
    │   │   └── CompraHttpDriver.java             (Capa 3: Protocol Driver)
    │   └── infrastructure/
    │       └── AcceptanceTestBase.java           (Configuración TestContainers)
    │
    └── features/
        └── procesarcompra/            ← TDD (J.B. Rainsberger)
            ├── ProcesarCompraUseCaseTest.java    (Collaboration Test)
            └── ProcesarCompraSpecTest.java Contract.java (Contract Test)
```

---

## PARTE 1: Las 4 Capas de Dave Farley para ATDD

Dave Farley propone separar las pruebas de aceptación en capas para hacerlas mantenibles y expresivas:

```
┌─────────────────────────────────────────────────────────────────────┐
│  CAPA 1: SPECIFICATION (Especificación)                            │
│  ═══════════════════════════════════════                            │
│  "Dado un cliente VIP, cuando compra $200, entonces paga $180"      │
│  → Lenguaje de NEGOCIO puro. No menciona HTTP, JSON, ni bases de   │
│    datos.                                                           │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  CAPA 2: DSL (Domain Specific Language)                             │
│  ══════════════════════════════════════                             │
│  clienteVipExiste("CLI-001")                                        │
│  realizarCompra("CLI-001", 200.00)                                  │
│  verificarTotalEs(180.00)                                           │
│  → Acciones de ALTO NIVEL. Oculta detalles técnicos.                │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  CAPA 3: PROTOCOL DRIVER (Controlador de Protocolo)                 │
│  ══════════════════════════════════════════════════                 │
│  POST /api/compras con JSON {"clienteId": "CLI-001", "monto": 200}  │
│  → Detalles TÉCNICOS del protocolo (HTTP, WebSocket, gRPC, etc.)    │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  CAPA 4: SYSTEM UNDER TEST (Sistema Bajo Prueba)                    │
│  ══════════════════════════════════════════════                     │
│  Spring Boot + PostgreSQL (TestContainers)                          │
│  → El sistema REAL ejecutándose.                                    │
└─────────────────────────────────────────────────────────────────────┘
```

---

## PARTE 2: TDD al estilo J.B. Rainsberger

J.B. Rainsberger propone dos tipos de tests para evitar la "Trampa de los Tests integrados":

| Tipo de Test | Propósito | I/O Real | Velocidad |
|:---|:---|:---|:---|
| **Collaboration Test** | Probar lógica de negocio | ❌ No (Mocks) | ⚡ Milisegundos |
| **Contract Test** | Probar que la implementación cumple el contrato | ✅ Sí (TestContainers) | 🐢 Segundos |

```
┌─────────────────────────────────────────────────────────────────────┐
│                    COLLABORATION TEST                               │
│  ═══════════════════════════════════                                │
│  Sujeto: ProcesarCompraUseCase                                      │
│  Dependencias: Mock de ClienteRepository                            │
│  Pregunta: "¿La lógica de negocio es correcta?"                     │
│                                                                     │
│  → Si el email está mal formateado, ¿rechaza?                       │
│  → Si el cliente no existe, ¿lanza excepción?                       │
│  → Si es VIP con $200, ¿calcula bien el 10%?                        │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                      CONTRACT TEST                                  │
│  ═══════════════════════════════════                                │
│  Sujeto: JpaClienteRepository                                       │
│  Dependencias: PostgreSQL real (TestContainers)                     │
│  Pregunta: "¿El mapeo JPA y las queries funcionan?"                 │
│                                                                     │
│  → ¿Puedo guardar un cliente?                                       │
│  → ¿Puedo recuperarlo por ID?                                       │
│  → ¿Las anotaciones @Entity están correctas?                        │
└─────────────────────────────────────────────────────────────────────┘
```

---

## FASE 1: Configuración Base con TestContainers

### 1.1 Dependencias (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.0</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.tienda</groupId>
    <artifactId>tienda-vip</artifactId>
    <version>1.0.0</version>
    <name>tienda-vip</name>
    <description>Diferencias entre BDD, ATDD y TDD en Java: Guía Completa</description>
    <url/>
    <licenses>
        <license/>
    </licenses>
    <developers>
        <developer/>
    </developers>
    <scm>
        <connection/>
        <developerConnection/>
        <tag/>
        <url/>
    </scm>
    <properties>
        <java.version>21</java.version>
    </properties>
    <dependencies>
        <!-- Si usas fechas (Instant, LocalDateTime), añade esta dependencia: -->
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- PostgreSQL -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test-classic</artifactId>
            <scope>test</scope>
        </dependency>
        <!-- TestContainers -->
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

        <!-- AssertJ para assertions fluidas -->
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

### 1.2 Clase Base para Tests de Aceptación

```java
// src/test/java/com/tienda/acceptance/infrastructure/AcceptanceTestBase.java

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

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AcceptanceTestBase {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:alpine")
            .withDatabaseName("tienda_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    // Obtenemos el puerto real donde levantó Spring
    @LocalServerPort
    int port;

    // Esta es la nueva herramienta estrella de Spring 6
    protected RestClient restClient;

    @BeforeEach
    void setupClient() {
        // Configuramos el cliente para apuntar a localhost:PUERTO
        this.restClient = RestClient.create("http://localhost:" + port);
    }
}
```

### 1.3 Clase principal

```java
// src/main/java/com/tienda/TiendaApplication.java

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

## FASE 2: ATDD con las 4 Capas de Dave Farley

### CAPA 1: Specification (Especificación)

Esta es la capa que lee el Product Owner. Lenguaje puro de negocio.

```java
// src/test/java/com/tienda/acceptance/specs/ProcesarCompraSpec.java

package com.tienda.acceptance.specs;

import com.tienda.acceptance.drivers.CompraHttpDriver;
import com.tienda.acceptance.dsl.CompraDsl;
import com.tienda.acceptance.infrastructure.AcceptanceTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CAPA 1: Specification
 *
 * Esta clase la puede leer el Product Owner, QA, o cualquier stakeholder.
 *
 * REGLA DE ORO: No debe haber NINGÚN detalle técnico aquí.
 * - No HTTP, no JSON, no SQL
 * - Solo lenguaje de NEGOCIO
 *
 * Formato: Given / When / Then usando el DSL
 */

@DisplayName("Feature: Procesar Compra con Descuento VIP")
class ProcesarCompraSpec extends AcceptanceTestBase {

    private CompraDsl compra;

    @BeforeEach
    void setUp() {
        // CAMBIO: Usamos 'restClient' (Java 21/Spring 6 style)
        CompraHttpDriver driver = new CompraHttpDriver(restClient);
        compra = new CompraDsl(driver);
        compra.dadoQueNoHayClientes();
    }

    // ... EL RESTO DE LOS TESTS PERMANECE EXACTAMENTE IGUAL ...
    // Gracias al DSL, el código del test no cambió nada.

    @Nested
    @DisplayName("Escenario: Cliente VIP compra más de $100")
    class ClienteVipCompraGrande {
        @Test
        @DisplayName("Entonces recibe 10% de descuento")
        void debeAplicarDescuento() {
            compra.dadoUnClienteVip("CLI-001", "Juan Pérez");
            compra.cuandoRealizaUnaCompraDe("CLI-001", 200.00);
            compra
                    .entoncesLaCompraEsExitosa()
                    .entoncesSeAplicoDescuento()
                    .entoncesElTotalEs(180.00);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // ESCENARIO: Cliente VIP con compra pequeña
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Escenario: Cliente VIP compra $100 o menos")
    class ClienteVipCompraPequena {

        @Test
        @DisplayName("Entonces NO recibe descuento")
        void noDebeAplicarDescuento() {
            // GIVEN
            compra.dadoUnClienteVip("CLI-002", "María García");

            // WHEN
            compra.cuandoRealizaUnaCompraDe("CLI-002", 100.00);

            // THEN
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

    // ══════════════════════════════════════════════════════════════════
    // ESCENARIO: Cliente Normal (no VIP)
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Escenario: Cliente Normal (no VIP)")
    class ClienteNormal {

        @Test
        @DisplayName("Nunca recibe descuento, sin importar el monto")
        void nuncaRecibeDescuento() {
            // GIVEN
            compra.dadoUnClienteNormal("CLI-004", "Paula Torres");

            // WHEN: Compra grande
            compra.cuandoRealizaUnaCompraDe("CLI-004", 500.00);

            // THEN: Sin descuento
            compra
                    .entoncesLaCompraEsExitosa()
                    .entoncesNoSeAplicoDescuento()
                    .entoncesElTotalEs(500.00);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // ESCENARIO: Errores
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Escenario: Casos de Error")
    class CasosDeError {

        @Test
        @DisplayName("Cliente no existe en el sistema")
        void clienteNoExiste() {
            // GIVEN: No creamos ningún cliente

            // WHEN: Intentamos comprar con un ID inexistente
            compra.cuandoRealizaUnaCompraDe("CLIENTE-FANTASMA", 200.00);

            // THEN
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

### CAPA 2: DSL (Domain Specific Language)

Esta capa traduce el lenguaje de negocio a llamadas del Protocol Driver.

```java
// src/test/java/com/tienda/acceptance/dsl/CompraDsl.java

package com.tienda.acceptance.dsl;

import com.tienda.acceptance.drivers.CompraHttpDriver;
import com.tienda.features.procesarcompra.Cliente;
import com.tienda.features.procesarcompra.Ticket;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CAPA 2: DSL (Domain Specific Language)
 *
 * Proporciona un vocabulario de NEGOCIO para escribir tests.
 *
 * NO conoce:
 * - HTTP, JSON, URLs
 * - Detalles de implementación
 *
 * SÍ conoce:
 * - Conceptos del dominio (Cliente, Compra, VIP, Descuento)
 * - Acciones de negocio (registrar cliente, realizar compra)
 */
public class CompraDsl {

    private final CompraHttpDriver driver;

    public CompraDsl(CompraHttpDriver driver) {
        this.driver = driver;
    }

    // ══════════════════════════════════════════════════════════════════
    // GIVEN: Configuración del estado inicial
    // ══════════════════════════════════════════════════════════════════

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

    // ══════════════════════════════════════════════════════════════════
    // WHEN: Acciones de negocio
    // ══════════════════════════════════════════════════════════════════

    public CompraDsl cuandoRealizaUnaCompraDe(String clienteId, double monto) {
        driver.realizarCompra(clienteId, monto);
        return this;
    }

    // ══════════════════════════════════════════════════════════════════
    // THEN: Verificaciones
    // ══════════════════════════════════════════════════════════════════

    public CompraDsl entoncesLaCompraEsExitosa() {
        assertTrue(driver.fueExitoso(), "La compra debería ser exitosa");
        return this;
    }

    public CompraDsl entoncesElTotalEs(double totalEsperado) {
        Ticket ticket = driver.obtenerTicket();
        assertEquals(totalEsperado, ticket.getTotal(), 0.01,
                "El total del ticket no coincide");
        return this;
    }

    public CompraDsl entoncesElDescuentoEs(double descuentoEsperado) {
        Ticket ticket = driver.obtenerTicket();
        assertEquals(descuentoEsperado, ticket.getDescuento(), 0.01,
                "El descuento no coincide");
        return this;
    }

    public CompraDsl entoncesSeAplicoDescuento() {
        Ticket ticket = driver.obtenerTicket();
        assertTrue(ticket.isDescuentoAplicado(),
                "Debería haberse aplicado descuento");
        return this;
    }

    public CompraDsl entoncesNoSeAplicoDescuento() {
        Ticket ticket = driver.obtenerTicket();
        assertFalse(ticket.isDescuentoAplicado(),
                "No debería haberse aplicado descuento");
        return this;
    }

    public CompraDsl entoncesElClienteNoFueEncontrado() {
        assertEquals(HttpStatus.NOT_FOUND, driver.obtenerCodigoEstado(),
                "Debería retornar 404 Not Found");
        return this;
    }

    public CompraDsl entoncesHayUnErrorDeValidacion() {
        assertEquals(HttpStatus.BAD_REQUEST, driver.obtenerCodigoEstado(),
                "Debería retornar 400 Bad Request");
        return this;
    }
}
```

---

### CAPA 3: Protocol Driver (Controlador de Protocolo)

Esta capa conoce los detalles técnicos: HTTP, JSON, URLs, headers.

```java
// src/test/java/com/tienda/acceptance/drivers/CompraHttpDriver.java

package com.tienda.acceptance.drivers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tienda.features.procesarcompra.Cliente;
import com.tienda.features.procesarcompra.SolicitudCompra;
import com.tienda.features.procesarcompra.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

/**
 * CAPA 3: Protocol Driver (Versión RestClient - Spring 6)
 *
 * Conoce los detalles técnicos del protocolo HTTP:
 * - URLs de los endpoints
 * - Métodos HTTP (GET, POST, etc.)
 * - Headers y Content-Types
 * - Serialización JSON
 *
 * OCULTA estos detalles de las capas superiores.
 */
public class CompraHttpDriver {

    private final RestClient restClient;
    private final ObjectMapper objectMapper; // Necesario para deserializar manualmente

    // CAMBIO 1: Guardamos la respuesta como String (JSON crudo) para evitar fallos de mapeo en errores
    private ResponseEntity<String> ultimaRespuesta;

    public CompraHttpDriver(RestClient restClient) {
        this.restClient = restClient;
        this.objectMapper = new ObjectMapper(); // Instancia simple de Jackson
    }

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

    // ══════════════════════════════════════════════════════════════════
    // OPERACIONES DE NEGOCIO
    // ══════════════════════════════════════════════════════════════════

    public void realizarCompra(String clienteId, double monto) {
        SolicitudCompra solicitud = new SolicitudCompra(clienteId, monto);

        // CAMBIO 2: Recibimos String.class en lugar de Ticket.class
        // Esto permite capturar tanto el JSON de éxito como el JSON de error sin explotar.
        this.ultimaRespuesta = restClient.post()
                .uri("/api/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .body(solicitud)
                .retrieve()
                .onStatus(status -> true, (req, resp) -> { /* Ignorar errores HTTP para validarlos en el test */ })
                .toEntity(String.class);
    }

    // ══════════════════════════════════════════════════════════════════
    // OPERACIONES DE VERIFICACIÓN
    // ══════════════════════════════════════════════════════════════════

    public Ticket obtenerTicket() {
        // CAMBIO 3: Deserializamos a Ticket SOLO si la respuesta fue exitosa y tiene cuerpo
        if (ultimaRespuesta.getStatusCode().is2xxSuccessful() && ultimaRespuesta.getBody() != null) {
            try {
                return objectMapper.readValue(ultimaRespuesta.getBody(), Ticket.class);
            } catch (Exception e) {
                throw new RuntimeException("Error al leer el Ticket de la respuesta JSON", e);
            }
        }
        return null; // O lanzar excepción si prefieres, pero null es seguro aquí
    }

    public HttpStatus obtenerCodigoEstado() {
        return (HttpStatus) ultimaRespuesta.getStatusCode();
    }

    public boolean fueExitoso() {
        return ultimaRespuesta.getStatusCode().is2xxSuccessful();
    }
}
```
### CAPA 4: System Under Test (El Sistema Real)

Primero definimos qué vamos a construir. Todo dentro de un Vertical Slice.

```java
// src/main/java/com/tienda/features/procesarcompra/Cliente.java

package com.tienda.features.procesarcompra;

import jakarta.persistence.*;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    private String id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private boolean vip;

    protected Cliente() {} // JPA

    public Cliente(String id, String nombre, boolean vip) {
        this.id = id;
        this.nombre = nombre;
        this.vip = vip;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public boolean isVip() { return vip; }
}
```

```java
// src/main/java/com/tienda/features/procesarcompra/SolicitudCompra.java

package com.tienda.features.procesarcompra;

public class SolicitudCompra {

    private String clienteId;
    private double monto;

    public SolicitudCompra() {}

    public SolicitudCompra(String clienteId, double monto) {
        this.clienteId = clienteId;
        this.monto = monto;
    }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
}
```

```java
// src/main/java/com/tienda/features/procesarcompra/Ticket.java

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

### 🔴 Estado Actual: TODOS LOS TESTS FALLAN

```
ProcesarCompraSpec
├── Cliente VIP compra más de $100
│   └── debeAplicarDescuento()              ❌ 404 Not Found
├── Cliente VIP compra $100 o menos
│   ├── noDebeAplicarDescuento()            ❌ 404 Not Found
│   └── casoBordeNoRecibeDescuento()        ❌ 404 Not Found
├── Cliente Normal (no VIP)
│   └── nuncaRecibeDescuento()              ❌ 404 Not Found
└── Casos de Error
    ├── clienteNoExiste()                   ❌ 404 Not Found
    └── montoInvalido()                     ❌ 404 Not Found
```

---

## FASE 3: TDD al estilo J.B. Rainsberger

### 3.1 El Contrato (Interfaz del Repositorio)

```java
// src/main/java/com/tienda/features/procesarcompra/ClienteRepository.java

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

---

### 3.2 Collaboration Test (Lógica de Negocio Aislada)

**Foco:** Validar la lógica de negocio del Use Case.
**Dependencias:** Mocks.
**Velocidad:** Milisegundos.

```java
// src/test/java/com/tienda/features/procesarcompra/ProcesarCompraUseCaseTest.java

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

### 🔴 Estado: Tests FALLAN (No existe la clase)

```
ProcesarCompraUseCaseTest
├── ClienteVipCompraGrande
│   ├── debeCalcularDescuento()              ❌ ClassNotFoundException
│   ├── casoBordeSiRecibeDescuento()         ❌ ClassNotFoundException
│   └── montoMuyGrande()                     ❌ ClassNotFoundException
├── ... (todos fallan igual)
```

---

### 3.3 Implementación del Use Case 🟢

```java
// src/main/java/com/tienda/features/procesarcompra/ProcesarCompraUseCase.java

package com.tienda.features.procesarcompra;

import org.springframework.stereotype.Service;

/**
 * USE CASE: Procesar Compra
 *
 * Contiene la LÓGICA DE NEGOCIO pura.
 *
 * - Depende del CONTRATO (ClienteRepository), no de la implementación
 * - No conoce JPA, HTTP, ni ningún detalle técnico
 * - Fácilmente testeable con mocks
 */
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
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
    }

    private Cliente buscarCliente(String clienteId) {
        return clienteRepository.buscarPorId(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoException(clienteId));
    }

    private double calcularDescuento(Cliente cliente, double monto) {
        boolean esVip = cliente.isVip();
        boolean superaMontoMinimo = monto > MONTO_MINIMO_PARA_DESCUENTO;

        if (esVip && superaMontoMinimo) {
            return monto * PORCENTAJE_DESCUENTO;
        }
        return 0.00;
    }
}
```

```java
// src/main/java/com/tienda/features/procesarcompra/ClienteNoEncontradoException.java

package com.tienda.features.procesarcompra;

public class ClienteNoEncontradoException extends RuntimeException {

    public ClienteNoEncontradoException(String clienteId) {
        super("Cliente no encontrado: " + clienteId);
    }
}
```

### 🟢 Estado: Collaboration Tests PASAN

```
ProcesarCompraUseCaseTest
├── ClienteVipCompraGrande
│   ├── debeCalcularDescuento()              ✅ PASS
│   ├── casoBordeSiRecibeDescuento()         ✅ PASS
│   └── montoMuyGrande()                     ✅ PASS
├── ClienteVipCompraPequena
│   ├── noDebeAplicarDescuento()             ✅ PASS
│   └── casoBordeNoRecibeDescuento()         ✅ PASS
├── ClienteNormal
│   └── nuncaRecibeDescuento()               ✅ PASS
├── Validaciones
│   ├── clienteNoExiste()                    ✅ PASS
│   ├── montoNegativo()                      ✅ PASS
│   └── montoCero()                          ✅ PASS
└── Colaboraciones
    ├── debeBuscarCliente()                  ✅ PASS
    └── noDebeCalcularSiNoExiste()           ✅ PASS
```

---

### 3.4 Contract Test (Repositorio con TestContainers)

**Foco:** Validar que la implementación JPA cumple el contrato.
**Dependencias:** PostgreSQL real (TestContainers).
**Velocidad:** Segundos (levanta Docker).

#### Paso 1: Definir el Contrato Abstracto

```java
// src/test/java/com/tienda/features/procesarcompra/ClienteRepositoryContract.java

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

#### Paso 2: Implementación del Contract Test con TestContainers

```java
// src/test/java/com/tienda/features/procesarcompra/JpaClienteRepositoryTest.java

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
class JpaClienteRepositoryTest extends ClienteRepositoryContract {

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

#### Paso 3: Implementación del Repositorio JPA 🟢

```java
// src/main/java/com/tienda/features/procesarcompra/JpaClienteRepository.java

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

### 🟢 Estado: Contract Tests PASAN

```
JpaClienteRepositoryTest (extends ClienteRepositoryContract)
├── debeGuardarYRecuperar()          ✅ PASS
├── debeGuardarMultiples()           ✅ PASS
├── debeRetornarVacioSiNoExiste()    ✅ PASS
├── debeDistinguirVip()              ✅ PASS
└── debeEliminarTodos()              ✅ PASS
```

---

## FASE 4: Conectar Todo (El Endpoint)

### 4.1 El Endpoint HTTP

```java
// src/main/java/com/tienda/features/procesarcompra/ProcesarCompraEndpoint.java

package com.tienda.features.procesarcompra;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProcesarCompraEndpoint {

    private final ProcesarCompraUseCase useCase;
    private final ClienteRepository clienteRepository;

    public ProcesarCompraEndpoint(
            ProcesarCompraUseCase useCase,
            ClienteRepository clienteRepository) {
        this.useCase = useCase;
        this.clienteRepository = clienteRepository;
    }

    @PostMapping("/compras")
    public ResponseEntity<Ticket> procesarCompra(@RequestBody SolicitudCompra solicitud) {
        Ticket ticket = useCase.ejecutar(
                solicitud.getClienteId(),
                solicitud.getMonto()
        );
        return ResponseEntity.ok(ticket);
    }

    // Endpoints de soporte para tests de aceptación
    @PostMapping("/clientes")
    public ResponseEntity<Void> crearCliente(@RequestBody Cliente cliente) {
        clienteRepository.guardar(cliente);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clientes")
    public ResponseEntity<Void> eliminarClientes() {
        clienteRepository.eliminarTodos();
        return ResponseEntity.ok().build();
    }
}
```

### 4.2 Manejo de Excepciones

```java
// src/main/java/com/tienda/features/procesarcompra/CompraExceptionHandler.java

package com.tienda.features.procesarcompra;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class CompraExceptionHandler {

    @ExceptionHandler(ClienteNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleClienteNoEncontrado(
            ClienteNoEncontradoException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "Cliente no encontrado",
                        "mensaje", ex.getMessage(),
                        "timestamp", LocalDateTime.now()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleArgumentoInvalido(
            IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "Datos inválidos",
                        "mensaje", ex.getMessage(),
                        "timestamp", LocalDateTime.now()
                ));
    }
}
```

### 4.3 Configuración JPA para Spring

```java
// src/main/java/com/tienda/shared/config/JpaConfig.java

package com.tienda.shared.config;

import com.tienda.features.procesarcompra.ClienteRepository;
import com.tienda.features.procesarcompra.JpaClienteRepository;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpaConfig {

    @Bean
    public ClienteRepository clienteRepository(EntityManager entityManager) {
        return new JpaClienteRepository(entityManager);
    }
}
```

### 4.4 Application Properties

```yaml
# src/main/resources/application.yml

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tienda
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

```yaml
# src/test/resources/application-test.yml

spring:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
```

---

## FASE 5: Ejecutar Tests de Aceptación

### 🟢 Estado Final: TODOS LOS TESTS PASAN

```
═══════════════════════════════════════════════════════════════════════════
                           RESULTADOS FINALES
═══════════════════════════════════════════════════════════════════════════

ATDD/BDD - Tests de Aceptación (4 Capas de Dave Farley)
─────────────────────────────────────────────────────────────────────────
ProcesarCompraSpec
├── Cliente VIP compra más de $100
│   └── debeAplicarDescuento()                    ✅ PASS
├── Cliente VIP compra $100 o menos
│   ├── noDebeAplicarDescuento()                  ✅ PASS
│   └── casoBordeNoRecibeDescuento()              ✅ PASS
├── Cliente Normal (no VIP)
│   └── nuncaRecibeDescuento()                    ✅ PASS
└── Casos de Error
    ├── clienteNoExiste()                         ✅ PASS
    └── montoInvalido()                           ✅ PASS

TDD - Collaboration Tests (J.B. Rainsberger)
─────────────────────────────────────────────────────────────────────────
ProcesarCompraUseCaseTest
├── ClienteVipCompraGrande
│   ├── debeCalcularDescuento()                   ✅ PASS
│   ├── casoBordeSiRecibeDescuento()              ✅ PASS
│   └── montoMuyGrande()                          ✅ PASS
├── ClienteVipCompraPequena
│   ├── noDebeAplicarDescuento()                  ✅ PASS
│   └── casoBordeNoRecibeDescuento()              ✅ PASS
├── ClienteNormal
│   └── nuncaRecibeDescuento()                    ✅ PASS
├── Validaciones
│   ├── clienteNoExiste()                         ✅ PASS
│   ├── montoNegativo()                           ✅ PASS
│   └── montoCero()                               ✅ PASS
└── Colaboraciones
    ├── debeBuscarCliente()                       ✅ PASS
    └── noDebeCalcularSiNoExiste()                ✅ PASS

TDD - Contract Tests (J.B. Rainsberger + TestContainers)
─────────────────────────────────────────────────────────────────────────
JpaClienteRepositoryTest
├── debeGuardarYRecuperar()                       ✅ PASS
├── debeGuardarMultiples()                        ✅ PASS
├── debeRetornarVacioSiNoExiste()                 ✅ PASS
├── debeDistinguirVip()                           ✅ PASS
└── debeEliminarTodos()                           ✅ PASS

═══════════════════════════════════════════════════════════════════════════
Tests ejecutados: 22 | Exitosos: 22 | Fallidos: 0
═══════════════════════════════════════════════════════════════════════════
```

---

## Diagrama de Arquitectura Final

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        ATDD: 4 CAPAS DE DAVE FARLEY                     │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │ CAPA 1: SPECIFICATION (ProcesarCompraSpec.java)                   │  │
│  │ "Dado un cliente VIP, cuando compra $200, entonces paga $180"     │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                  │                                      │
│                                  ▼                                      │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │ CAPA 2: DSL (CompraDsl.java)                                      │  │
│  │ dadoUnClienteVip("CLI-001").cuandoCompra(200).entoncesTotalEs(180)│  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                  │                                      │
│                                  ▼                                      │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │ CAPA 3: PROTOCOL DRIVER (CompraHttpDriver.java)                   │  │
│  │ POST /api/compras → JSON → HTTP Response                          │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                  │                                      │
│                                  ▼                                      │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │ CAPA 4: SYSTEM UNDER TEST                                         │  │
│  │ Spring Boot + PostgreSQL (TestContainers)                         │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                    TDD: ENFOQUE J.B. RAINSBERGER                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────────────────┐    ┌─────────────────────────────────┐ │
│  │   COLLABORATION TEST       │    │      CONTRACT TEST              │ │
│  │   (Lógica de Negocio)      │    │   (Integración con DB)          │ │
│  ├─────────────────────────────┤    ├─────────────────────────────────┤ │
│  │                             │    │                                 │ │
│  │  ProcesarCompraUseCaseTest  │    │  JpaClienteRepositoryTest       │ │
│  │                             │    │                                 │ │
│  │  ┌───────────────────────┐  │    │  ┌───────────────────────────┐  │ │
│  │  │ ProcesarCompraUseCase │  │    │  │  JpaClienteRepository     │  │ │
│  │  └───────────┬───────────┘  │    │  └───────────────────────────┘  │ │
│  │              │              │    │              │                  │ │
│  │              ▼              │    │              ▼                  │ │
│  │  ┌───────────────────────┐  │    │  ┌───────────────────────────┐  │ │
│  │  │   Mock Repository     │  │    │  │  PostgreSQL (Docker)      │  │ │
│  │  └───────────────────────┘  │    │  └───────────────────────────┘  │ │
│  │                             │    │                                 │ │
│  │  Velocidad: ⚡ ms           │    │  Velocidad: 🐢 segundos         │ │
│  │  I/O: ❌ No                 │    │  I/O: ✅ Sí (Real)              │ │
│  │                             │    │                                 │ │
│  └─────────────────────────────┘    └─────────────────────────────────┘ │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                    VERTICAL SLICE ARCHITECTURE                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  features/                                                              │
│  └── procesarcompra/                 ← Todo junto por FEATURE           │
│      ├── Cliente.java                   (Entidad)                       │
│      ├── ClienteRepository.java         (Contrato)                      │
│      ├── JpaClienteRepository.java      (Implementación)                │
│      ├── ProcesarCompraUseCase.java     (Lógica de Negocio)             │
│      ├── ProcesarCompraEndpoint.java    (HTTP)                          │
│      ├── SolicitudCompra.java           (DTO entrada)                   │
│      ├── Ticket.java                    (DTO salida)                    │
│      └── Excepciones...                                                 │
│                                                                         │
│  ❌ NO hay paquetes por capa técnica (controller/, service/, repo/)     │
│  ✅ SÍ hay paquetes por funcionalidad de negocio                        │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Resumen de Conceptos Clave

### Las 4 Capas de Dave Farley (ATDD)

| Capa | Responsabilidad | Conoce | No Conoce |
|:---|:---|:---|:---|
| **Specification** | Lenguaje de negocio | Reglas, escenarios | HTTP, JSON, SQL |
| **DSL** | Vocabulario del dominio | Acciones de alto nivel | Detalles de protocolo |
| **Protocol Driver** | Comunicación técnica | HTTP, headers, URLs | Lógica de negocio |
| **System Under Test** | Ejecución real | Todo el stack | - |

### Enfoque J.B. Rainsberger (TDD)

| Tipo de Test | Sujeto | Dependencias | Velocidad | Propósito |
|:---|:---|:---|:---|:---|
| **Collaboration** | Use Case | Mocks | ⚡ ms | Lógica de negocio |
| **Contract** | Repository | DB Real | 🐢 s | Integración correcta |

### Vertical Slice Architecture

```
❌ Evitar:                          ✅ Preferir:
controllers/                        features/
├── CompraController.java           ├── procesarcompra/
├── UsuarioController.java          │   ├── ProcesarCompraEndpoint.java
services/                           │   ├── ProcesarCompraUseCase.java
├── CompraService.java              │   └── ... (todo junto)
├── UsuarioService.java             └── registrarusuario/
repositories/                           ├── RegistrarUsuarioEndpoint.java
├── CompraRepository.java               └── ... (todo junto)
└── UsuarioRepository.java
```

### ¿Cuándo Falla Cada Tipo de Test?

| Si falla... | Significa que... |
|:---|:---|
| **Specification (ATDD)** | El flujo completo no cumple el requisito de negocio |
| **Collaboration Test** | La lógica de negocio tiene un bug |
| **Contract Test** | El mapeo JPA o las queries SQL están mal |

---

## Conclusión

> **ATDD** define QUÉ debe hacer el sistema (comportamiento esperado).
> **TDD Collaboration** asegura que la LÓGICA de negocio es correcta (sin I/O).
> **TDD Contract** asegura que la INTEGRACIÓN con la base de datos funciona.
> **Vertical Slice** organiza el código por funcionalidad, no por capas técnicas.
> **TestContainers** proporciona infraestructura REAL en los tests.

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