package ru.hogwarts.school.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.hogwarts.school.model.Student;

import java.util.Collection;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    // Поиск студентов по возрасту между min и max
    Collection<Student> findByAgeBetween(int min, int max);

    // Поиск студентов по имени (регистронезависимый)
    Collection<Student> findByNameContainingIgnoreCase(String name);

    // Поиск всех студентов, упорядоченных по возрасту
    Collection<Student> findAllByOrderByAgeAsc();

    // ШАГ 1.1: Получить количество всех студентов
    @Query("SELECT COUNT(s) FROM Student s")
    int getTotalStudentsCount();

    // ШАГ 1.2: Получить средний возраст студентов
    @Query("SELECT AVG(s.age) FROM Student s")
    double getAverageAge();

    // ШАГ 1.3: Получить последних 5 студентов (по убыванию ID)
    @Query(value = "SELECT * FROM student ORDER BY id DESC LIMIT 5", nativeQuery = true)
    List<Student> getLastFiveStudents();
}