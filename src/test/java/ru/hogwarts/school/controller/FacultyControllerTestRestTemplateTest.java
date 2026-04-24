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
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FacultyControllerTestRestTemplateTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private StudentRepository studentRepository;

    private String baseUrl;
    private Faculty testFaculty;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/faculty";

        // Создаем тестовый факультет
        testFaculty = new Faculty();
        testFaculty.setName("Гриффиндор");
        testFaculty.setColor("Красный");
        testFaculty = facultyRepository.save(testFaculty);
    }

    @AfterEach
    void tearDown() {
        // Очищаем базу
        studentRepository.deleteAll();
        facultyRepository.deleteAll();
    }

    // 1. Тест CREATE (POST)
    @Test
    void testCreateFaculty() {
        // Подготовка
        Faculty newFaculty = new Faculty();
        newFaculty.setName("Слизерин");
        newFaculty.setColor("Зеленый");

        // Действие
        ResponseEntity<Faculty> response = restTemplate.postForEntity(
                baseUrl,
                newFaculty,
                Faculty.class
        );

        // Проверка
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Слизерин", response.getBody().getName());
        assertEquals("Зеленый", response.getBody().getColor());

        // Проверка в БД
        assertTrue(facultyRepository.findById(response.getBody().getId()).isPresent());
    }

    // 2. Тест READ (GET by id)
    @Test
    void testGetFacultyById() {
        // Действие
        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                baseUrl + "/" + testFaculty.getId(),
                Faculty.class
        );

        // Проверка
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testFaculty.getId(), response.getBody().getId());
        assertEquals(testFaculty.getName(), response.getBody().getName());
        assertEquals(testFaculty.getColor(), response.getBody().getColor());
    }

    // 3. Негативный кейс: GET by id (404 Not Found)
    @Test
    void testGetFacultyByIdNotFound() {
        // Действие
        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                baseUrl + "/999",
                Faculty.class
        );

        // Проверка
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    // 4. Тест UPDATE (PUT)
    @Test
    void testUpdateFaculty() {
        // Подготовка
        testFaculty.setName("Гриффиндор Обновленный");
        testFaculty.setColor("Золотой");

        // Действие
        HttpEntity<Faculty> requestEntity = new HttpEntity<>(testFaculty);
        ResponseEntity<Faculty> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                requestEntity,
                Faculty.class
        );

        // Проверка
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testFaculty.getId(), response.getBody().getId());
        assertEquals("Гриффиндор Обновленный", response.getBody().getName());
        assertEquals("Золотой", response.getBody().getColor());

        // Проверка в БД
        Faculty updatedFromDb = facultyRepository.findById(testFaculty.getId()).orElse(null);
        assertNotNull(updatedFromDb);
        assertEquals("Гриффиндор Обновленный", updatedFromDb.getName());
    }

    // 5. Негативный кейс: UPDATE несуществующего факультета
    @Test
    void testUpdateFacultyNotFound() {
        // Подготовка
        Faculty nonExistentFaculty = new Faculty();
        nonExistentFaculty.setId(999L);
        nonExistentFaculty.setName("Несуществующий");
        nonExistentFaculty.setColor("Черный");

        // Действие
        HttpEntity<Faculty> requestEntity = new HttpEntity<>(nonExistentFaculty);
        ResponseEntity<Faculty> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                requestEntity,
                Faculty.class
        );

        // Проверка
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    // 6. Тест DELETE
    @Test
    void testDeleteFaculty() {
        // Действие
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + testFaculty.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Проверка
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Проверка, что факультет удален из БД
        assertFalse(facultyRepository.findById(testFaculty.getId()).isPresent());
    }

    // 7. Тест GET ALL (получить все факультеты)
    @Test
    void testGetAllFaculties() {
        // Подготовка
        Faculty faculty2 = new Faculty();
        faculty2.setName("Слизерин");
        faculty2.setColor("Зеленый");
        facultyRepository.save(faculty2);

        // Действие
        ResponseEntity<List<Faculty>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Faculty>>() {}
        );

        // Проверка
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() >= 2);
    }

    // 8. Тест поиска факультета по имени или цвету (filter)
    @Test
    void testGetFacultiesByNameOrColor() {
        // Подготовка
        Faculty faculty2 = new Faculty();
        faculty2.setName("Слизерин");
        faculty2.setColor("Зеленый");
        facultyRepository.save(faculty2);

        // Действие - ищем по имени
        ResponseEntity<List<Faculty>> responseByName = restTemplate.exchange(
                baseUrl + "/filter?nameOrColor=Гриф",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Faculty>>() {}
        );

        // Проверка по имени
        assertEquals(HttpStatus.OK, responseByName.getStatusCode());
        assertNotNull(responseByName.getBody());
        assertTrue(responseByName.getBody().stream()
                .anyMatch(f -> f.getName().equalsIgnoreCase("Гриффиндор")));

        // Действие - ищем по цвету
        ResponseEntity<List<Faculty>> responseByColor = restTemplate.exchange(
                baseUrl + "/filter?nameOrColor=Зеленый",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Faculty>>() {}
        );

        // Проверка по цвету
        assertEquals(HttpStatus.OK, responseByColor.getStatusCode());
        assertNotNull(responseByColor.getBody());
        assertTrue(responseByColor.getBody().stream()
                .anyMatch(f -> f.getColor().equalsIgnoreCase("Зеленый")));
    }

    // 9. Тест получения студентов факультета
    @Test
    void testGetFacultyStudents() {
        // Подготовка - создаем студентов и привязываем к факультету
        Student student1 = new Student();
        student1.setName("Гарри Поттер");
        student1.setAge(11);
        student1.setFaculty(testFaculty);
        studentRepository.save(student1);

        Student student2 = new Student();
        student2.setName("Гермиона Грейнджер");
        student2.setAge(12);
        student2.setFaculty(testFaculty);
        studentRepository.save(student2);

        // Действие
        ResponseEntity<List<Student>> response = restTemplate.exchange(
                baseUrl + "/" + testFaculty.getId() + "/students",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Student>>() {}
        );

        // Проверка
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().stream()
                .anyMatch(s -> s.getName().equals("Гарри Поттер")));
        assertTrue(response.getBody().stream()
                .anyMatch(s -> s.getName().equals("Гермиона Грейнджер")));
    }

    // 10. Негативный кейс: получение студентов несуществующего факультета
    @Test
    void testGetFacultyStudentsNotFound() {
        // Действие
        ResponseEntity<List<Student>> response = restTemplate.exchange(
                baseUrl + "/999/students",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Student>>() {}
        );

        // Проверка
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}