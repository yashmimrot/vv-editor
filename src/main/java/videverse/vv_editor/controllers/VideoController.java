package videverse.vv_editor.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import videverse.vv_editor.models.Video;
import videverse.vv_editor.services.VideoService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

  @Autowired
  private VideoService videoService;

  @PostMapping("/upload")
  public ResponseEntity<Video> uploadVideo(@RequestParam("file") MultipartFile file) throws IOException {
    long maxSize = 25 * 1024 * 1024;  // 25 MB
    int maxDuration = 60; // 60 seconds
    int minDuration = 5;  // 5 seconds

    Video video = videoService.uploadVideo(file, maxSize, minDuration, maxDuration);

    return ResponseEntity.status(HttpStatus.CREATED).body(video);
  }

  @PostMapping("/trim/{videoId}")
  public ResponseEntity<Video> trimVideo(@PathVariable Long videoId,
                                         @RequestParam int startSeconds,
                                         @RequestParam int endSeconds) throws Exception {
    Video video = videoService.trimVideo(videoId, startSeconds, endSeconds);
    return ResponseEntity.ok(video);
  }

  @PostMapping("/merge")
  public ResponseEntity<Video> mergeVideos(@RequestBody List<Long> videoIds) throws Exception {
    Video video = videoService.mergeVideos(videoIds);
    return ResponseEntity.ok(video);
  }

  @GetMapping("/{videoId}")
  public ResponseEntity<Video> getVideo(@PathVariable Long videoId) {
    Video video = videoService.getVideo(videoId);
    return ResponseEntity.ok(video);
  }

  @GetMapping("/share/{videoId}")
  public ResponseEntity<String> shareVideo(@PathVariable Long videoId,
                                           @RequestParam long expiryTimeInSeconds) {
    String link = videoService.generateVideoLink(videoId, expiryTimeInSeconds);
    return ResponseEntity.ok("Shareable link: " + link);
  }
}
