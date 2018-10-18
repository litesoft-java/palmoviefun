package org.superbiz.moviefun.blobstore;

import io.minio.ErrorCode;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.errors.ErrorResponseException;
import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class MinioStore implements BlobStore {

    private final Tika tika = new Tika();

    private final MinioClient minioClient;
    private final String bucketName;
    private final String basePath;

    public MinioStore(MinioClient minioClient, String bucketName, String basePath) throws IOException {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
        this.basePath = basePath;
        try {
            // Check if the bucket already exists.
            boolean isExist = minioClient.bucketExists(bucketName);
            if (!isExist) {
                minioClient.makeBucket(bucketName);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void put(String name, InputStream inputStream, String contentType) throws IOException {
        byte[] bytes; // NOTE: this will either blow up on large files OR truncate the size
        try {
            bytes = IOUtils.toByteArray(inputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        try {
            minioClient.putObject(bucketName, basePath + name, stream, bytes.length, contentType);
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    @Override
    public void put(Blob blob) throws IOException {
        try {
            put(blob.name, blob.inputStream, blob.contentType);
        } finally {
            IOUtils.closeQuietly(blob.inputStream);
        }
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        String objectKey = basePath + name;
        String contentType = null;
        InputStream stream = null;
        try {
            ObjectStat objectStat = minioClient.statObject(bucketName, objectKey);
            contentType = objectStat.contentType();
            stream = minioClient.getObject(bucketName, objectKey);
        } catch (ErrorResponseException e) {
            ErrorCode errorCode = e.errorResponse().errorCode();
            if (errorCode == ErrorCode.NO_SUCH_KEY) {
                return Optional.empty();
            }
            throw new IOException(e);
        } catch (Exception e) {
            throw new IOException(e);
        }
        return Optional.of(new Blob(name, stream, contentType));
    }

    @Override
    public void deleteAll() {
        // ...
    }
}