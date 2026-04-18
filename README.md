# Taller JTA — Sistema de Exámenes en Línea

Aplicación Jakarta EE distribuida en microservicios Docker. Permite a estudiantes rendir exámenes en línea y recibir su nota por correo electrónico de forma asíncrona vía RabbitMQ.

---

## Arquitectura

```
Usuario
  │
  ▼
┌─────────────────────┐     HTTP REST      ┌──────────────────────┐
│   wildfly-logica    │ ─────────────────▶ │   wildfly-datos      │
│   (puerto 8080)     │ ◀───────────────── │   (puerto 8081)      │
│  Lógica de negocio  │   nota + datos     │  Acceso a BD / JPA   │
└─────────────────────┘                    └──────────┬───────────┘
          │                                           │
          │ publica mensaje                  ┌────────┴────────┐
          ▼                                  │                 │
┌─────────────────────┐              ┌───────┴──────┐  ┌──────┴──────┐
│      RabbitMQ       │              │db-estudiantes│  │ db-examenes │
│   (cola.correos)    │              │  puerto 5433 │  │ puerto 5434 │
└──────────┬──────────┘              └──────────────┘  └─────────────┘
           │ consume
           ▼
┌──────────────────────┐
│   correo-consumer    │
│  JavaMail → SMTP     │
└──────────────────────┘
```

### Contenedores

| Contenedor | Tecnología | Puerto |
|---|---|---|
| `wildfly-logica` | WildFly 39 / Jakarta EE 10 | 8080 (app), 9990 (admin) |
| `wildfly-datos` | WildFly 39 / Jakarta EE 10 | 8081 (app), 9991 (admin) |
| `db-estudiantes` | PostgreSQL 15 | 5433 |
| `db-examenes` | PostgreSQL 15 | 5434 |
| `rabbitmq` | RabbitMQ 3 + Management UI | 5672 (AMQP), 15672 (web) |
| `correo-consumer` | OpenJDK 21 JAR standalone | — |

---

## Requisitos previos

| Herramienta | Versión mínima | Verificar con |
|---|---|---|
| Java JDK | 21 | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Docker | 24+ | `docker -version` |
| Docker Compose | 2.x (plugin) | `docker compose version` |

---

## Configuración inicial (solo una vez)

### Credenciales de correo

Abre `docker-compose.yaml` en la raíz del proyecto y edita estas dos líneas:

```yaml
MAIL_USER: tucorreo@gmail.com    # ← tu dirección Gmail
MAIL_PASS: xxxx xxxx xxxx xxxx  # ← App Password de 16 caracteres
```

