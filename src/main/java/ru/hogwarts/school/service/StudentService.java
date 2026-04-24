package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.Collection;
import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // CRUD методы
    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    public Student getStudent(Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    public Student updateStudent(Student student) {
        if (studentRepository.existsById(student.getId())) {
            return studentRepository.save(student);
        }
        return null;
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    public Collection<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Collection<Student> getStudentsByAgeBetween(int min, int max) {
        return studentRepository.findByAgeBetween(min, max);
    }

    public Faculty getStudentFaculty(Long studentId) {
        Student student = getStudent(studentId);
        if (student != null) {
            return student.getFaculty();
        }
        return null;
    }

    // ШАГ 1.1: Получить количество студентов
    public int getTotalStudentsCount() {
        return studentRepository.getTotalStudentsCount();
    }

    // ШАГ 1.2: Получить средний возраст студентов
    public double getAverageAge() {
        Double avg = studentRepository.getAverageAge();
        if (avg == null) {
            return 0.0;
        }
        return avg;
    }

    // ШАГ 1.3: Получить последних 5 студентов
    public List<Student> getLastFiveStudents() {
        return studentRepository.getLastFiveStudents();
    }
}