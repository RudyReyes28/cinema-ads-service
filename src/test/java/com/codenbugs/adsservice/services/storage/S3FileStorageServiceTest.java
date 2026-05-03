package com.codenbugs.adsservice.services.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.codenbugs.adsservice.exceptions.FileStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3FileStorageServiceTest {

    private static final String BUCKET = "test-bucket";
    private static final String REGION = "us-east-1";

    @Mock
    private AmazonS3 amazonS3;

    private S3FileStorageService service;

    @BeforeEach
    void setUp() {
        service = new S3FileStorageService(amazonS3, BUCKET, REGION);
    }

    // -------------------- uploadFile --------------------

    @Test
    void uploadFile_success() throws FileStorageException {
        MockMultipartFile file = validPdf("document.pdf");

        String url = service.uploadFile(file, "submissions");

        assertThat(url).startsWith("https://test-bucket.s3.us-east-1.amazonaws.com/submissions/");
        verify(amazonS3).putObject(any(PutObjectRequest.class));
    }

    @Test
    void uploadFile_sanitizesFilename() throws FileStorageException {
        MockMultipartFile file = validPdf("my file (v2).pdf");

        String url = service.uploadFile(file, "submissions");

        assertThat(url).doesNotContain(" ").doesNotContain("(").doesNotContain(")");
    }

    @Test
    void uploadFile_emptyFile_throws() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> service.uploadFile(file, "submissions"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("null or empty");
    }

    /*@Test
    void uploadFile_fileTooLarge_throws() {
        byte[] largeContent = new byte[11 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile("file", "big.mp4", "video/mp4", largeContent);

        assertThatThrownBy(() -> service.uploadFile(file, "submissions"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("100 MB");
    }*/

    @Test
    void uploadFile_invalidContentType_throws() {
        MockMultipartFile file = new MockMultipartFile("file", "image.py", "python/py", "data".getBytes());

        assertThatThrownBy(() -> service.uploadFile(file, "submissions"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Invalid file type");
    }

    @Test
    void uploadFile_docxAllowed() throws FileStorageException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "paper.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "content".getBytes()
        );

        String url = service.uploadFile(file, "submissions");

        assertThat(url).isNotBlank();
    }

    @Test
    void uploadFile_s3Failure_throws() {
        MockMultipartFile file = validPdf("doc.pdf");
        doThrow(new RuntimeException("S3 unavailable")).when(amazonS3).putObject(any(PutObjectRequest.class));

        assertThatThrownBy(() -> service.uploadFile(file, "submissions"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Failed to upload file to S3");
    }


    @Test
    void deleteFile_success() throws FileStorageException {
        String url = "https://test-bucket.s3.us-east-1.amazonaws.com/submissions/uuid-doc.pdf";

        service.deleteFile(url);

        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(amazonS3).deleteObject(captor.capture());
        assertThat(captor.getValue().getKey()).isEqualTo("submissions/uuid-doc.pdf");
    }

    @Test
    @DisplayName("deleteFile: should do nothing when URL is null")
    void deleteFile_nullUrl_doesNothing() throws FileStorageException {
        service.deleteFile(null);
        verifyNoInteractions(amazonS3);
    }

    @Test
    @DisplayName("deleteFile: should do nothing when URL is blank")
    void deleteFile_blankUrl_doesNothing() throws FileStorageException {
        service.deleteFile("   ");
        verifyNoInteractions(amazonS3);
    }

    @Test
    @DisplayName("deleteFile: should throw when URL does not match bucket/region")
    void deleteFile_invalidUrl_throws() {
        String wrongUrl = "https://other-bucket.s3.eu-west-1.amazonaws.com/file.pdf";

        assertThatThrownBy(() -> service.deleteFile(wrongUrl))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Cannot extract S3 key");
    }

    @Test
    @DisplayName("deleteFile: should throw FileStorageException when S3 fails")
    void deleteFile_s3Failure_throws() {
        String url = "https://test-bucket.s3.us-east-1.amazonaws.com/submissions/file.pdf";
        doThrow(new RuntimeException("S3 error")).when(amazonS3).deleteObject(any(DeleteObjectRequest.class));

        assertThatThrownBy(() -> service.deleteFile(url))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Failed to delete file from S3");
    }


    private MockMultipartFile validPdf(String filename) {
        return new MockMultipartFile("file", filename, "application/pdf", "pdf-content".getBytes());
    }

    // -------------------- Validación de tamaño de imagen > 5MB --------------------

    @Test
    void uploadFile_imageTooLarge_throws() {
        byte[] largeContent = new byte[(int) (5 * 1024 * 1024) + 1];
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", largeContent
        );

        assertThatThrownBy(() -> service.uploadFile(file, "ads/images"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("5 MB");
    }

// -------------------- Validacion de tamanio de video > 100MB --------------------

    @Test
    void uploadFile_videoTooLarge_throws() {
        byte[] largeContent = new byte[(int) (100 * 1024 * 1024) + 1];
        MockMultipartFile file = new MockMultipartFile(
                "file", "video.mp4", "video/mp4", largeContent
        );

        assertThatThrownBy(() -> service.uploadFile(file, "ads/videos"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("100 MB");
    }

// -------------------- Imagen dentro del limite de 5MB pasa la validacion --------------------

    @Test
    void uploadFile_imageWithinSizeLimit_succeeds() throws FileStorageException {
        byte[] content = new byte[(int) (5 * 1024 * 1024) - 1];
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", content
        );

        String url = service.uploadFile(file, "ads/images");

        assertThat(url).startsWith("https://test-bucket.s3.us-east-1.amazonaws.com/ads/images/");
        verify(amazonS3).putObject(any(PutObjectRequest.class));
    }

// -------------------- Video dentro del limite de 100MB pasa la validacion --------------------

    @Test
    void uploadFile_videoWithinSizeLimit_succeeds() throws FileStorageException {
        byte[] content = new byte[1024];
        MockMultipartFile file = new MockMultipartFile(
                "file", "video.mp4", "video/mp4", content
        );

        String url = service.uploadFile(file, "ads/videos");

        assertThat(url).startsWith("https://test-bucket.s3.us-east-1.amazonaws.com/ads/videos/");
        verify(amazonS3).putObject(any(PutObjectRequest.class));
    }

// -------------------- IOException al leer el InputStream --------------------

    @Test
    void uploadFile_ioExceptionOnRead_throws() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        when(file.getInputStream()).thenThrow(new IOException("disk error"));

        assertThatThrownBy(() -> service.uploadFile(file, "ads/images"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Failed to read file input stream");
    }

// -------------------- Tipos de imagen permitidos --------------------

    @Test
    void uploadFile_jpegAllowed() throws FileStorageException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "content".getBytes()
        );

        String url = service.uploadFile(file, "ads/images");

        assertThat(url).isNotBlank();
    }

    @Test
    void uploadFile_pngAllowed() throws FileStorageException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", "content".getBytes()
        );

        String url = service.uploadFile(file, "ads/images");

        assertThat(url).isNotBlank();
    }

// -------------------- Tipos de video permitidos --------------------

    @Test
    void uploadFile_mp4Allowed() throws FileStorageException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "video.mp4", "video/mp4", "content".getBytes()
        );

        String url = service.uploadFile(file, "ads/videos");

        assertThat(url).isNotBlank();
    }

// -------------------- Filename null se sanitiza a "file" --------------------

    @Test
    void uploadFile_nullFilename_sanitizesToFile() throws FileStorageException {
        MockMultipartFile file = new MockMultipartFile(
                "file", null, "image/jpeg", "content".getBytes()
        );

        String url = service.uploadFile(file, "ads/images");

        assertThat(url).contains("ads/images/");
    }
}