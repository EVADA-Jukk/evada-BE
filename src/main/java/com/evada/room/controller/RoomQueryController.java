package com.evada.room.controller;

import com.evada.common.dto.PageResponse;
import com.evada.room.dto.RoomContentResponse;
import com.evada.room.dto.RoomSummaryResponse;
import com.evada.room.service.RoomQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "방탈출(방)")
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomQueryController {

    private final RoomQueryService roomQueryService;

    @Operation(summary = "방탈출 목록", description = "PUBLIC 방만, 최신순. 카드에 플레이 수와 평균 클리어 시간(초)을 함께 내려줍니다.")
    @GetMapping
    public PageResponse<RoomSummaryResponse> getRooms(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return roomQueryService.getRooms(page, size);
    }

    @Operation(summary = "방탈출 시작 화면 정보")
    @GetMapping("/{roomId}")
    public RoomSummaryResponse getRoom(@PathVariable Long roomId) {
        return roomQueryService.getRoom(roomId);
    }

    @Operation(summary = "방 콘텐츠 목록", description = "sequence 순. 정답과 힌트 내용은 포함하지 않습니다.")
    @GetMapping("/{roomId}/contents")
    public List<RoomContentResponse> getContents(@PathVariable Long roomId) {
        return roomQueryService.getContents(roomId);
    }
}
