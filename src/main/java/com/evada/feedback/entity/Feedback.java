package com.evada.feedback.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** 제보함: 문의 / 오류 제보 / 테마 신청 */
@Entity
@Table(name = "feedbacks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "feedback_type")
    private FeedbackType type;

    @Column(nullable = false, length = 1000)
    private String content;

    // 방/플레이는 선택값이라 id만 보관 (조회 시 조인 불필요)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "play_id")
    private Long playId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Feedback(FeedbackType type, String content, Long roomId, Long playId, LocalDateTime createdAt) {
        this.type = type;
        this.content = content;
        this.roomId = roomId;
        this.playId = playId;
        this.createdAt = createdAt;
    }
}
