package org.superbiz.moviefun;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.superbiz.moviefun.blobstore.BlobStore;
import org.superbiz.moviefun.blobstore.FileStore;
import org.superbiz.moviefun.blobstore.MinioStore;

import java.io.IOException;

@SpringBootApplication
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

//    @Bean
//    public BlobStore fileStore(@Value("${FILE_STORE_BASE_DIRECTORY}") String fileStoreBaseDirectory) throws IOException {
//        return new FileStore(fileStoreBaseDirectory);
//    }

    @Bean
    public BlobStore minioStore(@Value("${MINIO_BASE_DIRECTORY}") String minioBaseDirectory,
                                @Value("${MINIO_BUCKET_NAME}") String minioBucketName,
                                @Value("${MINIO_URL}") String minioUrl,
                                @Value("${MINIO_ACCESS}") String minioAccess,
                                @Value("${MINIO_SECRET}") String minioSecret) throws Exception {
        return new MinioStore(new MinioClient(minioUrl, minioAccess, minioSecret), minioBucketName, minioBaseDirectory);
    }


}
