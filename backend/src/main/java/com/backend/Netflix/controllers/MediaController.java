package com.backend.Netflix.controllers;

import com.backend.Netflix.NetflixApplication;
import com.backend.Netflix.configs.FirebaseConfig;
import com.backend.Netflix.model.MediaRequestDTO;
import com.backend.Netflix.model.MediaResponseDTO;
import com.backend.Netflix.services.CassandraMediaService;
import com.backend.Netflix.services.GcpMediaDeleteService;
import com.backend.Netflix.services.GcpMediaUploadService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for handling media-related operations in the Netflix backend.
 */
@RestController
@RequestMapping("/media")
public class MediaController {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Autowired
    private CassandraMediaService cassandraService;


    @Autowired
    private GcpMediaUploadService gcpService;


    @Autowired
    private GcpMediaDeleteService gcpDelete;


    @GetMapping("/testendpoint")
    public ResponseEntity<String> testendpoint() {
        logger.info("Test endpoint hit");
        return ResponseEntity.ok("Server is running");
    }

    @PostMapping("/announceAll")
    public ResponseEntity<String> announceAll() {
        NetflixApplication.torrentManager.reannounceAllTorrents();
        return ResponseEntity.ok("All Torrents announced");
    }

    @PostMapping("/runAllTorrent")
    public ResponseEntity<String> runAllTorrent(){
        List<String> Titles = cassandraService.getTitles();
        for (String title : Titles) {
            String convertedTitle = title.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
            runTorrent(convertedTitle);
        }
        return ResponseEntity.ok("All Torrents are running");
    }


    @PostMapping("/runTorrent/{title}")
    public ResponseEntity<String> runTorrent(@PathVariable String title) {
        Path dirpath = Paths.get("/mnt/bucket" + title);
        Path HDfilepath = Paths.get(dirpath.toString(), "HD_video.torrent");
        Path LDfilepath = Paths.get(dirpath.toString(), "LD_video.torrent");
        System.out.println("Running torrent for: " + title);
        if (Files.notExists(HDfilepath)){
            try {
                gcpService.createTorrent(title, true);
                System.out.println("HD Torrent file created");
            } catch (IOException e) {
                System.out.println("Error creating HD torrent file" + e.getMessage());
            }
        }else {
            NetflixApplication.torrentManager.addTorrent(dirpath, HDfilepath);
            System.out.println("HD torrent added");
        }
        if (Files.notExists(LDfilepath)){
            try {
                gcpService.createTorrent(title, false);
                System.out.println("LD Torrent file created");
            } catch (IOException e) {
                System.out.println("Error creating LD torrent file" + e.getMessage());
            }
        }else {
            NetflixApplication.torrentManager.addTorrent(dirpath, HDfilepath);
            System.out.println("LD torrent added");
        }
        return ResponseEntity.ok("Torrent is now Running");
    }


//    @DeleteMapping("/title/{title}")
//    public ResponseEntity<Void> deleteByTitle(@PathVariable String title) {
//        cassandraService.deleteMediaByTitle(title);
//        gcpDelete.deleteMediaByTitle(title);
//        return ResponseEntity.noContent().build();
//    }


    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticateRequest(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Received authentication request");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.error("No Bearer token provided or invalid format. Header: {}", authHeader);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("No Bearer token provided");
        }

        String token = authHeader.substring(7);
        logger.info("Attempting to verify token");
        logger.info("Token: {}", token);

        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            logger.info("Token verified successfully for user: {}", decodedToken.getUid());
            return ResponseEntity.ok().build();
        } catch (FirebaseAuthException e) {
            logger.error("Token verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        }
    }



    /**
     * Processes and uploads new media content to backend.
     * Handles both media file upload to GCP and metadata storage in Cassandra.
     * @param mediaForm DTO containing media details including title, video file, and thumbnail
     * @return ResponseEntity containing the created media's details
     * @throws IOException if there's an error processing the media files
     * @throws InterruptedException if the upload process is interrupted
     */
    @PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> processMedia(@ModelAttribute @RequestBody MediaRequestDTO mediaForm) throws IOException, InterruptedException {
        boolean ifExists = cassandraService.ifMediaExists(mediaForm.getTitle());
        if (ifExists) {
            return ResponseEntity.badRequest().body("Media already exists");
        }
        logger.info("At upload");
        logger.info("Inserting on bucket");
        Map<String, String> bucketPaths = gcpService.upload(mediaForm.getTitle(), mediaForm.getVideoFile(), mediaForm.getThumbnail());
        logger.info("Successfully inserted on bucket");
        logger.info("Inserting on Cassandra");
        MediaResponseDTO response = cassandraService.insertMedia(mediaForm, bucketPaths);
        logger.info("Successfully inserted on cassandra");
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(uri).body(response);
    }


    /**
     * Retrieves all media entries from the database.
     * @return ResponseEntity containing a list of all media items
     */
    @GetMapping
    public ResponseEntity<List<MediaResponseDTO>> getAllMedia() {
        return ResponseEntity.ok(cassandraService.getAllMedia());
    }


    /**
     * Retrieves a specific media entry by its UUID.
     * @param id UUID of the media item to retrieve
     * @return ResponseEntity containing the requested media item's details
     */
    @GetMapping("/{id}")
    public ResponseEntity<MediaResponseDTO> getMediaById(@PathVariable UUID id) {
        return ResponseEntity.ok(cassandraService.getMediaById(id));
    }


    /**
     * Searches for media entries by title.
     * @param title Title of the media to search for
     * @return ResponseEntity containing a list of matching media items
     */
    @GetMapping("/title/{title}")
    public ResponseEntity<List<MediaResponseDTO>> getMediaByTitle(@PathVariable String title) {
        return ResponseEntity.ok(cassandraService.getMediaByTitle(title));
    }


    /**
     * Retrieves all media entries of a specific genre.
     * @param genre Genre to filter media by
     * @return ResponseEntity containing a list of media items in the specified genre
     */
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<MediaResponseDTO>> getMediaByGenre(@PathVariable String genre) {
        return ResponseEntity.ok(cassandraService.getMediaByGenre(genre));
    }


    /**
     * Deletes a media entry by its UUID.
     * Removes both the database entry and associated files from storage.
     * @param id UUID of the media item to delete
     * @return ResponseEntity with no content indicating successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        cassandraService.deleteMediaById(id);
        return ResponseEntity.noContent().build();
    }


    /**
     * Deletes all media entries with a specific title.
     * Removes both database entries and associated files from storage.
     * @param title Title of the media items to delete
     * @return ResponseEntity with no content indicating successful deletion
     */
    @DeleteMapping("/title/{title}")
    public ResponseEntity<Void> deleteByTitle(@PathVariable String title) {
        cassandraService.deleteMediaByTitle(title);
        gcpDelete.deleteMediaByTitle(title);
        return ResponseEntity.noContent().build();
    }


    /**
     * Deletes all media entries with a specific title.
     * Removes both database entries and associated files from storage.
     * @return ResponseEntity with no content indicating successful deletion
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAllMedia() {
        cassandraService.deleteAllMedia();
        return ResponseEntity.noContent().build();
    }
}