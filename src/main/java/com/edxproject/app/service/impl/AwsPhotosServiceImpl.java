package com.edxproject.app.service.impl;

import com.edxproject.app.repository.PhotosRepository;
import com.edxproject.app.service.PhotosService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.List;

@Service
public class AwsPhotosServiceImpl implements PhotosService {
    private final PhotosRepository photosRepository;

    public AwsPhotosServiceImpl(PhotosRepository photosRepository) {
        this.photosRepository = photosRepository;
    }

    @Override
    public Mono<List<String>> getPhotosUrls() {
        return Mono.<List<String>>fromCallable(photosRepository::getPhotosUrls);
    }

    @Override
    public Mono postPhoto(File file) {
        return Mono.fromCallable(() -> photosRepository.postPhoto(file));
    }
}
