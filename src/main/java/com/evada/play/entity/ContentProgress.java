package com.evada.play.entity;

import com.evada.admin.entity.Content;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** 플레이 안에서 문제(PROBLEM) 하나의 진행 상태 */
@Entity
@Table(name = "content_progress")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Long progressId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "play_id", nullable = false)
    private Play play;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(name = "submitted_answer", length = 255)
    private String submittedAnswer;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "progress_status")
    private ProgressStatus status;

    @Column(name = "solved_at")
    private LocalDateTime solvedAt;

    @Column(name = "hint_used", nullable = false)
    private boolean hintUsed;

    @Column(name = "answer_revealed", nullable = false)
    private boolean answerRevealed;

    public ContentProgress(Play play, Content content) {
        this.play = play;
        this.content = content;
        this.status = ProgressStatus.PLAYING;
    }

    public boolean isCompleted() {
        return status == ProgressStatus.COMPLETED;
    }

    public void submit(String answer, boolean correct, LocalDateTime now) {
        this.submittedAnswer = answer;
        if (correct) {
            this.status = ProgressStatus.COMPLETED;
            this.solvedAt = now;
        }
    }

    /** @return 이번에 처음 사용했으면 true */
    public boolean useHint() {
        if (hintUsed) {
            return false;
        }
        this.hintUsed = true;
        return true;
    }

    /** @return 이번에 처음 정답을 공개했으면 true */
    public boolean revealAnswer(LocalDateTime now) {
        if (answerRevealed) {
            return false;
        }
        this.answerRevealed = true;
        this.status = ProgressStatus.COMPLETED;
        this.solvedAt = now;
        return true;
    }
}
