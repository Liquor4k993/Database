package ru.hogwarts.school.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StudentControllerTestRestTemplateTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StudentRepository studentRepository;

    private String baseUrl;
    private Student testStudent;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/student";

        // Создаем тестового студента для использования в тестах
        testStudent = new Student();
        testStudent.setName("Гарри Поттер");
        testStudent.setAge(11);
        testStudent = studentRepository.save(testStudent);
    }

    @AfterEach
    void tearDown() {
        // Очищаем базу после каждого теста
        studentRepository.deleteAll();
    }

    // 1. Тест CREATE (POST)
    @Test
    void testCreateStudent() {
        // Подготовка
        Student newStudent = new Student();
        newStudent.setName("Гермиона Грейнджер");
        newStudent.setAge(12);

        // Действие
        ResponseEntity<Student> response = restTemplate.postForEntity(
                baseUrl,
                newStudent,
                Student.class
        );

        // Проверка
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Гермиона Грейнджер", response.getBody().getName());
        assertEquals(12, response.getBody().getAge());

        // Проверка, что студент реально сохранился в БД
        assertTrue(studentRepository.findById(response.getBody().getId()).isPresent());
    }

    // 2. Тест READ (GET by id)
    @Test
    void testGetStudentById() {
        // Действие
        ResponseEntity<Student> response = restTemplate.getForEntity(
                baseUrl + "/" + testStudent.getId(),
                Student.class
        );

        // Проверка
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testStudent.getId(), response.getBody().getId());
        assertEquals(testStudent.getName(), response.getBody().getName());
        assertEquals(testStudent.getAge(), response.getBody().getAge());
    }

    // 3. Негативный кейс: GET by id (404 Not Found)
    @Test
    void testGetStudentByIdNotFound() {
        // Действие - ищем несуществующего студента
        ResponseEntity<Student> response = restTemplate.getForEntity(
                baseUrl + "/999",
                Student.class
        );

        // Проверка
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    // 4. Тест UPDATE (PUT)
    @Test
    void testUpdateStudent() {
        // Подготовка
        testStudent.setName("Гарри Поттер Обновленный");
        testStudent.setAge(12);

        // Действие
        HttpEntity<Student> requestEntity = new HttpEntity<>(testStudent);
        ResponseEntity<Student> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                requestEntity,
                Student.class
        );

        // Проверка
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testStudent.getId(), response.getBody().getId());
        assertEquals("Гарри Поттер Обновленный", response.getBody().getName());
        assertEquals(12, response.getBody().getAge());

        // Проверка в БД
        Student updatedFromDb = studentRepository.findById(testStudent.getId()).orElse(null);
        assertNotNull(updatedFromDb);
        assertEquals("Гарри Поттер Обновленный", updatedFromDb.getName());
    }

    // 5. Негативный кейс: UPDATE несуществующего студента
    @Test
    void testUpdateStudentNotFound() {
        // Подготовка
        Student nonExistentStudent = new Student();
        nonExistentStudent.setId(999L);
        nonExistentStudent.setName("Несуществующий");
        nonExistentStudent.setAge(99);

        // Действие
        HttpEntity<Student> requestEntity = new HttpEntity<>(nonExistentStudent);
        ResponseEntity<Student> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                requestEntity,
                Student.class
        );

        // Проверка
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    // 6. Тест DELETE
    @Test
    void testDeleteStudent() {
        // Действие
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + testStudent.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Проверка
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Проверка, что студент удален из БД
        assertFalse(studentRepository.findById(testStudent.getId()).isPresent());
    }

    // 7. Тест GET ALL (получить всех студентов)
    @Test
    void testGetAllStudents() {
        // Подготовка - добавляем еще одного студента
        Student student2 = new Student();
        student2.setName("Рон Уизли");
        student2.setAge(11);
        studentRepository.save(student2);

        // Действие
        ResponseEntity<List<Student>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Student>>() {}
        );

        // Проверка
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() >= 2);
    }

    // 8. Тест GET студентов по возрасту (filter)
    @Test
    void testGetStudentsByAgeBetween() {
        // Подготовка - создаем студентов с разным возрастом
        Student youngStudent = new Student();
        youngStudent.setName("Молодой студент");
        youngStudent.setAge(10);
        studentRepository.save(youngStudent);

        Student oldStudent = new Student();
        oldStudent.setName("Старый студент");
        oldStudent.setAge(25);
        studentRepository.save(oldStudent);

        // Действие
        ResponseEntity<List<Student>> response = restTemplate.exchange(
                baseUrl + "/filter?min=10&max=15",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Student>>() {}
        );

        // Проверка
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Должны быть: testStudent (11 лет) и youngStudent (10 лет)
        assertTrue(response.getBody().size() >= 2);
        // Проверяем, что все студенты в ответе имеют возраст от 10 до 15
        for (Student student : response.getBody()) {
            assertTrue(student.getAge() >= 10 && student.getAge() <= 15);
        }
    }

    // 9. Негативный кейс: filter с min > max
    @Test
    void testGetStudentsByAgeBetweenInvalidParams() {
        // Действие
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/filter?min=20&max=10",
                String.class
        );

        // Проверка
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}