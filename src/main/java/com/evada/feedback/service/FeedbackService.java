package com.evada.feedback.service;

import com.evada.admin.repository.RoomRepository;
import com.evada.common.dto.PageResponse;
import com.evada.common.exception.BusinessException;
import com.evada.common.exception.ErrorCode;
import com.evada.feedback.dto.FeedbackCreateRequest;
import com.evada.feedback.dto.FeedbackCreateResponse;
import com.evada.feedback.dto.FeedbackResponse;
import com.evada.feedback.entity.Feedback;
import com.evada.feedback.entity.FeedbackType;
import com.evada.feedback.repository.FeedbackRepository;
import com.evada.play.entity.Play;
import com.evada.play.repository.PlayRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final RoomRepository roomRepository;
    private final PlayRepository playRepository;
    private final Clock clock;

    @Transactional
    public FeedbackCreateResponse create(FeedbackCreateRequest request) {
        Long roomId = request.roomId();
        if (request.playId() != null) {
            Play play = playRepository.findById(request.playId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PLAY_NOT_FOUND));
            // 플레이 중 제보면 방은 플레이에서 가져온다
            roomId = play.getRoom().getRoomId();
        } else if (roomId != null && !roomRepository.existsById(roomId)) {
            throw new BusinessException(ErrorCode.ROOM_NOT_FOUND);
        }

        Feedback saved = feedbackRepository.save(new Feedback(
                request.type(),
                request.content().strip(),
                roomId,
                request.playId(),
                LocalDateTime.now(clock)));
        return new FeedbackCreateResponse(saved.getFeedbackId());
    }

    /** 관리자 조회. type이 없으면 전체, 최신순. */
    public PageResponse<FeedbackResponse> getFeedbacks(FeedbackType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "feedbackId"));
        Page<Feedback> result = type == null
                ? feedbackRepository.findAll(pageable)
                : feedbackRepository.findByType(type, pageable);
        return PageResponse.of(result, FeedbackResponse::from);
    }
}
