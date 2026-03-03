package com.example.university;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * UniversityApplication — The starting point of the entire program.
 *
 * <p> {@code @SpringBootApplication} is like a "power button" annotation.
 * It tells Spring to:
 * <ol>
 *   <li>Scan every class in this package for components (controllers, services, repos).</li>
 *   <li>Auto-configure everything it finds (database, web server, etc.).</li>
 *   <li>Start an embedded Tomcat HTTP server on port 8080.</li>
 * </ol>
 *
 * <p><b>Spring Boot 3.2+ feature used here:</b> Virtual Threads.
 * Because {@code spring.threads.virtual.enabled=true} is set in
 * application.properties, Spring Boot automatically replaces Tomcat's
 * fixed thread pool with Java 21 virtual threads.  Every incoming HTTP
 * request gets its own cheap virtual thread instead of a precious OS thread.
 */
@SpringBootApplication
public class UniversityApplication {

    /**
     * The main() method — just like any other Java program.
     * SpringApplication.run() boots the whole application in one line.
     *
     * @param args  command-line arguments (passed in from the OS, usually empty)
     */
    public static void main(String[] args) {
        SpringApplication.run(UniversityApplication.class, args);
    }
}
