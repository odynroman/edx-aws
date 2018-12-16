package com.edxproject.app.repository.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.edxproject.app.repository.PhotosRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class S3PhotosRepository implements PhotosRepository {
    private static final String PREFIX = "";
    private static final String FILE_OBJ_KEY_NAME = "";
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
                .map(S3ObjectSummary::getKey)
                .map(pictKey -> {
                    GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(photosBucketName, pictKey);
                    return s3.generatePresignedUrl(request).toString();
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean postPhoto(File file) {

        try {
            // Upload a file as a new object with ContentType and title specified.
            PutObjectRequest request = new PutObjectRequest(photosBucketName, FILE_OBJ_KEY_NAME, file);
            s3.putObject(request);
        } catch(AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();

            return false;
        } catch(SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();

            return false;
        }

        return true;
    }

}
