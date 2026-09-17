package com.tripnest.tripnest_backend.controller;

import com.tripnest.tripnest_backend.dto.MediaDocumentResponse;
import com.tripnest.tripnest_backend.service.MediaDocumentService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaDocumentController {

    private final MediaDocumentService mediaDocumentService;

    @PostMapping(
            value = "/trip/{tripId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MediaDocumentResponse uploadDocument(
            @PathVariable Integer tripId,
            @RequestParam("file") MultipartFile file) {

        return mediaDocumentService.uploadDocument(
                tripId,
                file);
    }

    @GetMapping("/trip/{tripId}")
    public List<MediaDocumentResponse> getTripDocuments(
            @PathVariable Integer tripId) {

        return mediaDocumentService
                .getTripDocuments(tripId);
    }

    @DeleteMapping("/{documentId}")
    public void deleteDocument(
            @PathVariable Integer documentId) {

        mediaDocumentService
                .deleteDocument(documentId);
    }
}