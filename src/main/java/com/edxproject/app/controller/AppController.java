package com.edxproject.app.controller;

import com.edxproject.app.service.PhotosService;
import org.reactivestreams.Publisher;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class AppController {
    private static final String INDEX_HTML = "index.html";
    private static final String PHOTOS = "photos";
    private static final String URL = "url";
    private static final String UPLOADED = "uploaded";
    private final PhotosService photosService;

    public AppController(PhotosService photosService) {
        this.photosService = photosService;
    }

    @GetMapping("/")
    public String getIndexPage(Model model) {
        fetchPhotosToModel(model);
        return INDEX_HTML;
    }

    @PostMapping(value = "/test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String postPhotos(@RequestParam("file") MultipartFile file, Model model) {
        try {
            File photoFile = file.getResource().getFile();
            Mono postedPhotoUrl = photosService.postPhoto(photoFile);

            model.addAttribute(URL, postedPhotoUrl);
            fetchPhotosToModel(model);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return INDEX_HTML;
    }

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String postPhotos(@RequestBody Flux<Part> parts, Model model) {
        Flux<String> uploadResult = parts
                .filter(part -> part instanceof FilePart)
                .cast(FilePart.class)
                .flatMap(this::postFile)
                .log();

        //FIXME:add possibility to disp;ay uploaded pictures
        model.addAttribute(PHOTOS, new ReactiveDataDriverContextVariable(uploadResult));
        model.addAttribute(UPLOADED, true);

        return INDEX_HTML;
    }

    private Publisher<String> postFile(FilePart filePart) {
        Path target = Paths.get("").resolve(filePart.filename());
        try {
            Files.deleteIfExists(target);
            File file = Files.createFile(target).toFile();

            return filePart.transferTo(file)
                    .switchIfEmpty(photosService.postPhoto(file).flatMap(x -> Mono.empty()))
                    .flatMap(f -> photosService.postPhoto(file));
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
