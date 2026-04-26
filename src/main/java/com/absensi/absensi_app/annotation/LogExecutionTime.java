package com.absensi.absensi_app.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotasi kustom {@code @LogExecutionTime} untuk mengukur dan mencatat waktu eksekusi sebuah method
 * secara otomatis menggunakan Aspect-Oriented Programming (AOP).
 *
 * <p><b>Cara Kerja:</b><br>
 * Anotasi ini sendiri tidak melakukan apapun. Ia hanya berfungsi sebagai sebuah "marker" (penanda).
 * Yang melakukan pekerjaan sesungguhnya adalah {@link com.absensi.absensi_app.aspect.LoggingAspect},
 * sebuah komponen AOP yang dikonfigurasi untuk "mengawasi" method mana saja yang menggunakan
 * anotasi {@code @LogExecutionTime} ini, lalu menyisipkan logika logging di sekelilingnya.
 *
 * <p><b>Meta-Annotation yang digunakan:</b>
 * <ul>
 *   <li>{@code @Target(ElementType.METHOD)} - Membatasi agar anotasi ini hanya bisa
 *       diletakkan di atas sebuah <b>method</b>, bukan pada class atau field.</li>
 *   <li>{@code @Retention(RetentionPolicy.RUNTIME)} - Memastikan anotasi ini tetap
 *       tersedia di <b>runtime</b> (saat program berjalan), sehingga AOP proxy Spring Boot
 *       bisa mendeteksinya melalui Java Reflection.</li>
 * </ul>
 *
 * <p><b>Contoh Penggunaan:</b>
 * <pre>{@code
 * @LogExecutionTime
 * public void clockIn(Long userId, String type) {
 *     // Method akan otomatis di-log waktu eksekusinya
 * }
 * }</pre>
 *
 * @author  Fauzi Septian
 * @version 1.0
 * @see     com.absensi.absensi_app.aspect.LoggingAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {
    /**
     * Deskripsi opsional yang dapat ditambahkan untuk memberikan konteks tambahan pada log.
     *
     * @return deskripsi method, default string kosong.
     */
    String value() default "";
}
