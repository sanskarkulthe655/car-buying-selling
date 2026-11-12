package com.autoadmin.vehicle.photo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

class VehiclePhotoServiceTest {
    private VehiclePhotoRepository repository;
    private VehiclePhotoService service;

    @BeforeEach
    void setUp() {
        repository = mock(VehiclePhotoRepository.class);
        service = new VehiclePhotoService(repository);
    }

    @Test
    void rejectsMoreThanFivePhotosBeforeWritingToTheDatabase() {
        List<MockMultipartFile> sixFiles = List.of(
            image("1.jpg", new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff}),
            image("2.jpg", new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff}),
            image("3.jpg", new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff}),
            image("4.jpg", new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff}),
            image("5.jpg", new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff}),
            image("6.jpg", new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff})
        );
        assertThrows(ResponseStatusException.class, () -> service.savePhotos(1L, sixFiles));
        verify(repository, never()).saveAll(any());
    }

    @Test
    void rejectsFilesThatAreNotSupportedImageTypes() {
        MockMultipartFile text = new MockMultipartFile("photos", "photo.jpg", "image/jpeg", "not an image".getBytes());
        assertThrows(ResponseStatusException.class, () -> service.savePhotos(1L, List.of(text)));
        verify(repository, never()).saveAll(any());
    }

    @Test
    void rejectsImagesLargerThanTwoMegabytes() {
        MockMultipartFile tooLarge = image("large.png", new byte[(int) VehiclePhotoService.MAX_FILE_BYTES + 1]);
        assertThrows(ResponseStatusException.class, () -> service.savePhotos(1L, List.of(tooLarge)));
        verify(repository, never()).saveAll(any());
    }

    @Test
    void acceptsSupportedPngSignatureAndUsesDetectedMimeType() {
        byte[] png = new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0d, 0x0a, 0x1a, 0x0a, 1};
        MockMultipartFile upload = new MockMultipartFile("photos", "front.png", "application/octet-stream", png);
        when(repository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<VehiclePhoto> records = invocation.getArgument(0);
            List<VehiclePhoto> saved = new ArrayList<>();
            records.forEach(saved::add);
            return saved;
        });
        List<VehiclePhotoMetadata> metadata = service.savePhotos(7L, List.of(upload));
        verify(repository).saveAll(any());
        assertEquals("image/png", metadata.get(0).contentType());
    }

    private static MockMultipartFile image(String name, byte[] bytes) {
        return new MockMultipartFile("photos", name, "image/jpeg", bytes);
    }
}
