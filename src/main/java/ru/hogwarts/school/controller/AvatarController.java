package ru.hogwarts.school.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.service.AvatarService;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/avatar")
public class AvatarController {

    private final AvatarService avatarService;

    @Autowired
    public AvatarController(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    // Загрузка аватарки
    @PostMapping(value = "/{studentId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Avatar> uploadAvatar(
            @PathVariable Long studentId,
            @RequestParam MultipartFile avatar) throws IOException {
        Avatar uploadedAvatar = avatarService.uploadAvatar(studentId, avatar);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedAvatar);
    }

    // Получить аватарку (как данные)
    @GetMapping("/{studentId}/data")
    public ResponseEntity<byte[]> getAvatarData(@PathVariable Long studentId) {
        Avatar avatar = avatarService.getAvatarByStudentId(studentId);
        if (avatar == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(avatar.getMediaType()))
                .body(avatar.getData());
    }

    // Получить аватарку (из файла)
    @GetMapping("/{studentId}/file")
    public void getAvatarFile(@PathVariable Long studentId, HttpServletResponse response) throws IOException {
        Avatar avatar = avatarService.getAvatarByStudentId(studentId);
        if (avatar == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Path path = Path.of(avatar.getFilePath());
        response.setContentType(avatar.getMediaType());
        response.setContentLengthLong(avatar.getFileSize());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName() + "\"");

        Files.copy(path, response.getOutputStream());
    }

    // Получить все аватарки с пагинацией
    @GetMapping("/all")
    public ResponseEntity<Page<Avatar>> getAllAvatars(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Avatar> avatars = avatarService.getAllAvatars(page, size);
        return ResponseEntity.ok(avatars);
    }
}