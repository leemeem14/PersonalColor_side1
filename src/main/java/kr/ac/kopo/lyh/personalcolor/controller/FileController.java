package kr.ac.kopo.lyh.personalcolor.controller;

import org.springframework.core.io.Resource;
import kr.ac.kopo.lyh.personalcolor.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileStorageService fileStorageService;

    @GetMapping("/files/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Resource resource = fileStorageService.loadFile(fileName);

            // 파일의 Content-Type 결정
            String contentType = "application/octet-stream";
            try {
                contentType = Files.probeContentType(Paths.get(resource.getFile().getAbsolutePath()));
            } catch (IOException e) {
                log.debug("파일 타입을 결정할 수 없습니다: {}", fileName);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("파일 다운로드 오류: {}", fileName, e);
            return ResponseEntity.notFound().build();
        }
    }
}
