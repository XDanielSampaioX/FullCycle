package com.biblioteca.Biblioteca.Online.biblioteca.dto;

import java.util.List;

public record GoogleBooksResponse(
        Integer totalItems,
        List<Item> items
) {

    public record Item(
            String id,
            VolumeInfo volumeInfo,
            AccessInfo accessInfo,
            SaleInfo saleInfo
    ) {
    }

    public record VolumeInfo(
            String title,
            String subtitle,
            List<String> authors,
            String publisher,
            String publishedDate,
            String description,
            List<IndustryIdentifier> industryIdentifiers,
            Integer pageCount,
            List<String> categories,
            String language,
            ImageLinks imageLinks,
            String previewLink,
            String infoLink
    ) {
    }

    public record IndustryIdentifier(
            String type,
            String identifier
    ) {
    }

    public record ImageLinks(
            String smallThumbnail,
            String thumbnail
    ) {
    }

    public record AccessInfo(
            Boolean publicDomain,
            DownloadAccess epub,
            DownloadAccess pdf,
            String webReaderLink
    ) {
    }

    public record SaleInfo(
            String country,
            String saleability,
            Boolean isEbook
    ) {
    }

    public record DownloadAccess(
            Boolean isAvailable,
            String downloadLink
    ) {
    }
}
