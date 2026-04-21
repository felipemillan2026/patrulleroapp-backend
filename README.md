# PatrulleroApp — Backend

API REST para la gestión digitalizada de procedimientos operativos de patrulleros municipales.

## Stack tecnológico

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.5.13 |
| Seguridad | Spring Security + JWT (HMAC-SHA256) + BCrypt |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | MySQL 9.4 (Railway) |
| Reportes PDF | iTextPDF 5.5.13 |
| Email | Spring Mail (Gmail SMTP) |

## Arquitectura

El backend implementa una arquitectura REST stateless con separación por capas:

- **Controller** — endpoints REST con validación de roles
- **Service** — lógica de negocio y reglas operativas
- **Repository** — acceso a datos mediante Spring Data JPA
- **Security** — autenticación JWT con filtro por request

## Base de datos

Modelo relacional en 3FN compuesto por 10 tablas:
`roles`, `usuarios`, `departamentos`, `tipos_caso`, `turnos`, `turno_patrullero`, `solicitudes`, `solicitud_tipo_caso`, `imagenes`, `reportes`

## Requisitos

- Java JDK 21
- Maven (incluido via `mvnw`)
- Conexión a MySQL (local o Railway)

## Configuración

Crea `src/main/resources/application.properties` basándote en el archivo de ejemplo:

```properties
spring.datasource.url=jdbc:mysql://HOST:PORT/DATABASE
spring.datasource.username=USUARIO
spring.datasource.password=CONTRASEÑA
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=none
spring.jpa.open-in-view=false
spring.mail.host=smtp.gmail.com
spring.mail.port=465
spring.mail.username=TU_CORREO
spring.mail.password=TU_APP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.ssl.enable=true
```

## Levantar en desarrollo

```bash
./mvnw spring-boot:run
```

El servidor inicia en `http://localhost:8080`

## Endpoints principales

| Método | Ruta | Descripción | Rol |
|---|---|---|---|
| POST | /api/auth/login | Autenticación JWT | Público |
| GET | /api/turnos/activo | Turno activo | Autenticado |
| POST | /api/turnos/abrir | Abrir turno | Supervisor |
| PUT | /api/turnos/cerrar | Cerrar turno | Supervisor |
| GET | /api/turnos/{id}/reporte | Descargar PDF | Supervisor |
| GET | /api/turnos/ultimo-cerrado | Último turno cerrado | Supervisor |
| POST | /api/solicitudes | Crear solicitud | Patrullero |
| PUT | /api/solicitudes/{id} | Editar solicitud | Patrullero |
| PUT | /api/solicitudes/{id}/estado | Cambiar estado | Centralista |
| GET | /api/solicitudes/mis-solicitudes | Mis solicitudes | Patrullero |
| GET | /api/solicitudes/turno-activo | Solicitudes del turno | Centralista |
| GET | /api/departamentos | Listar departamentos | Autenticado |
| GET | /api/departamentos/{id}/tipos-caso | Tipos de caso | Autenticado |
| GET | /api/usuarios | Listar usuarios | Supervisor |
| POST | /api/usuarios | Crear usuario | Supervisor |
| PUT | /api/usuarios/{id} | Editar usuario | Supervisor |
| PUT | /api/usuarios/{id}/toggle-activo | Activar/desactivar | Supervisor |

## Despliegue en producción

El backend está desplegado en **Railway** con las siguientes variables de entorno: