package com.tripnest.tripnest_backend.repository;

import com.tripnest.tripnest_backend.entity.MediaDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaDocumentRepository
        extends JpaRepository<MediaDocument, Integer> {

    List<MediaDocument> findByTripIdOrderByUploadedAtDesc(
            Integer tripId);
}