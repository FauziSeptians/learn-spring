# Absensi App 🚀

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9.x-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![JWT](https://img.shields.io/badge/JWT-JSON%20Web%20Token-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white)](https://jwt.io/)

Aplikasi sistem absensi karyawan berbasis Spring Boot yang dirancang dengan arsitektur modern, aman, dan mudah dikembangkan.

## ✨ Fitur Utama
- **Authentication**: Registrasi user baru menggunakan BCrypt password encoding.
- **User Management**: Operasi CRUD (Create, Read, Update, Delete) data user.
- **Attendance System**: Pencatatan waktu masuk (Clock-In) dan keluar (Clock-Out). *(In Development)*
- **Interactive API Documentation**: Terintegrasi dengan Swagger/OpenAPI.
- **Database Console**: Akses langsung ke database H2 via browser.

## 🛠️ Stack Teknologi
- **Backend**: Spring Boot 3.2.0 (Java 21)
- **Security**: Spring Security & JWT
- **Database**: H2 In-Memory Database (Development)
- **ORM**: Spring Data JPA (Hibernate)
- **Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Utilities**: Lombok, Validation

## 🚀 Cara Menjalankan Project

### Prasyarat
- [JDK 21](https://www.oracle.com/java/technologies/downloads/#java21)
- [Maven 3.9+](https://maven.apache.org/download.cgi)

### Langkah-langkah
1. **Clone repository**
   ```bash
   git clone https://github.com/FauziSeptians/learn-spring.git
   cd learn-spring
   ```

2. **Jalankan aplikasi**
   Gunakan Maven Wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
   Atau jika sudah terinstall Maven:
   ```bash
   mvn spring-boot:run
   ```

3. **Akses Aplikasi**
   Aplikasi akan berjalan di port `9090`.

## 📖 Dokumentasi API & Tooling

| Service | URL | Keterangan |
|---------|-----|------------|
| **Swagger UI** | [http://localhost:9090/swagger-ui.html](http://localhost:9090/swagger-ui.html) | Dokumentasi API Interaktif |
| **H2 Console** | [http://localhost:9090/h2-console](http://localhost:9090/h2-console) | Database Manager (Browser) |
| **API Docs** | [http://localhost:9090/v3/api-docs](http://localhost:9090/v3/api-docs) | JSON OpenAPI Spec |

### Konfigurasi H2 Console
- **JDBC URL**: `jdbc:h2:mem:absensidb`
- **User**: `admin`
- **Password**: `admin123`

## 📡 Endpoint API Utama

### Auth
- `POST /api/auth/register` - Mendaftarkan user baru.

### User
- `GET /api/users` - Mengambil semua data user.
- `GET /api/users/{id}` - Mengambil detail user berdasarkan ID.
- `PUT /api/users/{id}` - Mengupdate data user.
- `DELETE /api/users/{id}` - Menghapus data user.

## 📂 Struktur Folder
```text
src/main/java/com/absensi/absensi_app/
├── config/       # Konfigurasi Security, Swagger, dll.
├── controller/   # REST Controllers (API Endpoints)
├── dto/          # Data Transfer Objects (Request/Response)
├── entity/       # Database Models (JPA Entities)
├── enums/        # Enums (Role, Status Absensi)
├── exception/    # Custom Exception Handling
├── repository/   # Data Access Layer
├── services/     # Business Logic Interfaces
└── util/         # Helper Classes & Mappers
```

---
Dibuat dengan ❤️ oleh [Fauzi Septian](https://github.com/FauziSeptians)
