package org.superbiz.moviefun.blobstore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public interface BlobStore {

    void put(String name, InputStream inputStream, String contentType) throws IOException;

    void put(Blob blob) throws IOException;

    Optional<Blob> get(String name) throws IOException;

    void deleteAll();
}
