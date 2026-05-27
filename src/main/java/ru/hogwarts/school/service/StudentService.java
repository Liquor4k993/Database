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
import java.util.stream.Collectors;

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
        return studentRepository.findAll();
    }

    public Collection<Student> getStudentsByAgeBetween(int min, int max) {
        logger.info("Was invoked method for get students by age between {} and {}", min, max);
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
        return studentRepository.getTotalStudentsCount();
    }

    public double getAverageAge() {
        logger.info("Was invoked method for get average age of students");
        Double avg = studentRepository.getAverageAge();
        if (avg == null) {
            return 0.0;
        }
        return avg;
    }

    public List<Student> getLastFiveStudents() {
        logger.info("Was invoked method for get last five students");
        return studentRepository.getLastFiveStudents();
    }

    public List<String> getNamesStartingWithA() {
        logger.info("Was invoked method for get names starting with letter A");
        return studentRepository.findAll().stream()
                .map(Student::getName)
                .filter(name -> name != null && name.startsWith("A"))
                .map(String::toUpperCase)
                .sorted()
                .collect(Collectors.toList());
    }

    public double getAverageAgeStream() {
        logger.info("Was invoked method for get average age using stream");
        return studentRepository.findAll().stream()
                .mapToInt(Student::getAge)
                .average()
                .orElse(0.0);
    }

    public void printStudentsParallel() {
        logger.info("Was invoked method for print students in parallel mode");

        List<Student> students = studentRepository.findAll();

        if (students.size() < 6) {
            logger.warn("Not enough students. Need at least 6, but found {}", students.size());
            System.out.println("Not enough students. Please add more students.");
            return;
        }

        List<String> names = students.stream()
                .map(Student::getName)
                .collect(Collectors.toList());

        System.out.println("=== PARALLEL PRINTING MODE ===");

        System.out.println("[MAIN THREAD] " + names.get(0));
        System.out.println("[MAIN THREAD] " + names.get(1));

        Thread thread1 = new Thread(() -> {
            System.out.println("[THREAD-1] " + names.get(2));
            System.out.println("[THREAD-1] " + names.get(3));
        });

        Thread thread2 = new Thread(() -> {
            System.out.println("[THREAD-2] " + names.get(4));
            System.out.println("[THREAD-2] " + names.get(5));
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            logger.error("Thread interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }

        System.out.println("=== PARALLEL PRINTING FINISHED ===");
    }

    public void printStudentsSynchronized() {
        logger.info("Was invoked method for print students in synchronized mode");

        List<Student> students = studentRepository.findAll();

        if (students.size() < 6) {
            logger.warn("Not enough students. Need at least 6, but found {}", students.size());
            System.out.println("Not enough students. Please add more students.");
            return;
        }

        List<String> names = students.stream()
                .map(Student::getName)
                .collect(Collectors.toList());

        System.out.println("=== SYNCHRONIZED PRINTING MODE ===");

        printName("[MAIN THREAD] " + names.get(0));
        printName("[MAIN THREAD] " + names.get(1));

        Thread thread1 = new Thread(() -> {
            printName("[THREAD-1] " + names.get(2));
            printName("[THREAD-1] " + names.get(3));
        });

        Thread thread2 = new Thread(() -> {
            printName("[THREAD-2] " + names.get(4));
            printName("[THREAD-2] " + names.get(5));
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            logger.error("Thread interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }

        System.out.println("=== SYNCHRONIZED PRINTING FINISHED ===");
    }

    private synchronized void printName(String nameWithThread) {
        System.out.println(nameWithThread);
    }
}