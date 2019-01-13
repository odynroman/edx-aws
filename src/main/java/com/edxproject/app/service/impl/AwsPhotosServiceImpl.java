package com.edxproject.app.service.impl;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;
import com.edxproject.app.repository.PhotosRepository;
import com.edxproject.app.service.PhotosService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AwsPhotosServiceImpl implements PhotosService {
    private static final String LABELS_DELIMITER = ", ";
    private final Integer maxRecognisedLabels;
    private final Float minConfidence;
    private final String photosBucketName;
    private final PhotosRepository photosRepository;
    private final AmazonRekognition rekognitionClient;

    public AwsPhotosServiceImpl(PhotosRepository photosRepository,
                                AmazonRekognition rekognitionClient,
                                @Value("${aws-photos-bucket}") String photosBucketName,
                                @Value("${max-recognized-labels}") Integer maxRecognisedLabels,
                                @Value("${min-confidence}") Float minConfidence) {
        this.maxRecognisedLabels = maxRecognisedLabels;
        this.minConfidence = minConfidence;
        this.photosRepository = photosRepository;
        this.rekognitionClient = rekognitionClient;
        this.photosBucketName = photosBucketName;
    }

    @Override
    public Mono<List<String>> getPhotosUrls() {
        return Mono.<List<String>>fromCallable(photosRepository::getPhotosUrls);
    }

    @Override
    public Mono<String> postPhoto(File file) {
        return Mono.fromCallable(() -> photosRepository.postPhoto(file))
                .map(this::detectLabels)
                .log();
    }

    private String detectLabels(String photoName) {
        S3Object picture = new S3Object()
                .withBucket(photosBucketName)
                .withName(photoName);

        DetectLabelsRequest labelsRequest = new DetectLabelsRequest()
                .withImage(new Image().withS3Object(picture))
                .withMaxLabels(maxRecognisedLabels)
                .withMinConfidence(minConfidence);

        return rekognitionClient.detectLabels(labelsRequest)
                .getLabels()
                .stream()
                .map(label -> label.getName() + "(" + label.getConfidence() + ")")
                .collect(Collectors.joining(LABELS_DELIMITER));
    }
}
