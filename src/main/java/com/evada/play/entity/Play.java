package com.evada.play.entity;

import com.evada.admin.entity.Room;
import com.evada.common.exception.BusinessException;
import com.evada.common.exception.ErrorCode;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "plays")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Play {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "play_id")
    private Long playId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "play_status")
    private PlayStatus status;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(nullable = false)
    private Short progress;

    @Column(name = "hint_count", nullable = false)
    private Short hintCount;

    @Column(name = "answer_reveal_count", nullable = false)
    private Short answerRevealCount;

    private Play(Room room, String nickname, LocalDateTime startAt) {
        this.room = room;
        this.nickname = nickname;
        this.status = PlayStatus.PLAYING;
        this.startAt = startAt;
        this.progress = 0;
        this.hintCount = 0;
        this.answerRevealCount = 0;
    }

    /** 게임 시작 = 타이머 시작 (start_at 기록) */
    public static Play start(Room room, String nickname, LocalDateTime now) {
        return new Play(room, nickname, now);
    }

    public boolean isPlaying() {
        return status == PlayStatus.PLAYING;
    }

    public void validatePlaying() {
        if (!isPlaying()) {
            throw new BusinessException(ErrorCode.PLAY_ALREADY_ENDED);
        }
    }

    public void updateProgress(int solvedCount, int totalProblemCount) {
        int value = totalProblemCount == 0 ? 100 : solvedCount * 100 / totalProblemCount;
        this.progress = (short) value;
    }

    public void increaseHintCount() {
        this.hintCount = (short) (hintCount + 1);
    }

    public void increaseAnswerRevealCount() {
        this.answerRevealCount = (short) (answerRevealCount + 1);
    }

    public void complete(LocalDateTime now) {
        validatePlaying();
        this.status = PlayStatus.COMPLETED;
        this.endAt = now;
    }

    public void exit(LocalDateTime now) {
        validatePlaying();
        this.status = PlayStatus.EXITED;
        this.endAt = now;
    }

    /** 종료됐으면 end_at까지, 진행 중이면 now까지의 경과 시간(초) */
    public long elapsedSeconds(LocalDateTime now) {
        LocalDateTime end = endAt != null ? endAt : now;
        return Duration.between(startAt, end).getSeconds();
    }
}
