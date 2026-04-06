package ru.hogwarts.school.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hogwarts.school.model.Student;
import java.util.Collection;

public interface StudentRepository extends JpaRepository<Student, Long> {

    // Поиск студентов по возрасту между min и max
    Collection<Student> findByAgeBetween(int min, int max);

    // Поиск студентов по имени (регистронезависимый)
    Collection<Student> findByNameContainingIgnoreCase(String name);

    // Поиск всех студентов, упорядоченных по возрасту
    Collection<Student> findAllByOrderByAgeAsc();
}