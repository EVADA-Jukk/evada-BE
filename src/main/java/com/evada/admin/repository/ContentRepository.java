package com.evada.admin.repository;

import com.evada.admin.entity.Content;
import com.evada.admin.entity.ContentType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentRepository extends JpaRepository<Content, Long> {

    @Query("select coalesce(max(c.sequence), 0) from Content c where c.room.roomId = :roomId")
    int findMaxSequenceByRoomId(@Param("roomId") Long roomId);

    List<Content> findByRoomRoomIdOrderBySequenceAsc(Long roomId);

    long countByRoomRoomIdAndType(Long roomId, ContentType type);
}
