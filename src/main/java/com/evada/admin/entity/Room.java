package com.evada.admin.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(nullable = false, length = 20)
    private String title;

 // 변경 후
    @Column(name = "image_key", nullable = false)
    private String imageKey;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "room_status")
    private Status status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Room(String title, String imageKey) {
        this.title = title;
        this.imageKey = imageKey;
        this.status = Status.PRIVATE; // 등록 직후엔 비공개가 기본값
        this.createdAt = LocalDateTime.now();
    }

    public void changeVisibility(boolean visible) {
        this.status = visible ? Status.PUBLIC : Status.PRIVATE;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeStatus(Status status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isPublic() {
        return status == Status.PUBLIC;
    }

    public enum Status {
        PUBLIC, PRIVATE
    }
}