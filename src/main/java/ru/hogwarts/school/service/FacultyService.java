package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.Collection;
import java.util.Comparator;

@Service
public class FacultyService {

    private static final Logger logger = LoggerFactory.getLogger(FacultyService.class);

    private final FacultyRepository facultyRepository;

    @Autowired
    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public Faculty createFaculty(Faculty faculty) {
        logger.info("Was invoked method for create faculty");
        logger.debug("Creating faculty with name: {}", faculty.getName());
        Faculty saved = facultyRepository.save(faculty);
        logger.info("Faculty created with id: {}", saved.getId());
        return saved;
    }

    public Faculty getFaculty(Long id) {
        logger.info("Was invoked method for get faculty by id: {}", id);
        return facultyRepository.findById(id).orElse(null);
    }

    public Faculty updateFaculty(Faculty faculty) {
        logger.info("Was invoked method for update faculty with id: {}", faculty.getId());
        if (facultyRepository.existsById(faculty.getId())) {
            Faculty updated = facultyRepository.save(faculty);
            logger.info("Faculty with id {} updated successfully", faculty.getId());
            return updated;
        }
        logger.warn("Attempt to update non-existent faculty with id: {}", faculty.getId());
        return null;
    }

    public void deleteFaculty(Long id) {
        logger.info("Was invoked method for delete faculty with id: {}", id);
        if (!facultyRepository.existsById(id)) {
            logger.error("Cannot delete faculty: there is no faculty with id = {}", id);
            throw new RuntimeException("Faculty not found with id: " + id);
        }
        facultyRepository.deleteById(id);
        logger.info("Faculty with id {} deleted successfully", id);
    }

    public Collection<Faculty> getAllFaculties() {
        logger.info("Was invoked method for get all faculties");
        Collection<Faculty> faculties = facultyRepository.findAll();
        logger.debug("Found {} faculties", faculties.size());
        return faculties;
    }

    public Collection<Faculty> getFacultiesByNameOrColor(String nameOrColor) {
        logger.info("Was invoked method for get faculties by name or color: {}", nameOrColor);
        return facultyRepository.findByNameContainingIgnoreCaseOrColorContainingIgnoreCase(nameOrColor, nameOrColor);
    }

    public Collection<Student> getFacultyStudents(Long facultyId) {
        logger.info("Was invoked method for get students of faculty with id: {}", facultyId);
        Faculty faculty = getFaculty(facultyId);
        if (faculty == null) {
            logger.error("Faculty not found with id: {}", facultyId);
            return null;
        }
        Collection<Student> students = faculty.getStudents();
        logger.debug("Faculty {} has {} students", faculty.getName(), students.size());
        return students;
    }
    public String getLongestFacultyName() {
        logger.info("Was invoked method for get longest faculty name");

        String longestName = facultyRepository.findAll().stream()
                .map(Faculty::getName)
                .filter(name -> name != null)
                .max(Comparator.comparingInt(String::length))
                .orElse("");

        logger.debug("Longest faculty name: {}", longestName);
        return longestName;
    }
}