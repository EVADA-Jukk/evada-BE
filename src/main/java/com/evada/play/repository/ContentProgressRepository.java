package com.evada.play.repository;

import com.evada.play.entity.ContentProgress;
import com.evada.play.entity.ProgressStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentProgressRepository extends JpaRepository<ContentProgress, Long> {

    Optional<ContentProgress> findByPlayPlayIdAndContentContentId(Long playId, Long contentId);

    List<ContentProgress> findByPlayPlayId(Long playId);

    long countByPlayPlayIdAndStatus(Long playId, ProgressStatus status);
}
