# Design Patterns - Absensi App 📚

Dokumen ini menjelaskan penerapan berbagai *Design Patterns* dalam project **Absensi App** untuk menciptakan kode yang bersih, mudah dikelola, dan profesional.

---

## 1. Strategy Pattern (Behavioral)
**Lokasi**: `com.absensi.absensi_app.strategy` & `AbsensiServiceImpl.java`

Pola ini digunakan untuk menangani berbagai tipe absensi (WFO, WFH, dll) tanpa menggunakan banyak blok `if-else` atau `switch`.

- **Kenapa ini Senior?**: Mematuhi *Open/Closed Principle*. Jika ada tipe absensi baru, kita cukup menambah class baru yang mengimplementasikan `CheckInStrategy` tanpa menyentuh logika utama di service.
- **Penerapan**:
    - `CheckInStrategy`: Interface dasar.
    - `WfoCheckInStrategy` & `WfhCheckInStrategy`: Implementasi spesifik.
    - `AbsensiServiceImpl`: Menggunakan list of strategies untuk mencari yang cocok secara dinamis.

```java
// Contoh penggunaan Strategy di AbsensiServiceImpl
@Service
public class AbsensiServiceImpl implements AbsensiService {
    private final List<CheckInStrategy> strategies;

    public void clockIn(Long userId, String type, String keterangan) {
        CheckInStrategy strategy = strategies.stream()
                .filter(s -> s.supports(type))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Tipe tidak didukung"));
        
        Absensi absensi = strategy.checkIn(user, keterangan);
        absensiRepository.save(absensi);
    }
}
```

---

## 2. Builder Pattern (Creational)
**Lokasi**: Hampir di seluruh Entity dan DTO (via Lombok `@Builder`).

Pola ini digunakan untuk membangun objek kompleks selangkah demi selangkah.

- **Penerapan**: 
```java
// Membangun objek User dengan Builder
User user = User.builder()
        .name(request.getName())
        .email(request.getEmail())
        .password(hashedPassword)
        .role(Role.EMPLOYEE)
        .build();
```
- **Manfaat**: Menghindari "Telescoping Constructor" (constructor dengan terlalu banyak parameter) dan membuat kode lebih mudah dibaca.

---

## 3. Factory Method (Creational)
**Lokasi**: `com.absensi.absensi_app.util.ApiMapper`

Digunakan untuk menstandarisasi pembuatan objek response API.

- **Penerapan**:
```java
// Factory method static untuk membuat response sukses
public static <T> ApiResponse<T> success(String message, T data) {
    return ApiResponse.<T>builder()
            .status(HttpStatus.OK.value())
            .message(message)
            .data(data)
            .build();
}
```
- **Manfaat**: Konsistensi format response di seluruh endpoint API.

---

## 4. Proxy Pattern (Structural)
**Lokasi**: `@Transactional` di Service Layer dan Spring Security.

Spring Framework menggunakan pola ini secara intensif untuk menyuntikkan logika tambahan (seperti manajemen transaksi atau keamanan) ke dalam metode kita.

- **Penerapan**: 
```java
// Spring menciptakan Proxy untuk menangani transaksi database
@Override
@Transactional
public UserResponse register(RegisterRequest request) {
    // Logika bisnis...
}
```

---

## 5. Data Mapper (Structural)
**Lokasi**: `com.absensi.absensi_app.util.UserMapper`

- **Penerapan**:
```java
// Mengonversi Entity ke DTO (Response)
public UserResponse toResponse(User user) {
    return UserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .build();
}
```
- **Manfaat**: Mencegah kebocoran data sensitif (seperti password) ke API response dan memungkinkan struktur data API berbeda dari struktur tabel database.

---

## 6. Chain of Responsibility / Interceptor (Behavioral)
**Lokasi**: `com.absensi.absensi_app.exception.GlobalExceptionHandler`

Meskipun dalam Spring Boot ini lebih dikenal sebagai *Controller Advice*, secara konsep ini mirip dengan interceptor atau rantai tanggung jawab.

- **Penerapan**: 
```java
// Mencegat semua ApiException secara global
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(ApiMapper.error(e.getStatus().value(), e.getMessage()));
    }
}
```
- **Manfaat**: Penanganan error terpusat. Kode bisnis (service) tidak perlu dikotori dengan blok `try-catch` yang berlebihan.

---

## Kesimpulan
Penerapan pola-pola di atas memastikan bahwa **Absensi App** dibangun dengan standar industri. Kode menjadi lebih modular, mudah diuji (*testable*), dan siap untuk dikembangkan menjadi aplikasi skala besar.
