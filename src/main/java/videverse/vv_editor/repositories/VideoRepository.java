package videverse.vv_editor.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import videverse.vv_editor.models.Video;

public interface VideoRepository extends JpaRepository<Video, Long> {
}