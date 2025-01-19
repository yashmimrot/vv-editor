package videverse.vv_editor.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import videverse.vv_editor.models.Video;
import videverse.vv_editor.repositories.VideoRepository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VideoService {

  @Autowired
  private VideoRepository videoRepository;

  private static final String VIDEO_STORAGE_PATH = "videos/";

  public Video uploadVideo(MultipartFile file, long maxSize, int minDuration, int maxDuration) throws IOException {
    if (file.getSize() > maxSize) {
      throw new IllegalArgumentException("File size exeeds the maximum linit of " + maxSize / (1024 * 1024) + "mb");
    }

    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("video/")) {
      throw new IllegalArgumentException("Invalid file type. Please upload a valid vpdeo file.");
    }

    String videoName = file.getOriginalFilename();
    if (videoName == null || videoName.isEmpty()) {
      throw new IllegalArgumentException("File name is empty");
    }

    String videoPath = VIDEO_STORAGE_PATH + videoName;
    Path destinationPath = Paths.get(videoPath);

    try (InputStream inputStream = file.getInputStream()) {
      Files.copy(inputStream, destinationPath);
    }

    int duration = getVideoDuration(videoPath);

    if (duration < minDuration) {
      throw new IllegalArgumentException("Video is too shorr. Minimum duration is " + minDuration + " seconds");
    }
    if (duration > maxDuration) {
      throw new IllegalArgumentException("Vidwo is too long. Maximum diration is " + maxDuration + " seconds.");
    }

    Video video = new Video();
    video.setName(videoName);
    video.setPath(videoPath);
    video.setDuration(duration);

    return videoRepository.save(video);
  }
  public Video trimVideo(Long videoId, int startSeconds, int endSeconds) throws Exception {
    // Fetch the video from the database by its ID
    Optional<Video> videoOptional = videoRepository.findById(videoId);
    if (videoOptional.isEmpty()) {
      throw new Exception("Video not found");
    }

    Video video = videoOptional.get();

    if (endSeconds <= startSeconds) {
      throw new IllegalArgumentException("End time must be greater than start time");
    }

    int videoDuration = video.getDuration();
    if (startSeconds >= videoDuration) {
      throw new IllegalArgumentException("Start time exceeds video duration");
    }
    if (endSeconds > videoDuration) {
      endSeconds = videoDuration;
    }

    String inputFilePath = video.getPath();
    String outputFilePath = VIDEO_STORAGE_PATH + "trimmed_" + video.getName(); // Save as a new file

    try {
      trimWithFFmpeg(inputFilePath, outputFilePath, startSeconds, endSeconds);
    } catch (IOException e) {
      throw new Exception("Failed to trim video: " + e.getMessage(), e);
    }

    video.setPath(outputFilePath);
    video.setDuration(endSeconds - startSeconds);

    return videoRepository.save(video);
  }

  private void trimWithFFmpeg(String inputFilePath, String outputFilePath, int startSeconds, int endSeconds) throws IOException {

    //command for trim
    String command = String.format("ffmpeg -i %s -ss %d -to %d -c:v libx264 -c:a aac -strict experimental %s",
        inputFilePath, startSeconds, endSeconds, outputFilePath);

    ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
    Process process = processBuilder.start();

    // wait  to finish and check if there is errors
    try {
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new IOException("FFmpeg process failed with exit code: " + exitCode);
      }
    } catch (InterruptedException e) {
      //error handling for interruptions
      Thread.currentThread().interrupt();
      throw new IOException("Video trimming process was interrupted", e);
    }
  }

  private int getVideoDuration(String videoPath) throws IOException {
    String ffmpegCommand = "ffmpeg -i " + videoPath + " 2>&1 | grep 'Duration' | awk '{print $2}' | tr -d ,";

    ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", ffmpegCommand);
    Process process = processBuilder.start();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String durationStr = reader.readLine();
      if (durationStr != null) {
        String[] timeParts = durationStr.split(":");
        int minutes = Integer.parseInt(timeParts[1]);
        int seconds = Integer.parseInt(timeParts[2].split("\\.")[0]);
        return minutes * 60 + seconds;  // Return total duration in seconds
      }
    } catch (IOException e) {
      throw new IOException("Failed to extract video duration", e);
    }
    return 0;
  }

  // Merge videos
  public Video mergeVideos(List<Long> videoIds) throws Exception {
    List<Video> videos = videoRepository.findAllById(videoIds);
    if (videos.isEmpty()) {
      throw new Exception("Videos not found");
    }

    List<String> videoPaths = new ArrayList<>();
    for (Video video : videos) {
      videoPaths.add(video.getPath());
    }
    String mergedVideoName = "merged_video_" + System.currentTimeMillis() + ".mp4"; // Using timestamp for uniqueness
    String mergedVideoPath = VIDEO_STORAGE_PATH + mergedVideoName;

    try {
      mergeWithFFmpeg(videoPaths, mergedVideoPath);
    } catch (IOException e) {
      throw new Exception("Failed to merge videos: " + e.getMessage(), e);
    }

    Video mergedVideo = new Video();
    mergedVideo.setName(mergedVideoName);
    mergedVideo.setPath(mergedVideoPath);

    int totalDuration = videos.stream()
        .mapToInt(Video::getDuration)
        .sum();
    mergedVideo.setDuration(totalDuration);

    return videoRepository.save(mergedVideo);
  }

  private void mergeWithFFmpeg(List<String> videoPaths, String mergedVideoPath) throws IOException {
    File listFile = new File(VIDEO_STORAGE_PATH + "videos_to_merge.txt");
    Process process = getProcess(videoPaths, mergedVideoPath, listFile);

    try {
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new IOException("FFmpeg process failed with exit code: " + exitCode);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Video merging process was interrupted", e);
    } finally {
      //cleam up
      listFile.delete();
    }
  }

  private static Process getProcess(List<String> videoPaths, String mergedVideoPath, File listFile) throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(listFile))) {
      for (String videoPath : videoPaths) {
        writer.write("file '" + videoPath + "'\n");
      }
    }

    //merging command
    String command = String.format("ffmpeg -f concat -safe 0 -i %s -c:v libx264 -c:a aac -strict experimental -y %s",
        listFile.getAbsolutePath(), mergedVideoPath);

    ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
    Process process = processBuilder.start();
    return process;
  }

  public Video getVideo(Long videoId) {
    Optional<Video> video = videoRepository.findById(videoId);
    return video.orElseThrow(() -> new RuntimeException("Video not found"));
  }

  public String generateVideoLink(Long videoId, long expiryTimeInSeconds) {
    String link = "http://localhost:8080/api/videos/" + videoId;
    return link + "?expiry=" + expiryTimeInSeconds;
  }
}