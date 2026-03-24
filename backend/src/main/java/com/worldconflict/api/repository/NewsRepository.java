package com.worldconflict.api.repository;

import com.worldconflict.api.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    List<News> findTop30ByOrderByPublishedAtDesc();
    List<News> findByConflictZoneIdOrderByPublishedAtDesc(Long conflictZoneId);
}
