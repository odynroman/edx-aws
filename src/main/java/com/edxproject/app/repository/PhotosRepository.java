package com.edxproject.app.repository;

import java.io.File;
import java.util.List;

public interface PhotosRepository {
    List<String> getPhotosUrls();
    String postPhoto(File file);
}
