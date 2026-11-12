package com.evada.room.service;

import com.evada.admin.entity.Content;
import com.evada.admin.entity.Room;
import com.evada.admin.image.ImageService;
import com.evada.admin.repository.ContentRepository;
import com.evada.admin.repository.RoomRepository;
import com.evada.common.dto.PageResponse;
import com.evada.common.exception.BusinessException;
import com.evada.common.exception.ErrorCode;
import com.evada.room.dto.RoomContentResponse;
import com.evada.room.dto.RoomSummaryResponse;
import com.evada.room.repository.RoomSummaryView;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomQueryService {

    private final RoomRepository roomRepository;
    private final ContentRepository contentRepository;
    private final ImageService imageService;

    public PageResponse<RoomSummaryResponse> getRooms(int page, int size) {
        return PageResponse.of(
                roomRepository.findPublicSummaries(PageRequest.of(page, size)),
                this::toSummary);
    }

    public RoomSummaryResponse getRoom(Long roomId) {
        return roomRepository.findPublicSummary(roomId)
                .map(this::toSummary)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
    }

    public List<RoomContentResponse> getContents(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .filter(Room::isPublic)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        return contentRepository.findByRoomRoomIdOrderBySequenceAsc(room.getRoomId()).stream()
                .map(this::toContent)
                .toList();
    }

    private RoomSummaryResponse toSummary(RoomSummaryView view) {
        Double average = view.getAverageClearSeconds();
        return new RoomSummaryResponse(
                view.getRoomId(),
                view.getTitle(),
                imageService.toUrl(view.getImageKey()),
                view.getPlayCount() == null ? 0 : view.getPlayCount(),
                average == null ? null : Math.round(average));
    }

    private RoomContentResponse toContent(Content content) {
        return new RoomContentResponse(
                content.getContentId(),
                content.getSequence(),
                content.getType(),
                content.getInputType(),
                content.getImageKey() == null ? null : imageService.toUrl(content.getImageKey()),
                content.getHint() != null && !content.getHint().isBlank(),
                content.getNextButtonX(),
                content.getNextButtonY(),
                content.getNextButtonWidth(),
                content.getNextButtonHeight());
    }
}
