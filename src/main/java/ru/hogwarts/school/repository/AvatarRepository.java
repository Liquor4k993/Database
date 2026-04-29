package ru.hogwarts.school.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.hogwarts.school.model.Avatar;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {

    // Поиск аватара по ID студента
    Avatar findByStudentId(Long studentId);

    // ШАГ 2: Пагинация для аватарок
    Page<Avatar> findAll(Pageable pageable);
}