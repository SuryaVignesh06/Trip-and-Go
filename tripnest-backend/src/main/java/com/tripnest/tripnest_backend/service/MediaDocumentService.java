package com.tripnest.tripnest_backend.service;

import com.tripnest.tripnest_backend.dto.MediaDocumentResponse;
import com.tripnest.tripnest_backend.entity.MediaDocument;
import com.tripnest.tripnest_backend.entity.Trip;
import com.tripnest.tripnest_backend.entity.User;
import com.tripnest.tripnest_backend.repository.MediaDocumentRepository;
import com.tripnest.tripnest_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaDocumentService {

    private final MediaDocumentRepository mediaDocumentRepository;
    private final UserRepository userRepository;
    private final TripAccessService tripAccessService;

    private final Path uploadDirectory =
            Paths.get("uploads/tripnest");

    private User getCurrentUser() {

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"));
    }

    public MediaDocumentResponse uploadDocument(
            Integer tripId,
            MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File is required");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "File size must not exceed 10 MB");
        }

        User currentUser = getCurrentUser();

        tripAccessService.checkAccess(
                tripId,
                currentUser);

        Trip trip =
                tripAccessService.getTrip(tripId);

        try {

            Files.createDirectories(uploadDirectory);

            String originalFileName =
                    file.getOriginalFilename();

            if (originalFileName == null
                    || originalFileName.isBlank()) {

                throw new IllegalArgumentException(
                        "Invalid file name");
            }

            String safeFileName =
                    Paths.get(originalFileName)
                            .getFileName()
                            .toString();

            String storedFileName =
                    System.currentTimeMillis()
                            + "_"
                            + safeFileName;

            Path destination =
                    uploadDirectory.resolve(
                            storedFileName);

            Files.copy(
                    file.getInputStream(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING);

            MediaDocument document =
                    new MediaDocument();

            document.setTrip(trip);
            document.setUploadedBy(currentUser);
            document.setFileName(safeFileName);
            document.setFileType(
                    file.getContentType() != null
                            ? file.getContentType()
                            : "application/octet-stream");
            document.setFilePath(
                    destination.toString());
            document.setFileSize(
                    file.getSize());

            MediaDocument saved =
                    mediaDocumentRepository.save(
                            document);

            return toResponse(saved);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to upload file",
                    e);
        }
    }

    public List<MediaDocumentResponse> getTripDocuments(
            Integer tripId) {

        User currentUser = getCurrentUser();

        tripAccessService.checkAccess(
                tripId,
                currentUser);

        return mediaDocumentRepository
                .findByTripIdOrderByUploadedAtDesc(tripId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteDocument(
            Integer documentId) {

        User currentUser = getCurrentUser();

        MediaDocument document =
                mediaDocumentRepository
                        .findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document not found"));

        Integer tripId =
                document.getTrip()
                        .getId();

        tripAccessService.checkAccess(
                tripId,
                currentUser);

        boolean isOwnerOrAdmin =
                tripAccessService.isOwnerOrGroupAdmin(
                        tripId,
                        currentUser);

        boolean isUploader =
                document.getUploadedBy()
                        .getId()
                        .equals(currentUser.getId());

        if (!isOwnerOrAdmin && !isUploader) {

            throw new RuntimeException(
                    "You are not authorized to delete this document");
        }

        try {

            Files.deleteIfExists(
                    Paths.get(document.getFilePath()));

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to delete file",
                    e);
        }

        mediaDocumentRepository.delete(document);
    }

    private MediaDocumentResponse toResponse(
            MediaDocument document) {

        return new MediaDocumentResponse(
                document.getId(),
                document.getTrip().getId(),
                document.getFileName(),
                document.getFileType(),
                document.getFileSize(),
                document.getUploadedBy().getId(),
                document.getUploadedBy().getName(),
                document.getUploadedAt());
    }
}