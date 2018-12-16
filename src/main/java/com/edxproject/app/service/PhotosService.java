package com.edxproject.app.service;

import reactor.core.publisher.Mono;

import java.io.File;
import java.util.List;

public interface PhotosService {
    Mono<List<String>> getPhotosUrls();
    Mono postPhoto(File file);
}
