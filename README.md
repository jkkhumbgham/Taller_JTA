# Taller JTA
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
│  JakartaMail → SMTP  │
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

## Requisitos

| Herramienta | Versión mínima | Verificar con |
|---|---|---|
| Java JDK | 21 | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Docker | 24+ | `docker -version` |
| Docker Compose | 2.x (plugin) | `docker compose version` |

---

## Configuración inicial

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


Esto no es necesario si quiere seguir usando el correo ya configurado testingsistemas39@gmail.com

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

---

## Levantar la aplicación

```bash
# Desde la raíz (`Taller_JTA/`)
docker compose up --build
```

La primera vez tarda unos minutos en descargar imágenes base. La app está lista cuando veas:

```
wildfly-logica  | WFLYSRV0025: WildFly ... started in ...ms
wildfly-datos   | WFLYSRV0025: WildFly ... started in ...ms
correo-consumer | [Consumer] Escuchando cola: cola.correos
```

---

## Probar la aplicación

### Verificar que los servicios están vivos

usando tu navegador o Postman que es la herramienta recomendada para probar la app ejecuta http://localhost:8080/taller2/api/examen/test y deberias recibir un "Funciona"

### Entregar un examen

para probar la funcionalidad clave de la app ejecuta preferiblemente en Postman http://localhost:8080/taller2/api/examen/entregar y pon en el body un JSON como:
```bash
{
  "estudianteId": 2,
  "examenId": 1,
  "respuestas": {
    "1": 1,
    "2": 3
  }
}
```
Tener en cuenta que al ser estudiantes con correos falsos, para poder ver el correo edita el import-estudiantes.sql y pon el correo al que quieras que llegue en alguno de los estudiantes, recuerda que al momento de hacer la peticion el estudianteId debe coincidir con el estudiante donde pusiste la direccion de correo donde quieres recibir este

**Respuesta esperada:** `Examen entregado correctamente`

**Flujo interno que ocurre:**
1. `wildfly-logica` recibe el POST
2. Llama por REST a `wildfly-datos` para persistir respuestas y calcular nota
3. `wildfly-datos` guarda todo en PostgreSQL y devuelve la nota
4. `wildfly-logica` publica un mensaje en la cola `cola.correos` de RabbitMQ
5. `correo-consumer` consume el mensaje y envía el correo al estudiante por SMTP

---

## Datos de prueba precargados

Las tablas se crean y los datos se insertan automáticamente cuando WildFly arranca, gracias a Hibernate (`drop-and-create`) y los archivos `import-*.sql`.

---

**Estudiantes** — `db-estudiantes`

| ID | Nombre | Apellido | Correo |
|---|---|---|---|
| 1 | Juan | Pérez | juan.perez@test.com |
| 2 | María | García | maria.garcia@test.com |
| 3 | Carlos | López | carlos.lopez@test.com |

---

**Exámenes** — `db-examenes`

| ID | Nombre | Materia |
|---|---|---|
| 1 | Examen 1 | Historia |

**Preguntas**

| ID | Enunciado | Examen ID |
|---|---|---|
| 1 | ¿En qué año llegó Colón a América? | 1 |
| 2 | ¿Quién fue el primer presidente de Colombia? | 1 |

**Opciones**

| ID | Texto | ¿Correcta? | Pregunta ID |
|---|---|---|---|
| 1 | 1492 | T | 1 |
| 2 | 1500 | F | 1 |
| 3 | 1485 | F | 1 |
| 4 | Simón Bolívar | F | 2 |
| 5 | Francisco de Paula Santander | F | 2 |
| 6 | Antonio Nariño | T | 2 |

---

