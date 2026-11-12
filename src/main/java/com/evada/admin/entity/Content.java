package com.evada.admin.entity;

import java.math.BigDecimal;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_id")
    private Long contentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private Integer sequence;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false, columnDefinition = "content_type")
    private ContentType type;

    @Column(length = 255)
    private String answer;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "input_type", columnDefinition = "content_input_type")
    private ContentInputType inputType;

    @Column(name = "image_key")
    private String imageKey;

    @Column(length = 255)
    private String hint;

    @Column(name = "next_button_x")
    private BigDecimal nextButtonX;

    @Column(name = "next_button_y")
    private BigDecimal nextButtonY;

    @Column(name = "next_button_width")
    private BigDecimal nextButtonWidth;

    @Column(name = "next_button_height")
    private BigDecimal nextButtonHeight;

    @Builder
    private Content(Room room, Integer sequence, ContentType type, String answer,
                    ContentInputType inputType, String imageKey, String hint,
                    BigDecimal nextButtonX, BigDecimal nextButtonY,
                    BigDecimal nextButtonWidth, BigDecimal nextButtonHeight) {
        this.room = room;
        this.sequence = sequence;
        this.type = type;
        this.answer = answer;
        this.inputType = inputType;
        this.imageKey = imageKey;
        this.hint = hint;
        this.nextButtonX = nextButtonX;
        this.nextButtonY = nextButtonY;
        this.nextButtonWidth = nextButtonWidth;
        this.nextButtonHeight = nextButtonHeight;
    }
}