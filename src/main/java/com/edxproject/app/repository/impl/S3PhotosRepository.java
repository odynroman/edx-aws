package com.edxproject.app.repository.impl;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.edxproject.app.repository.PhotosRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class S3PhotosRepository implements PhotosRepository {
    private static final String PREFIX = "";
    private final AmazonS3 s3;
    private final String photosBucketName;

    public S3PhotosRepository(AmazonS3 s3, @Value("${aws-photos-bucket}") String photosBucketName) {
        this.s3 = s3;
        this.photosBucketName = photosBucketName;
    }

    public List<String> getPhotosUrls() {
        ObjectListing photosListing = s3.listObjects(photosBucketName, PREFIX);
        List<S3ObjectSummary> summaries = photosListing.getObjectSummaries();

        while (photosListing.isTruncated()) {
            photosListing = s3.listNextBatchOfObjects (photosListing);
            summaries.addAll (photosListing.getObjectSummaries());
        }

        return summaries.stream()
                .sorted(Comparator.comparing(S3ObjectSummary::getLastModified))
                .map(S3ObjectSummary::getKey)
                .map(this::getS3ObjectUrl)
                .collect(Collectors.toList());
    }

    private String getS3ObjectUrl(String objectKey) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(photosBucketName, objectKey);
        return s3.generatePresignedUrl(request).toString();
    }

    @Override
    public String postPhoto(File file) {

        try {
            // Upload a file as a new object with ContentType and title specified.
            PutObjectRequest request = new PutObjectRequest(photosBucketName, file.getName(), file);
            s3.putObject(request).getMetadata();
            return file.getName();

        } catch(SdkClientException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            // or
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }

        return "";
    }

}
