package com.edxproject.app.controller;

import com.edxproject.app.service.PhotosService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;

@Controller
public class AppController {
    private final PhotosService photosService;

    public AppController(PhotosService photosService) {
        this.photosService = photosService;
    }

    @GetMapping("/")
    public Mono<String> getIndexPage() {
        return Mono.fromCallable(() -> "index.html");
    }

    @PostMapping("/")
    public Mono<String> postPhotos(@RequestParam("file") MultipartFile file) {
        try {
            File photo = file.getResource().getFile();
            return photosService.postPhoto(photo)
                    .map(x -> "index.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Mono.fromCallable(() -> "index.html");
    }
}
