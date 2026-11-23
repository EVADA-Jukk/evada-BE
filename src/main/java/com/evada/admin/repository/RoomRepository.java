package com.evada.admin.repository;

import com.evada.admin.entity.Room;
import com.evada.room.repository.RoomSummaryView;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomRepository extends JpaRepository<Room, Long> {

    String SUMMARY_SELECT = """
            SELECT r.room_id   AS "roomId",
                   r.title     AS "title",
                   r.image_key AS "imageKey",
                   COUNT(p.play_id) AS "playCount",
                   AVG(EXTRACT(EPOCH FROM (p.end_at - p.start_at)))
                       FILTER (WHERE p.status = 'COMPLETED') AS "averageClearSeconds"
            FROM rooms r
            LEFT JOIN plays p ON p.room_id = r.room_id
            """;

    /** 사용자용 방 목록: PUBLIC 방 + 플레이 수 + 평균 클리어 시간 */
    @Query(value = SUMMARY_SELECT + """
            WHERE r.status = 'PUBLIC'
            GROUP BY r.room_id
            ORDER BY r.created_at DESC, r.room_id DESC
            """,
            countQuery = "SELECT COUNT(*) FROM rooms r WHERE r.status = 'PUBLIC'",
            nativeQuery = true)
    Page<RoomSummaryView> findPublicSummaries(Pageable pageable);

    @Query(value = SUMMARY_SELECT + """
            WHERE r.status = 'PUBLIC' AND r.room_id = :roomId
            GROUP BY r.room_id
            """,
            nativeQuery = true)
    Optional<RoomSummaryView> findPublicSummary(@Param("roomId") Long roomId);
}
