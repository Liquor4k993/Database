package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.Collection;
import java.util.List;

@Service
public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student createStudent(Student student) {
        logger.info("Was invoked method for create student");
        logger.debug("Creating student with name: {}", student.getName());
        Student saved = studentRepository.save(student);
        logger.info("Student created with id: {}", saved.getId());
        return saved;
    }

    public Student getStudent(Long id) {
        logger.info("Was invoked method for get student by id: {}", id);
        return studentRepository.findById(id).orElse(null);
    }

    public Student updateStudent(Student student) {
        logger.info("Was invoked method for update student with id: {}", student.getId());
        if (studentRepository.existsById(student.getId())) {
            Student updated = studentRepository.save(student);
            logger.info("Student with id {} updated successfully", student.getId());
            return updated;
        }
        logger.warn("Attempt to update non-existent student with id: {}", student.getId());
        return null;
    }

    public void deleteStudent(Long id) {
        logger.info("Was invoked method for delete student with id: {}", id);
        if (!studentRepository.existsById(id)) {
            logger.error("Cannot delete student: there is no student with id = {}", id);
            throw new RuntimeException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
        logger.info("Student with id {} deleted successfully", id);
    }

    public Collection<Student> getAllStudents() {
        logger.info("Was invoked method for get all students");
        Collection<Student> students = studentRepository.findAll();
        logger.debug("Found {} students", students.size());
        return students;
    }

    public Collection<Student> getStudentsByAgeBetween(int min, int max) {
        logger.info("Was invoked method for get students by age between {} and {}", min, max);
        if (min > max) {
            logger.warn("Invalid age range: min={} > max={}", min, max);
        }
        return studentRepository.findByAgeBetween(min, max);
    }

    public Faculty getStudentFaculty(Long studentId) {
        logger.info("Was invoked method for get faculty of student with id: {}", studentId);
        Student student = getStudent(studentId);
        if (student == null) {
            logger.error("Student not found with id: {}", studentId);
            return null;
        }
        return student.getFaculty();
    }

    public int getTotalStudentsCount() {
        logger.info("Was invoked method for get total students count");
        int count = studentRepository.getTotalStudentsCount();
        logger.debug("Total students count: {}", count);
        return count;
    }

    public double getAverageAge() {
        logger.info("Was invoked method for get average age of students");
        Double avg = studentRepository.getAverageAge();
        if (avg == null) {
            logger.warn("No students found, returning average age as 0.0");
            return 0.0;
        }
        logger.debug("Average age of students: {}", avg);
        return avg;
    }

    public List<Student> getLastFiveStudents() {
        logger.info("Was invoked method for get last five students");
        List<Student> lastFive = studentRepository.getLastFiveStudents();
        logger.debug("Retrieved {} last students", lastFive.size());
        return lastFive;
    }
}