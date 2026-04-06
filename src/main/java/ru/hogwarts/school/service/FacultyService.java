package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.Collection;

@Service
public class FacultyService {

    private final FacultyRepository facultyRepository;

    @Autowired
    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public Faculty createFaculty(Faculty faculty) {
        return facultyRepository.save(faculty);
    }

    public Faculty getFaculty(Long id) {
        return facultyRepository.findById(id).orElse(null);
    }

    public Faculty updateFaculty(Faculty faculty) {
        if (facultyRepository.existsById(faculty.getId())) {
            return facultyRepository.save(faculty);
        }
        return null;
    }

    public void deleteFaculty(Long id) {
        facultyRepository.deleteById(id);
    }

    public Collection<Faculty> getAllFaculties() {
        return facultyRepository.findAll();
    }

    // Поиск факультета по имени или цвету (регистронезависимый)
    public Collection<Faculty> getFacultiesByNameOrColor(String nameOrColor) {
        return facultyRepository.findByNameContainingIgnoreCaseOrColorContainingIgnoreCase(nameOrColor, nameOrColor);
    }

    // Получить всех студентов факультета
    public Collection<Student> getFacultyStudents(Long facultyId) {
        Faculty faculty = getFaculty(facultyId);
        if (faculty != null) {
            return faculty.getStudents();
        }
        return null;
    }
}