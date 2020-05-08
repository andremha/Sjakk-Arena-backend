package no.ntnu.sjakkarena.controllers;

import no.ntnu.sjakkarena.data.Image;
import no.ntnu.sjakkarena.services.FileStorageService;
import no.ntnu.sjakkarena.utils.RESTSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

// code adapted from: https://www.devglan.com/spring-boot/spring-boot-file-upload-download and https://www.callicoder.com/spring-boot-file-upload-download-jpa-hibernate-mysql-database-example/

@RestController
public class FileController {

    @Autowired
    FileStorageService storageService;

    public String basePath = "C:\\Sjakk-Arena\\endPositions\\";

    @RequestMapping(value = "/playerFile/Upload", method = RequestMethod.POST)
    public ResponseEntity uploadFile(@RequestParam("Image")MultipartFile file) throws IOException {
        try {
            int playerId = RESTSession.getUserId();
            storageService.uploadFile(file, playerId);
            return new ResponseEntity(HttpStatus.OK);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("image: " + file + " was not found." + e);
        } catch (MultipartException e) {
            throw new MultipartException(e + " Not a multipartfile.");
        }
    }

    @RequestMapping(value = "/playerFile/Download/{gameId}", method = RequestMethod.GET)
    public ResponseEntity downloadFiles(@PathVariable ("gameId") int gameId) {
        List<Image> images = storageService.fetchGameImages(gameId);
        Path path = Paths.get(basePath + images);
        Resource resource = null;
        for (int i = 0; i < images.size(); i++) {
            try {
                resource = new UrlResource(path.toUri());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("image/jpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
