package ru.practicum.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EndpointHitTest {

    private ObjectMapper objectMapper;
    private Validator validator;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidEndpointHit() {
        EndpointHit hit = new EndpointHit(
                "ewm-main-service",
                "/events/1",
                "192.163.0.1",
                LocalDateTime.of(2022, 9, 6, 11, 0, 23)
        );

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(hit);

        assertTrue(violations.isEmpty());
        assertEquals("ewm-main-service", hit.getApp());
        assertEquals("/events/1", hit.getUri());
        assertEquals("192.163.0.1", hit.getIp());
        assertNotNull(hit.getTimestamp());
    }

    @Test
    void testInvalidEndpointHit_BlankApp() {
        EndpointHit hit = new EndpointHit(
                "",
                "/events/1",
                "192.163.0.1",
                LocalDateTime.now()
        );

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(hit);

        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Идентификатор сервиса"));
    }

    @Test
    void testInvalidEndpointHit_BlankUri() {
        EndpointHit hit = new EndpointHit(
                "ewm-main-service",
                "",
                "192.163.0.1",
                LocalDateTime.now()
        );

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(hit);

        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("URI"));
    }

    @Test
    void testInvalidEndpointHit_BlankIp() {
        EndpointHit hit = new EndpointHit(
                "ewm-main-service",
                "/events/1",
                "",
                LocalDateTime.now()
        );

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(hit);

        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("IP-адрес"));
    }

    @Test
    void testInvalidEndpointHit_NullTimestamp() {
        EndpointHit hit = new EndpointHit(
                "ewm-main-service",
                "/events/1",
                "192.163.0.1",
                null
        );

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(hit);

        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Время запроса"));
    }

    @Test
    void testJsonSerialization() throws Exception {
        EndpointHit hit = new EndpointHit(
                "ewm-main-service",
                "/events/1",
                "192.163.0.1",
                LocalDateTime.of(2022, 9, 6, 11, 0, 23)
        );

        String json = objectMapper.writeValueAsString(hit);

        assertTrue(json.contains("\"timestamp\":\"2022-09-06 11:00:23\""));
        assertTrue(json.contains("\"app\":\"ewm-main-service\""));
        assertTrue(json.contains("\"uri\":\"/events/1\""));
        assertTrue(json.contains("\"ip\":\"192.163.0.1\""));
    }

    @Test
    void testJsonDeserialization() throws Exception {
        String json = "{"
                + "\"app\": \"ewm-main-service\","
                + "\"uri\": \"/events/1\","
                + "\"ip\": \"192.163.0.1\","
                + "\"timestamp\": \"2022-09-06 11:00:23\""
                + "}";

        EndpointHit hit = objectMapper.readValue(json, EndpointHit.class);

        assertEquals("ewm-main-service", hit.getApp());
        assertEquals("/events/1", hit.getUri());
        assertEquals("192.163.0.1", hit.getIp());
        assertEquals(LocalDateTime.of(2022, 9, 6, 11, 0, 23), hit.getTimestamp());
    }

    @Test
    void testNoArgsConstructor() {
        EndpointHit hit = new EndpointHit();

        assertNotNull(hit);
        assertNull(hit.getApp());
        assertNull(hit.getUri());
        assertNull(hit.getIp());
        assertNull(hit.getTimestamp());
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime timestamp = LocalDateTime.of(2022, 9, 6, 11, 0, 23);
        EndpointHit hit1 = new EndpointHit("ewm-main-service", "/events/1", "192.163.0.1", timestamp);
        EndpointHit hit2 = new EndpointHit("ewm-main-service", "/events/1", "192.163.0.1", timestamp);
        EndpointHit hit3 = new EndpointHit("other-service", "/events/1", "192.163.0.1", timestamp);

        assertEquals(hit1, hit2);
        assertEquals(hit1.hashCode(), hit2.hashCode());
        assertNotEquals(hit1, hit3);
        assertNotEquals(hit1.hashCode(), hit3.hashCode());
    }

    @Test
    void testToString() {
        EndpointHit hit = new EndpointHit(
                "ewm-main-service",
                "/events/1",
                "192.163.0.1",
                LocalDateTime.of(2022, 9, 6, 11, 0, 23)
        );

        String toString = hit.toString();

        assertTrue(toString.contains("ewm-main-service"));
        assertTrue(toString.contains("/events/1"));
        assertTrue(toString.contains("192.163.0.1"));
    }
}
