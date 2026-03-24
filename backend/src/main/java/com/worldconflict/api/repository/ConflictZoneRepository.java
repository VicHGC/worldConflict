package com.worldconflict.api.repository;

import com.worldconflict.api.entity.ConflictZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConflictZoneRepository extends JpaRepository<ConflictZone, Long> {
    List<ConflictZone> findByIsActive(Boolean isActive);
}
