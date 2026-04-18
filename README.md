# PatrulleroApp — Backend

API REST desarrollada con Java 21 + Spring Boot 3.5 para la gestión de procedimientos municipales en terreno.

## Stack tecnológico
- Java 21
- Spring Boot 3.5.13
- Spring Security + JWT
- Spring Data JPA + Hibernate
- SQL Server (base de datos relacional, 10 tablas en 3FN)
- iTextPDF (generación de reportes)

## Requisitos previos
- Java JDK 21
- SQL Server con la base de datos PatrulleroAppDB creada
- Maven (incluido via mvnw)

## Configuración
Edita `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=PatrulleroAppDB;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=TU_PASSWORD
```

## Levantar en desarrollo
```bash
./mvnw spring-boot:run
```
El servidor inicia en `http://localhost:8080`

## Endpoints principales
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /api/auth/login | Autenticación JWT |
| GET | /api/turnos/activo | Turno activo |
| POST | /api/turnos/abrir | Abrir turno |
| PUT | /api/turnos/cerrar | Cerrar turno |
| GET | /api/turnos/{id}/reporte | Descargar PDF |
| POST | /api/solicitudes | Crear solicitud |
| GET | /api/solicitudes/turno-activo | Solicitudes del turno |
| GET | /api/usuarios | Listar usuarios |
| POST | /api/usuarios | Crear usuario |

## Despliegue
Desplegado en Railway conectado a este repositorio GitHub.

## Autor
Felipe Millán Flores — Analista Programador IPLACEX