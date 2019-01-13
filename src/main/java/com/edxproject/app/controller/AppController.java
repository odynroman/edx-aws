package com.edxproject.app.controller;

import com.edxproject.app.service.PhotosService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
public class AppController {
    private static final String INDEX_HTML = "index.html";
    private static final String PHOTOS = "photos";
    private static final String UPLOADED = "uploaded";
    private static final String LABELS = "all_labels";
    private final PhotosService photosService;
    private final String photosFolder;

    public AppController(PhotosService photosService, @Value("${uploaded-photos-folder}") String photosFolder) {
        this.photosService = photosService;
        this.photosFolder = photosFolder;
    }

    @GetMapping("/")
    public String getIndexPage(Model model) {
        fetchPhotosToModel(model);
        return INDEX_HTML;
    }

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String postPhotos(@RequestBody Flux<Part> parts, Model model) {
        Flux<String> uploadResult = parts
                .filter(part -> part instanceof FilePart)
                .cast(FilePart.class)
                .flatMap(this::postFile)
                .log();

        model.addAttribute(LABELS, new ReactiveDataDriverContextVariable(uploadResult));
        model.addAttribute(UPLOADED, true);

        return INDEX_HTML;
    }

    private Mono<String> postFile(FilePart filePart) {
        UUID uuid = UUID.randomUUID();
        Path target = Paths.get("").resolve(photosFolder + uuid.toString() + ".png");
        try {
            Files.deleteIfExists(target);
            File file = Files.createFile(target).toFile();

            return filePart.transferTo(file)
                    .then(photosService.postPhoto(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Mono.empty();
    }

    private void fetchPhotosToModel(Model model) {
        Flux photos = photosService.getPhotosUrls()
                .log()
                .flatMapMany(Flux::fromIterable);
        model.addAttribute(PHOTOS, new ReactiveDataDriverContextVariable(photos));
    }
}
