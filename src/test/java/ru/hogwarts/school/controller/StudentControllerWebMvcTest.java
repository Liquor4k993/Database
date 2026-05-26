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
import ru.hogwarts.school.service.StudentService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class StudentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    // 1. Тест CREATE (POST)
    @Test
    void testCreateStudent() throws Exception {
        // Подготовка
        Student inputStudent = new Student();
        inputStudent.setName("Гарри Поттер");
        inputStudent.setAge(11);

        Student outputStudent = new Student();
        outputStudent.setId(1L);
        outputStudent.setName("Гарри Поттер");
        outputStudent.setAge(11);

        when(studentService.createStudent(any(Student.class))).thenReturn(outputStudent);

        // Действие и проверка
        mockMvc.perform(post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputStudent)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Гарри Поттер"))
                .andExpect(jsonPath("$.age").value(11));

        verify(studentService, times(1)).createStudent(any(Student.class));
    }

    // 2. Тест READ (GET by id) - позитивный
    @Test
    void testGetStudentById() throws Exception {
        // Подготовка
        Student student = new Student();
        student.setId(1L);
        student.setName("Гарри Поттер");
        student.setAge(11);

        when(studentService.getStudent(1L)).thenReturn(student);

        // Действие и проверка
        mockMvc.perform(get("/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Гарри Поттер"))
                .andExpect(jsonPath("$.age").value(11));

        verify(studentService, times(1)).getStudent(1L);
    }

    // 3. Негативный кейс: GET by id (404 Not Found)
    @Test
    void testGetStudentByIdNotFound() throws Exception {
        // Подготовка
        when(studentService.getStudent(999L)).thenReturn(null);

        // Действие и проверка
        mockMvc.perform(get("/student/999"))
                .andExpect(status().isNotFound());

        verify(studentService, times(1)).getStudent(999L);
    }

    // 4. Тест UPDATE (PUT) - позитивный
    @Test
    void testUpdateStudent() throws Exception {
        // Подготовка
        Student inputStudent = new Student();
        inputStudent.setId(1L);
        inputStudent.setName("Гарри Поттер Обновленный");
        inputStudent.setAge(12);

        Student outputStudent = new Student();
        outputStudent.setId(1L);
        outputStudent.setName("Гарри Поттер Обновленный");
        outputStudent.setAge(12);

        when(studentService.updateStudent(any(Student.class))).thenReturn(outputStudent);

        // Действие и проверка
        mockMvc.perform(put("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputStudent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Гарри Поттер Обновленный"))
                .andExpect(jsonPath("$.age").value(12));

        verify(studentService, times(1)).updateStudent(any(Student.class));
    }

    // 5. Негативный кейс: UPDATE (404 Not Found)
    @Test
    void testUpdateStudentNotFound() throws Exception {
        // Подготовка
        Student inputStudent = new Student();
        inputStudent.setId(999L);
        inputStudent.setName("Несуществующий");
        inputStudent.setAge(99);

        when(studentService.updateStudent(any(Student.class))).thenReturn(null);

        // Действие и проверка
        mockMvc.perform(put("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputStudent)))
                .andExpect(status().isNotFound());

        verify(studentService, times(1)).updateStudent(any(Student.class));
    }

    // 6. Тест DELETE
    @Test
    void testDeleteStudent() throws Exception {
        // Подготовка
        doNothing().when(studentService).deleteStudent(1L);

        // Действие и проверка
        mockMvc.perform(delete("/student/1"))
                .andExpect(status().isNoContent());

        verify(studentService, times(1)).deleteStudent(1L);
    }

    // 7. Тест GET ALL
    @Test
    void testGetAllStudents() throws Exception {
        // Подготовка
        List<Student> students = Arrays.asList(
                createStudent(1L, "Гарри Поттер", 11),
                createStudent(2L, "Гермиона Грейнджер", 12),
                createStudent(3L, "Рон Уизли", 11)
        );

        when(studentService.getAllStudents()).thenReturn(students);

        // Действие и проверка
        mockMvc.perform(get("/student"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Гарри Поттер"))
                .andExpect(jsonPath("$[1].name").value("Гермиона Грейнджер"))
                .andExpect(jsonPath("$[2].name").value("Рон Уизли"));

        verify(studentService, times(1)).getAllStudents();
    }

    // 8. Тест GET студентов по возрасту (filter) - позитивный
    @Test
    void testGetStudentsByAgeBetween() throws Exception {
        // Подготовка
        List<Student> students = Arrays.asList(
                createStudent(1L, "Гарри Поттер", 11),
                createStudent(2L, "Рон Уизли", 11)
        );

        when(studentService.getStudentsByAgeBetween(10, 12)).thenReturn(students);

        // Действие и проверка
        mockMvc.perform(get("/student/filter")
                        .param("min", "10")
                        .param("max", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].age").value(11))
                .andExpect(jsonPath("$[1].age").value(11));

        verify(studentService, times(1)).getStudentsByAgeBetween(10, 12);
    }

    // 9. Негативный кейс: filter с min > max
    @Test
    void testGetStudentsByAgeBetweenInvalidParams() throws Exception {
        // Действие и проверка
        mockMvc.perform(get("/student/filter")
                        .param("min", "20")
                        .param("max", "10"))
                .andExpect(status().isBadRequest());

        verify(studentService, never()).getStudentsByAgeBetween(anyInt(), anyInt());
    }

    // 10. Тест GET факультета студента
    @Test
    void testGetStudentFaculty() throws Exception {
        // Подготовка
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Гриффиндор");
        faculty.setColor("Красный");

        when(studentService.getStudentFaculty(1L)).thenReturn(faculty);

        // Действие и проверка
        mockMvc.perform(get("/student/1/faculty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Гриффиндор"))
                .andExpect(jsonPath("$.color").value("Красный"));

        verify(studentService, times(1)).getStudentFaculty(1L);
    }

    // 11. Негативный кейс: GET факультета несуществующего студента
    @Test
    void testGetStudentFacultyNotFound() throws Exception {
        // Подготовка
        when(studentService.getStudentFaculty(999L)).thenReturn(null);

        // Действие и проверка
        mockMvc.perform(get("/student/999/faculty"))
                .andExpect(status().isNotFound());

        verify(studentService, times(1)).getStudentFaculty(999L);
    }

    private Student createStudent(Long id, String name, int age) {
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setAge(age);
        return student;
    }
}