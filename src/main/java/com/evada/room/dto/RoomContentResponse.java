package com.evada.room.dto;

import com.evada.admin.entity.ContentInputType;
import com.evada.admin.entity.ContentType;
import java.math.BigDecimal;

/** 플레이 화면에 그릴 콘텐츠. 정답과 힌트 내용은 내려주지 않는다. */
public record RoomContentResponse(
        Long contentId,
        int sequence,
        ContentType type,
        ContentInputType inputType,
        String imageUrl,
        boolean hasHint,
        BigDecimal nextButtonX,
        BigDecimal nextButtonY,
        BigDecimal nextButtonWidth,
        BigDecimal nextButtonHeight
) {
}
