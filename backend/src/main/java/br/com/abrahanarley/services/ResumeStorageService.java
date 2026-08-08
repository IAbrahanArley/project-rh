package br.com.abrahanarley.services;

import br.com.abrahanarley.exceptions.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;

@Service
public class ResumeStorageService {

	private final S3Client s3Client;
	private final S3Presigner s3Presigner;
	private final String bucket;
	private final long uploadExpirationSeconds;
	private final long downloadExpirationSeconds;

	public ResumeStorageService(
			S3Client s3Client,
			S3Presigner s3Presigner,
			@Value("${app.storage.resumes.bucket}") String bucket,
			@Value("${app.storage.resumes.upload-url-expiration-seconds}") long uploadExpirationSeconds,
			@Value("${app.storage.resumes.download-url-expiration-seconds}") long downloadExpirationSeconds) {
		this.s3Client = s3Client;
		this.s3Presigner = s3Presigner;
		this.bucket = bucket;
		this.uploadExpirationSeconds = uploadExpirationSeconds;
		this.downloadExpirationSeconds = downloadExpirationSeconds;
	}

	public URL createUploadUrl(String storageKey, String contentType) {
		assertBucketConfigured();
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucket)
				.key(storageKey)
				.contentType(contentType)
				.build();
		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
				.signatureDuration(Duration.ofSeconds(uploadExpirationSeconds))
				.putObjectRequest(putObjectRequest)
				.build();
		PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
		return presignedRequest.url();
	}

	public URL createDownloadUrl(String storageKey, String fileName) {
		assertBucketConfigured();
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				.bucket(bucket)
				.key(storageKey)
				.responseContentType("application/pdf")
				.responseContentDisposition("inline; filename=\"" + sanitizeHeaderFileName(fileName) + "\"")
				.build();
		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
				.signatureDuration(Duration.ofSeconds(downloadExpirationSeconds))
				.getObjectRequest(getObjectRequest)
				.build();
		PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
		return presignedRequest.url();
	}

	public HeadObjectResponse headObject(String storageKey) {
		assertBucketConfigured();
		return s3Client.headObject(HeadObjectRequest.builder()
				.bucket(bucket)
				.key(storageKey)
				.build());
	}

	public void deleteObject(String storageKey) {
		assertBucketConfigured();
		s3Client.deleteObject(DeleteObjectRequest.builder()
				.bucket(bucket)
				.key(storageKey)
				.build());
	}

	public long getUploadExpirationSeconds() {
		return uploadExpirationSeconds;
	}

	public long getDownloadExpirationSeconds() {
		return downloadExpirationSeconds;
	}

	private void assertBucketConfigured() {
		if (bucket == null || bucket.isBlank()) {
			throw new BusinessException("Resume bucket is not configured.");
		}
	}

	private String sanitizeHeaderFileName(String fileName) {
		return fileName.replaceAll("[\\r\\n\"]", "_");
	}
}
