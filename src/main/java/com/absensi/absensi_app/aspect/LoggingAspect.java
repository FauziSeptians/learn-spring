package com.absensi.absensi_app.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Komponen AOP (Aspect-Oriented Programming) yang bertanggung jawab untuk mencatat
 * waktu eksekusi method secara otomatis.
 *
 * <p><b>Konsep AOP:</b><br>
 * AOP memungkinkan kita memisahkan "cross-cutting concerns" (kepedulian lintas lapisan)
 * seperti logging, keamanan, dan transaksi dari logika bisnis utama. Tanpa AOP, kita harus
 * menyalin-tempel kode logging ke setiap method secara manual.
 *
 * <p><b>Annotation yang digunakan pada class ini:</b>
 * <ul>
 *   <li>{@code @Aspect} - Menandai class ini sebagai sebuah "Aspect" dalam AOP.
 *       Spring akan menggunakannya sebagai definisi dari "apa yang harus dilakukan" dan
 *       "kapan melakukannya".</li>
 *   <li>{@code @Component} - Mendaftarkan class ini sebagai Spring Bean, sehingga
 *       Spring dapat mengelola siklus hidupnya (lifecycle) dan melakukan Dependency Injection.</li>
 * </ul>
 *
 * <p><b>Istilah AOP dalam class ini:</b>
 * <ul>
 *   <li><b>Advice</b>: Kode yang dijalankan oleh Aspect. Di sini, advice-nya adalah method
 *       {@link #logExecutionTime(ProceedingJoinPoint)}.</li>
 *   <li><b>Pointcut</b>: Ekspresi yang menentukan method mana yang akan diintersepsi.
 *       Di sini: semua method yang dianotasi dengan {@code @LogExecutionTime}.</li>
 *   <li><b>Join Point</b>: Titik eksekusi spesifik yang sedang diintersepsi (method yang sedang dipanggil).</li>
 * </ul>
 *
 * @author  Fauzi Septian
 * @version 1.0
 * @see     com.absensi.absensi_app.annotation.LogExecutionTime
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Advice bertipe {@code @Around} yang membungkus eksekusi method yang dianotasi
     * dengan {@code @LogExecutionTime}.
     *
     * <p><b>Bagaimana {@code @Around} bekerja:</b><br>
     * Berbeda dengan {@code @Before} atau {@code @After}, {@code @Around} mengambil kendali
     * penuh atas eksekusi method target. Ia berjalan <b>sebelum dan sesudah</b> method asli dipanggil.
     * Method asli tidak akan berjalan sampai kita secara eksplisit memanggil
     * {@code joinPoint.proceed()}.
     *
     * <p><b>Pointcut Expression:</b><br>
     * {@code "@annotation(com.absensi.absensi_app.annotation.LogExecutionTime)"} berarti:
     * "Intersepsi semua method yang memiliki anotasi {@code @LogExecutionTime} di manapun."
     *
     * @param joinPoint objek {@link ProceedingJoinPoint} yang merepresentasikan method
     *                  yang sedang diintersepsi. Melalui objek ini kita bisa mendapatkan
     *                  nama method, nama class, dan memicu eksekusi method aslinya.
     * @return nilai kembalian dari method asli yang dieksekusi.
     * @throws Throwable jika method asli melempar exception, exception tersebut akan
     *                   di-log dan di-rethrow agar tetap bisa ditangani oleh caller.
     */
    @Around("@annotation(com.absensi.absensi_app.annotation.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.info("START_EXECUTION | Method: [{}.{}]", className, methodName);

        Object proceed;
        try {
            // Memanggil method asli. Tanpa baris ini, method yang didekorasi tidak akan pernah berjalan.
            proceed = joinPoint.proceed();
        } catch (Throwable t) {
            log.error("ERROR_EXECUTION | Method: [{}.{}] | Error: [{}]", className, methodName, t.getMessage());
            throw t; // Re-throw agar ExceptionHandler tetap bisa menangkap exception ini
        }

        long executionTime = System.currentTimeMillis() - start;

        log.info("END_EXECUTION | Method: [{}.{}] | Executed in {} ms", className, methodName, executionTime);

        return proceed;
    }
}
