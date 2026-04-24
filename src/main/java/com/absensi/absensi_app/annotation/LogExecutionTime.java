package com.absensi.absensi_app.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation untuk mengimplementasikan Phase 1: Aspect-Oriented Programming (AOP).
 * Digunakan untuk melakukan logging eksekusi method secara otomatis.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {
    String value() default ""; // Optional description for the log
}
