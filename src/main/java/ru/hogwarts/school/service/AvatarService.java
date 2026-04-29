package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Transactional
public class AvatarService {

    private static final Logger logger = LoggerFactory.getLogger(AvatarService.class);

    private final AvatarRepository avatarRepository;
    private final StudentRepository studentRepository;
    private final String avatarsDir = "avatars";

    @Autowired
    public AvatarService(AvatarRepository avatarRepository, StudentRepository studentRepository) {
        this.avatarRepository = avatarRepository;
        this.studentRepository = studentRepository;
        try {
            Files.createDirectories(Paths.get(avatarsDir));
            logger.info("Avatars directory created/verified at: {}", avatarsDir);
        } catch (IOException e) {
            logger.error("Failed to create avatars directory: {}", e.getMessage(), e);
        }
    }

    public Avatar uploadAvatar(Long studentId, MultipartFile file) throws IOException {
        logger.info("Was invoked method for upload avatar for student id: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    logger.error("Student not found with id: {}", studentId);
                    return new RuntimeException("Студент не найден");
                });

        Path studentDir = Paths.get(avatarsDir, studentId.toString());
        Files.createDirectories(studentDir);
        logger.debug("Created directory for student: {}", studentDir);

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = studentDir.resolve(fileName);
        Files.write(filePath, file.getBytes());
        logger.debug("Avatar saved to: {}", filePath);

        Avatar avatar = avatarRepository.findByStudentId(studentId);
        if (avatar == null) {
            avatar = new Avatar();
            avatar.setStudent(student);
            logger.debug("Creating new avatar entity for student id: {}", studentId);
        } else {
            logger.debug("Updating existing avatar for student id: {}", studentId);
        }

        avatar.setFilePath(filePath.toString());
        avatar.setFileSize(file.getSize());
        avatar.setMediaType(file.getContentType());
        avatar.setData(file.getBytes());

        Avatar saved = avatarRepository.save(avatar);
        logger.info("Avatar uploaded successfully for student id: {}", studentId);
        return saved;
    }

    public Avatar getAvatarByStudentId(Long studentId) {
        logger.info("Was invoked method for get avatar by student id: {}", studentId);
        Avatar avatar = avatarRepository.findByStudentId(studentId);
        if (avatar == null) {
            logger.warn("No avatar found for student id: {}", studentId);
        }
        return avatar;
    }

    public Page<Avatar> getAllAvatars(int page, int size) {
        logger.info("Was invoked method for get all avatars with page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Avatar> avatars = avatarRepository.findAll(pageable);
        logger.debug("Retrieved {} avatars out of {} total", avatars.getNumberOfElements(), avatars.getTotalElements());
        return avatars;
    }
}