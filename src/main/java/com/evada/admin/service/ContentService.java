package com.evada.admin.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.evada.admin.dto.ContentCreateRequest;
import com.evada.admin.dto.ContentCreateResponse;
import com.evada.admin.dto.ContentItemRequest;
import com.evada.admin.entity.Content;
import com.evada.admin.entity.ContentType;
import com.evada.admin.entity.Room;
import com.evada.admin.image.ImageCategory;
import com.evada.admin.image.ImageService;
import com.evada.admin.repository.ContentRepository;
import com.evada.admin.repository.RoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentService {

    private final ContentRepository contentRepository;
    private final RoomRepository roomRepository;
    private final ImageService imageService;

    @Transactional
    public ContentCreateResponse createContents(Long roomId, ContentCreateRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "존재하지 않는 방입니다."));

        int sequence = contentRepository.findMaxSequenceByRoomId(roomId);
        List<Content> contents = new ArrayList<>();

        for (ContentItemRequest item : request.contents()) {
            validateItem(item);
            imageService.validateImageKey(item.imageKey(), ImageCategory.CONTENT);

            boolean isProblem = item.type() == ContentType.PROBLEM;
            contents.add(Content.builder()
                    .room(room)
                    .sequence(++sequence)
                    .type(item.type())
                    .answer(isProblem ? item.answer() : null)
                    .inputType(isProblem ? item.inputType() : null)
                    .hint(isProblem ? item.hint() : null)
                    .imageKey(item.imageKey())
                    .nextButtonX(item.nextButtonX())
                    .nextButtonY(item.nextButtonY())
                    .nextButtonWidth(item.nextButtonWidth())
                    .nextButtonHeight(item.nextButtonHeight())
                    .build());
        }

        List<Long> ids = contentRepository.saveAll(contents).stream()
                .map(Content::getContentId)
                .toList();
        return new ContentCreateResponse(ids);
    }

    private void validateItem(ContentItemRequest item) {
        if (item.type() == ContentType.PROBLEM
                && (item.answer() == null || item.answer().isBlank() || item.inputType() == null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "문제 유형은 정답과 입력 방식이 필요합니다.");
        }
    }
}