> **¿Cómo generar una App Password de Gmail?**
> 1. Entra a [myaccount.google.com/security](https://myaccount.google.com/security)
> 2. Activa la **Verificación en dos pasos** si no la tienes
> 3. Busca **"Contraseñas de aplicaciones"**
> 4. Crea una nueva → "Correo" + "Otro (nombre personalizado)"
> 5. Copia la clave de 16 caracteres y pégala en `MAIL_PASS`

---

## Compilar los proyectos

Desde la raíz del repositorio (`Taller_JTA/`), ejecuta en orden:

```bash
# 1. Capa de lógica
cd taller2
./mvnw clean package -DskipTests
cd ..

# 2. Capa de datos
cd taller2-datos
./mvnw clean package -DskipTests
cd ..

# 3. Consumer de correos
cd correo-consumer
mvn clean package -DskipTests
cd ..
```

> En **Windows** usa `mvnw.cmd` en lugar de `./mvnw`.  
> En **Linux/Mac**, si el script no ejecuta: `chmod +x taller2/mvnw taller2-datos/mvnw`

---

## Levantar la aplicación

```bash
# Desde la raíz (donde está docker-compose.yaml)
docker compose up --build
```

La primera vez tarda unos minutos en descargar imágenes base. La app está lista cuando veas:

```
wildfly-logica  | WFLYSRV0025: WildFly ... started in ...ms
wildfly-datos   | WFLYSRV0025: WildFly ... started in ...ms
correo-consumer | [Consumer] Escuchando cola: cola.correos
```

Para correr en segundo plano:

```bash
docker compose up --build -d

# Ver logs en tiempo real
docker compose logs -f
```

---

## Probar la aplicación

### Verificar que los servicios están vivos

```bash
curl http://localhost:8080/taller2/api/examen/test
# Respuesta esperada: Funciona
```

### Entregar un examen

```bash
curl -X POST http://localhost:8080/taller2/api/examen/entregar \
  -H "Content-Type: application/json" \
  -d '{
    "estudianteId": 1,
    "examenId": 1,
    "respuestas": {
      "1": 1,
      "2": 6
    }
  }'
```

**Respuesta esperada:** `Examen entregado correctamente`

**Flujo interno que ocurre:**
1. `wildfly-logica` recibe el POST
2. Llama por REST a `wildfly-datos` para persistir respuestas y calcular nota
3. `wildfly-datos` guarda todo en PostgreSQL y devuelve la nota
4. `wildfly-logica` publica un mensaje en la cola `cola.correos` de RabbitMQ
5. `correo-consumer` consume el mensaje y envía el correo al estudiante por SMTP

---

## Datos de prueba precargados

Las bases de datos se inicializan automáticamente al levantar los contenedores.

**Estudiantes** (`db-estudiantes`)

| ID | Nombre | Apellido | Correo |
|---|---|---|---|
| 1 | Juan | Pérez | juan.perez@test.com |
| 2 | María | García | maria.garcia@test.com |
| 3 | Carlos | López | carlos.lopez@test.com |

**Exámenes** (`db-examenes`)

| ID | Nombre | Materia |
|---|---|---|
| 1 | Examen 1 | Historia |

**Preguntas del Examen 1**

| ID | Enunciado | ID opción correcta |
|---|---|---|
| 1 | ¿En qué año llegó Colón a América? | 1 → "1492" |
| 2 | ¿Quién fue el primer presidente de Colombia? | 6 → "Antonio Nariño" |

---

## Interfaces de administración

| Interfaz | URL | Usuario | Contraseña |
|---|---|---|---|
| RabbitMQ Management | http://localhost:15672 | guest | guest |
| WildFly Admin (lógica) | http://localhost:9990 | — | — |
| WildFly Admin (datos) | http://localhost:9991 | — | — |

---

## Detener la aplicación

```bash
# Detener contenedores
docker compose down

# Detener Y borrar datos de las BDs (reset completo)
docker compose down -v
```

---

## Estructura del repositorio

```
Taller_JTA/
│
├── docker-compose.yaml              ← Orquestador (configura aquí el correo)
│
├── taller2/                         ← Capa de LÓGICA (puerto 8080)
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/org/eclipse/jakarta/
│       ├── Config/                  JAX-RS config
│       ├── Controller/              Endpoint REST público (/api/examen)
│       ├── DTO/                     EntregarExamenDTO
│       ├── Service/                 NuevoExamenService
│       └── Util/                    Correos.java → publica a RabbitMQ
│
├── taller2-datos/                   ← Capa de DATOS (puerto 8081)
│   ├── Dockerfile
│   ├── configure.cli                Datasources XA PostgreSQL
│   ├── pom.xml
│   ├── init-estudiantes/            SQL inicial de la BD estudiantes
│   ├── init-examenes/               SQL inicial de la BD examenes
│   └── src/main/java/org/eclipse/jakarta/
│       ├── Config/                  JAX-RS config
│       ├── Controller/              DatosController (endpoint interno)
│       ├── Model/                   Entidades JPA
│       └── Repository/              Repositorios JPA
│
└── correo-consumer/                 ← Consumer RabbitMQ + JavaMail
    ├── Dockerfile
    ├── pom.xml
    └── src/main/java/org/eclipse/jakarta/consumer/
        └── CorreoConsumer.java      Escucha cola y envía correo SMTP
```

---

## Solución de problemas

**El correo no llega**
- Verifica `MAIL_USER` y `MAIL_PASS` en `docker-compose.yaml`
- `MAIL_PASS` debe ser una App Password de Gmail (16 caracteres), no tu clave normal
- Revisa logs: `docker compose logs correo-consumer`

**WildFly no arranca / error de conexión a la BD**
- Espera 30-60 segundos; PostgreSQL puede tardar en inicializarse
- Verifica que los puertos 5433 y 5434 no estén ocupados: `sudo lsof -i :5433`
- Revisa logs: `docker compose logs db-estudiantes`

**Error al compilar con Maven**
- Asegúrate de usar Java 21: `java -version`
- Verifica que el `JAVA_HOME` apunta a Java 21

**RabbitMQ no conecta**
- El consumer reintenta 10 veces con 5 segundos de espera entre intentos
- Si falla: `docker compose restart correo-consumer`
- Verifica la consola en http://localhost:15672

**Puerto ya en uso**
```bash
# Linux/Mac
sudo lsof -i :8080

# Windows
netstat -ano | findstr :8080
```
