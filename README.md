# Taller No. 2 — Transacciones Distribuidas

Este taller implementa una solución para el manejo de transacciones distribuidas entre dos bases de datos, desacoplando la lógica de negocio del acceso a datos. Cuando un estudiante entrega un examen, el sistema persiste la información en ambas bases de datos, calcula la nota y le envía un correo electrónico con su resultado, todo de forma coordinada.

---

## ¿Cómo está organizado el sistema?

La aplicación se divide en seis contenedores que se comunican entre sí:

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

| Contenedor | Tecnología | Puerto |
|---|---|---|
| `wildfly-logica` | WildFly 39 / Jakarta EE 10 | 8080 (app), 9990 (admin) |
| `wildfly-datos` | WildFly 39 / Jakarta EE 10 | 8081 (app), 9991 (admin) |
| `db-estudiantes` | PostgreSQL 15 | 5433 |
| `db-examenes` | PostgreSQL 15 | 5434 |
| `rabbitmq` | RabbitMQ 3 + Management UI | 5672 (AMQP), 15672 (web) |
| `correo-consumer` | OpenJDK 21 JAR standalone | — |

---

## Lo que se necesita antes de empezar

Se debe tener instaladas las siguientes herramientas:

| Herramienta | Versión | ¿Cómo verificarla? |
|---|---|---|
| Java JDK | 21 | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Docker | 24+ | `docker -version` |
| Docker Compose | 2.x (plugin) | `docker compose version` |

---

## Configuración inicial

### Credenciales de correo

Antes de levantar la aplicación, el desarrollador debe editar el archivo `docker-compose.yaml` con su cuenta de Gmail:

```yaml
MAIL_USER: tucorreo@gmail.com    # dirección Gmail del desarrollador
MAIL_PASS: xxxx xxxx xxxx xxxx  # App Password de 16 caracteres
```

> **¿Cómo se genera una App Password de Gmail?**
> 1. El desarrollador ingresa a [myaccount.google.com/security](https://myaccount.google.com/security)
> 2. Activa la **Verificación en dos pasos** si aún no la tiene
> 3. Busca **"Contraseñas de aplicaciones"**
> 4. Crea una nueva → "Correo" + "Otro (nombre personalizado)"
> 5. Copia la clave de 16 caracteres y la pega en `MAIL_PASS`

Para el desarrollo del taller se usa la cuenta **testingsistemas39@gmail.com**.

---

## Compilación de los proyectos

Desde la raíz del repositorio (`Taller_JTA/`), el desarrollador compila los tres módulos en orden:

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

## Levantamiento de la aplicación

Con todo compilado, se ejecuta desde la raíz del proyecto:

```bash
docker compose up --build
```

---

## Cómo probar la aplicación?

### Verificar que los servicios están en línea

Se puede abrir en el navegador o en Postman la siguiente URL:

```
http://localhost:8080/taller2/api/examen/test
```

Si todo está bien, la respuesta será simplemente: `Funciona`

---

### Entregar un examen

Para probar el flujo completo, se hace una petición `POST` a:

```
http://localhost:8080/taller2/api/examen/entregar
```

Con el siguiente cuerpo JSON:

```json
{
  "estudianteId": 2,
  "examenId": 1,
  "respuestas": {
    "1": 1,
    "2": 3
  }
}
```

> **Nota sobre el correo:** Como los estudiantes de prueba tienen correos ficticios, el desarrollador puede editar el archivo `import-estudiantes.sql` y reemplazar el correo de uno de los estudiantes por una dirección real donde quiera recibir la notificación. El `estudianteId` en la petición debe coincidir con el estudiante cuyo correo fue modificado.

**Respuesta esperada:** `Examen entregado correctamente`

**Flujo de trabajo:**
1. `wildfly-logica` recibe el `POST`
2. Llama por REST a `wildfly-datos` para persistir las respuestas y calcular la nota
3. `wildfly-datos` guarda todo en PostgreSQL y devuelve la nota obtenida
4. `wildfly-logica` publica un mensaje en la cola `cola.correos` de RabbitMQ
5. `correo-consumer` consume el mensaje y envía el correo al estudiante vía SMTP

---

## Datos de prueba precargados

Las tablas se crean e inicializan automáticamente cuando WildFly arranca, gracias a Hibernate (`drop-and-create`) y los archivos `import-*.sql`. No se requiere ninguna configuración manual de base de datos.

### Estudiantes — `db-estudiantes`

| ID | Nombre | Apellido | Correo |
|---|---|---|---|
| 1 | Juan | Pérez | juan.perez@test.com |
| 2 | María | García | maria.garcia@test.com |
| 3 | Carlos | López | carlos.lopez@test.com |

---

### Exámenes — `db-examenes`

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
