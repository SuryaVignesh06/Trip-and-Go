package com.tripnest.tripnest_backend.dto;

import java.time.LocalDateTime;

public record MediaDocumentResponse(
        Integer id,
        Integer tripId,
        String fileName,
        String fileType,
        Long fileSize,
        Integer uploadedBy,
        String uploadedByName,
        LocalDateTime uploadedAt
) {
}