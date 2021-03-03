package com.example.aws.s3;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class GalleryController {
    private S3Service s3Service;
    private GalleryService galleryService;

    @GetMapping("/gallery")
    public String dispWrite() {
        return "/gallery";
    }

    /**
     * form으로부터 넘어온 파일 객체를 받기 위해, MultipartFile 타입의 파라미터를 작성해줍니다.
     * S3에 파일 업로드를 할 때 IOException이 발생할 수 있으므로 예외를 던집니다.
     */
    @PostMapping("/gallery")
    public String execWrite(GalleryDto galleryDto, MultipartFile file) throws IOException {
        String imgPath = s3Service.upload(file); // s3Service는 AWS S3의 비즈니스 로직을 담당하며, 파일을 조작합니다.
        galleryDto.setFilePath(imgPath);
        galleryService.savePost(galleryDto); // galleryService는 DB에 데이터를 조작하기 위한 서비스입니다.
        return "redirect:/gallery";
    }
}