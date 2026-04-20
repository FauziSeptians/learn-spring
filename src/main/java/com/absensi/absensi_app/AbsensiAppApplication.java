package com.absensi.absensi_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.absensi.absensi_app")
public class AbsensiAppApplication {

  public static void main(String[] args) {
    SpringApplication.run(AbsensiAppApplication.class, args);
  }
}
