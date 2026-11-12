package com.evada.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evada.admin.dto.RoomCreateRequest;
import com.evada.admin.dto.RoomCreateResponse;
import com.evada.admin.dto.RoomStatusResponse;
import com.evada.common.exception.BusinessException;
import com.evada.common.exception.ErrorCode;
import com.evada.admin.entity.Room;
import com.evada.admin.image.ImageCategory;
import com.evada.admin.image.ImageService;
import com.evada.admin.repository.RoomRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final ImageService imageService;

    @Transactional
    public RoomCreateResponse createGame(RoomCreateRequest request) {
        imageService.validateImageKey(request.imageKey(), ImageCategory.ROOM);

        Room room = Room.builder()
                .title(request.title())
                .imageKey(request.imageKey())
                .build();

        Room saved = roomRepository.save(room);
        return RoomCreateResponse.of(saved, imageService.toUrl(saved.getImageKey()));
    }

    @Transactional
    public RoomStatusResponse changeStatus(Long roomId, Room.Status status) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        room.changeStatus(status);
        return new RoomStatusResponse(room.getRoomId(), room.getStatus());
    }
}