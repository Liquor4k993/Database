package ru.hogwarts.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.FacultyService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FacultyController.class)
class FacultyControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FacultyService facultyService;

    // 1. Тест CREATE (POST)
    @Test
    void testCreateFaculty() throws Exception {
        // Подготовка
        Faculty inputFaculty = new Faculty();
        inputFaculty.setName("Гриффиндор");
        inputFaculty.setColor("Красный");

        Faculty outputFaculty = new Faculty();
        outputFaculty.setId(1L);
        outputFaculty.setName("Гриффиндор");
        outputFaculty.setColor("Красный");

        when(facultyService.createFaculty(any(Faculty.class))).thenReturn(outputFaculty);

        // Действие и проверка
        mockMvc.perform(post("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputFaculty)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Гриффиндор"))
                .andExpect(jsonPath("$.color").value("Красный"));

        verify(facultyService, times(1)).createFaculty(any(Faculty.class));
    }

    // 2. Тест READ (GET by id) - позитивный
    @Test
    void testGetFacultyById() throws Exception {
        // Подготовка
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Гриффиндор");
        faculty.setColor("Красный");

        when(facultyService.getFaculty(1L)).thenReturn(faculty);

        // Действие и проверка
        mockMvc.perform(get("/faculty/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Гриффиндор"))
                .andExpect(jsonPath("$.color").value("Красный"));

        verify(facultyService, times(1)).getFaculty(1L);
    }

    // 3. Негативный кейс: GET by id (404 Not Found)
    @Test
    void testGetFacultyByIdNotFound() throws Exception {
        // Подготовка
        when(facultyService.getFaculty(999L)).thenReturn(null);

        // Действие и проверка
        mockMvc.perform(get("/faculty/999"))
                .andExpect(status().isNotFound());

        verify(facultyService, times(1)).getFaculty(999L);
    }

    // 4. Тест UPDATE (PUT) - позитивный
    @Test
    void testUpdateFaculty() throws Exception {
        // Подготовка
        Faculty inputFaculty = new Faculty();
        inputFaculty.setId(1L);
        inputFaculty.setName("Гриффиндор Обновленный");
        inputFaculty.setColor("Золотой");

        Faculty outputFaculty = new Faculty();
        outputFaculty.setId(1L);
        outputFaculty.setName("Гриффиндор Обновленный");
        outputFaculty.setColor("Золотой");

        when(facultyService.updateFaculty(any(Faculty.class))).thenReturn(outputFaculty);

        // Действие и проверка
        mockMvc.perform(put("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputFaculty)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Гриффиндор Обновленный"))
                .andExpect(jsonPath("$.color").value("Золотой"));

        verify(facultyService, times(1)).updateFaculty(any(Faculty.class));
    }

    // 5. Негативный кейс: UPDATE (404 Not Found)
    @Test
    void testUpdateFacultyNotFound() throws Exception {
        // Подготовка
        Faculty inputFaculty = new Faculty();
        inputFaculty.setId(999L);
        inputFaculty.setName("Несуществующий");
        inputFaculty.setColor("Черный");

        when(facultyService.updateFaculty(any(Faculty.class))).thenReturn(null);

        // Действие и проверка
        mockMvc.perform(put("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputFaculty)))
                .andExpect(status().isNotFound());

        verify(facultyService, times(1)).updateFaculty(any(Faculty.class));
    }

    // 6. Тест DELETE
    @Test
    void testDeleteFaculty() throws Exception {
        // Подготовка
        doNothing().when(facultyService).deleteFaculty(1L);

        // Действие и проверка
        mockMvc.perform(delete("/faculty/1"))
                .andExpect(status().isNoContent());

        verify(facultyService, times(1)).deleteFaculty(1L);
    }

    // 7. Тест GET ALL
    @Test
    void testGetAllFaculties() throws Exception {
        // Подготовка
        List<Faculty> faculties = Arrays.asList(
                createFaculty(1L, "Гриффиндор", "Красный"),
                createFaculty(2L, "Слизерин", "Зеленый"),
                createFaculty(3L, "Когтевран", "Синий")
        );

        when(facultyService.getAllFaculties()).thenReturn(faculties);

        // Действие и проверка
        mockMvc.perform(get("/faculty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Гриффиндор"))
                .andExpect(jsonPath("$[1].name").value("Слизерин"))
                .andExpect(jsonPath("$[2].name").value("Когтевран"));

        verify(facultyService, times(1)).getAllFaculties();
    }

    // 8. Тест поиска факультета по имени или цвету (filter)
    @Test
    void testGetFacultiesByNameOrColor() throws Exception {
        // Подготовка
        List<Faculty> faculties = Arrays.asList(
                createFaculty(1L, "Гриффиндор", "Красный"),
                createFaculty(2L, "Слизерин", "Зеленый")
        );

        when(facultyService.getFacultiesByNameOrColor("Гриф")).thenReturn(
                Collections.singletonList(createFaculty(1L, "Гриффиндор", "Красный"))
        );

        // Действие и проверка
        mockMvc.perform(get("/faculty/filter")
                        .param("nameOrColor", "Гриф"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Гриффиндор"));

        verify(facultyService, times(1)).getFacultiesByNameOrColor("Гриф");
    }

    // 9. Тест получения студентов факультета - позитивный
    @Test
    void testGetFacultyStudents() throws Exception {
        // Подготовка
        List<Student> students = Arrays.asList(
                createStudent(1L, "Гарри Поттер", 11),
                createStudent(2L, "Гермиона Грейнджер", 12)
        );

        when(facultyService.getFacultyStudents(1L)).thenReturn(students);

        // Действие и проверка
        mockMvc.perform(get("/faculty/1/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Гарри Поттер"))
                .andExpect(jsonPath("$[1].name").value("Гермиона Грейнджер"));

        verify(facultyService, times(1)).getFacultyStudents(1L);
    }

    // 10. Негативный кейс: получение студентов несуществующего факультета
    @Test
    void testGetFacultyStudentsNotFound() throws Exception {
        // Подготовка
        when(facultyService.getFacultyStudents(999L)).thenReturn(null);

        // Действие и проверка
        mockMvc.perform(get("/faculty/999/students"))
                .andExpect(status().isNotFound());

        verify(facultyService, times(1)).getFacultyStudents(999L);
    }

    private Faculty createFaculty(Long id, String name, String color) {
        Faculty faculty = new Faculty();
        faculty.setId(id);
        faculty.setName(name);
        faculty.setColor(color);
        return faculty;
    }

    private Student createStudent(Long id, String name, int age) {
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setAge(age);
        return student;
    }
}