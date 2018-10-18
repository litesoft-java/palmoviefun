package org.superbiz.moviefun.blobstore;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;

import java.io.*;
import java.util.Optional;

public class FileStore implements BlobStore {

    private final Tika tika = new Tika();

    private final File baseDirectory;

    public FileStore(File baseDirectory) throws IOException {
        if (!baseDirectory.isDirectory()) {
            throw new IllegalArgumentException("Not a Directory: " + baseDirectory);
        }
        baseDirectory = baseDirectory.getCanonicalFile();
        if (File.separator.equals(baseDirectory.getPath())) {
            throw new IllegalArgumentException("Nix root not supported");
        }
        this.baseDirectory = baseDirectory;
    }

    public FileStore(String baseDirectory) throws IOException {
        this(new File(baseDirectory));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void put(String name, InputStream inputStream, String contentType) throws IOException {
        try {
            File targetFile = new File(baseDirectory, name);
            targetFile.delete();
            targetFile.getParentFile().mkdirs();

            try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                IOUtils.copy(inputStream, outputStream);
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Override
    public void put(Blob blob) throws IOException {
        put(blob.name, blob.inputStream, blob.contentType);
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        File file = new File(baseDirectory, name);
        if (!file.exists()) {
            return Optional.empty();
        }

        return Optional.of(new Blob(name, new FileInputStream(file), tika.detect(file)));
    }

    @Override
    public void deleteAll() {
        // ...
    }
}