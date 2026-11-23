package com.evada.play.repository;

import com.evada.play.entity.Play;
import com.evada.play.entity.PlayStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayRepository extends JpaRepository<Play, Long> {

    /**
     * 같은 방의 클리어 기록 중 순위.
     * 경과 시간이 짧을수록, 같으면 (힌트 + 정답 보기) 횟수가 적을수록 높다.
     */
    @Query(value = """
            SELECT COUNT(*) + 1
            FROM plays p
            JOIN plays me ON me.play_id = :playId
            WHERE p.room_id = me.room_id
              AND p.status = 'COMPLETED'
              AND p.play_id <> me.play_id
              AND ( (p.end_at - p.start_at) < (me.end_at - me.start_at)
                 OR ( (p.end_at - p.start_at) = (me.end_at - me.start_at)
                      AND (p.hint_count + p.answer_reveal_count)
                          < (me.hint_count + me.answer_reveal_count) ) )
            """, nativeQuery = true)
    long findRank(@Param("playId") Long playId);

    long countByRoomRoomIdAndStatus(Long roomId, PlayStatus status);
}
