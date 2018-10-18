package org.superbiz.moviefun.albums;

import org.apache.tika.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.IOException;
import java.util.Map;

import static java.lang.String.format;

@SuppressWarnings("unused")
@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    private final BlobStore blobStore;

    public AlbumsController(AlbumsBean albumsBean, BlobStore blobStore) {
        this.albumsBean = albumsBean;
        this.blobStore = blobStore;
    }

    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        saveUploadToFile(uploadedFile, getCoverFilePath(albumId));

        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException {
        String coverFilePath = getCoverFilePath(albumId);
        Blob blob = blobStore.get(coverFilePath).orElseGet(this::createDefault);

        byte[] imageBytes = IOUtils.toByteArray(blob.inputStream);
        HttpHeaders headers = createImageHttpHeaders(blob.contentType, imageBytes);

        return new HttpEntity<>(imageBytes, headers);
    }

    private Blob createDefault() {
        String name = "/default-cover.jpg";
        return new Blob(name, AlbumsController.class.getResourceAsStream(name), "image/jpeg");
    }

    private HttpHeaders createImageHttpHeaders(String contentType, byte[] imageBytes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }

    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, String name) throws IOException {
        Blob blob = new Blob(name, uploadedFile.getInputStream(), uploadedFile.getContentType());
        blobStore.put(blob);
    }

    private String getCoverFilePath(long albumId) {
        return format("covers/%d", albumId);
    }
}
