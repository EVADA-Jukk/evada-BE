package com.evada.admin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evada.admin.dto.RoomCreateRequest;
import com.evada.admin.dto.RoomCreateResponse;
import com.evada.admin.dto.RoomStatusRequest;
import com.evada.admin.dto.RoomStatusResponse;
import com.evada.admin.service.RoomService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "관리자 방")
@RestController
@RequestMapping("/api/admin/rooms")
@RequiredArgsConstructor 
public class RoomController {

	private final RoomService  gameService;

    @PostMapping
    public ResponseEntity<RoomCreateResponse> createGame(@Valid @RequestBody RoomCreateRequest request) {
        RoomCreateResponse response = gameService.createGame(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "방 공개/비공개 변경", description = "등록 직후는 PRIVATE. PUBLIC이어야 사용자 목록에 보이고 플레이할 수 있습니다.")
    @PatchMapping("/{roomId}/status")
    public RoomStatusResponse changeStatus(@PathVariable Long roomId,
                                           @Valid @RequestBody RoomStatusRequest request) {
        return gameService.changeStatus(roomId, request.status());
    }
}
