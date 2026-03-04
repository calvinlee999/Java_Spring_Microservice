package com.example.university.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AppConfig — application-level Spring configuration.
 *
 * <p>A {@code @Configuration} class is a regular Java class where you define
 * {@link Bean}s (Spring-managed objects).  Think of it as a recipe book:
 * each {@code @Bean} method is one recipe that Spring follows at startup to
 * create and register an object in its container.
 *
 * <p><b>Why have a separate config class?</b>
 * The main {@code UniversityApplication.java} class should stay small and focused
 * on booting Spring Boot.  Complex configuration logic belongs here, keeping
 * concerns separated.
 *
 * <p><b>Beans defined here:</b>
 * <ul>
 *   <li>{@link #startupBanner()} — logs important URLs when the app starts</li>
 * </ul>
 *
 * <p><b>Principal architect note:</b>
 * This class is the right place to add beans like custom
 * {@code PasswordEncoder}, {@code ObjectMapper} configuration,
 * feature-flag beans, or any other application-wide singleton.
 */
@Configuration
public class AppConfig {

    /**
     * SLF4J logger for startup messages.
     * Using a class-level logger (not a method-local one) is the standard
     * practice — one logger per class, reused for all log statements.
     */
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    /**
     * Prints a startup banner to the log as soon as the application context
     * finishes loading and the server is ready to accept requests.
     *
     * <p><b>Spring Boot Feature: {@link ApplicationRunner}</b>
     * An {@link ApplicationRunner} is a functional interface with one method:
     * {@code run(ApplicationArguments args)}.  Spring Boot calls every
     * {@link ApplicationRunner} bean automatically, <em>after</em> the full
     * context is ready but <em>before</em> the app starts accepting traffic.
     * It's perfect for one-time startup tasks like logging URLs, warming caches,
     * or validating external service connections.
     *
     * <p><b>Think of it like a welcome announcement:</b><br>
     * When a school opens its doors in the morning, a PA announcement says
     * "Good morning, the library is open, the cafeteria is open, the gym is
     * scheduled for maintenance."  This banner does the same thing for the app.
     *
     * @return an {@link ApplicationRunner} that logs useful URLs on startup
     */
    @Bean
    public ApplicationRunner startupBanner() {
        return args -> {
            log.info("");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("  University Modern — Application Ready");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("");
            log.info("  REST API (custom controller):");
            log.info("    List courses    →  GET  http://localhost:8080/api/courses");
            log.info("    Filter courses  →  GET  http://localhost:8080/api/courses/filter?name=...");
            log.info("    By credits      →  GET  http://localhost:8080/api/courses/credits/3");
            log.info("    External API    →  GET  http://localhost:8080/api/courses/external/Java");
            log.info("");
            log.info("  Spring HATEOAS (auto-generated REST):");
            log.info("    Courses         →  GET  http://localhost:8080/courses");
            log.info("    Staff           →  GET  http://localhost:8080/staff");
            log.info("    Students        →  GET  http://localhost:8080/students");
            log.info("    Departments     →  GET  http://localhost:8080/departments");
            log.info("");
            log.info("  Developer Tools:");
            log.info("    Swagger UI      →  http://localhost:8080/swagger-ui.html");
            log.info("    OpenAPI JSON    →  http://localhost:8080/v3/api-docs");
            log.info("");
            log.info("  Spring Boot Actuator (health / Kubernetes probes):");
            log.info("    Full health     →  http://localhost:8080/actuator/health");
            log.info("    Readiness probe →  http://localhost:8080/actuator/health/readiness");
            log.info("    Liveness probe  →  http://localhost:8080/actuator/health/liveness");
            log.info("    Metrics         →  http://localhost:8080/actuator/metrics");
            log.info("");
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("");
        };
    }
}
