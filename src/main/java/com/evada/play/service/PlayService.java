package com.evada.play.service;

import com.evada.admin.entity.Content;
import com.evada.admin.entity.ContentType;
import com.evada.admin.entity.Room;
import com.evada.admin.repository.ContentRepository;
import com.evada.admin.repository.RoomRepository;
import com.evada.common.exception.BusinessException;
import com.evada.common.exception.ErrorCode;
import com.evada.play.dto.AnswerRevealResponse;
import com.evada.play.dto.AnswerSubmitResponse;
import com.evada.play.dto.HintResponse;
import com.evada.play.dto.PlayContentResponse;
import com.evada.play.dto.PlayContentStatus;
import com.evada.play.dto.PlayEndResponse;
import com.evada.play.dto.PlayResponse;
import com.evada.play.dto.PlayResultResponse;
import com.evada.play.dto.PlayStartRequest;
import com.evada.play.dto.ProgressResponse;
import com.evada.play.dto.TimerResponse;
import com.evada.play.entity.ContentProgress;
import com.evada.play.entity.Play;
import com.evada.play.entity.PlayStatus;
import com.evada.play.repository.ContentProgressRepository;
import com.evada.play.repository.PlayRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayService {

    private final PlayRepository playRepository;
    private final ContentProgressRepository progressRepository;
    private final RoomRepository roomRepository;
    private final ContentRepository contentRepository;
    private final Clock clock;

    // ------------------------------------------------------------------
    // 시작 / 조회
    // ------------------------------------------------------------------

    /** 닉네임 등록 + 게임 시작 + 타이머 시작 */
    @Transactional
    public PlayResponse start(PlayStartRequest request) {
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        if (!room.isPublic()) {
            throw new BusinessException(ErrorCode.ROOM_PRIVATE);
        }
        Play play = playRepository.save(Play.start(room, request.nickname().strip(), now()));
        return toPlayResponse(play);
    }

    public PlayResponse getPlay(Long playId) {
        return toPlayResponse(findPlay(playId));
    }

    /** 콘텐츠별 진행 상태. COMPLETED 화면은 '이전' 버튼으로 다시 볼 수 있다. */
    public List<PlayContentResponse> getPlayContents(Long playId) {
        PlayState state = loadState(findPlay(playId));
        Integer currentSequence = state.currentSequence();

        return state.contents().stream()
                .map(content -> {
                    ContentProgress progress = state.progress(content);
                    return new PlayContentResponse(
                            content.getContentId(),
                            content.getSequence(),
                            content.getType(),
                            statusOf(content, currentSequence),
                            progress != null && progress.isHintUsed(),
                            progress != null && progress.isAnswerRevealed());
                })
                .toList();
    }

    public TimerResponse getTimer(Long playId) {
        Play play = findPlay(playId);
        return new TimerResponse(play.getStartAt(), play.getEndAt(), play.elapsedSeconds(now()));
    }

    public ProgressResponse getProgress(Long playId) {
        Play play = findPlay(playId);
        PlayState state = loadState(play);
        return new ProgressResponse(playId, state.solvedCount(), state.problemCount(), play.getProgress());
    }

    // ------------------------------------------------------------------
    // 문제 풀이 / 힌트 / 정답 보기
    // ------------------------------------------------------------------

    /** 문제 풀이 + 정답 검증 + 오답 처리 */
    @Transactional
    public AnswerSubmitResponse submitAnswer(Long playId, Long contentId, String submittedAnswer) {
        Play play = findPlay(playId);
        play.validatePlaying();
        PlayState state = loadState(play);
        Content content = state.findProblem(contentId);
        state.validateUnlocked(content);

        ContentProgress progress = progressOf(state, play, content);
        if (progress.isCompleted()) {
            throw new BusinessException(ErrorCode.ALREADY_SOLVED);
        }

        boolean correct = matches(content.getAnswer(), submittedAnswer);
        progress.submit(submittedAnswer, correct, now());
        if (correct) {
            play.updateProgress(state.solvedCount(), state.problemCount());
        }

        return new AnswerSubmitResponse(
                contentId,
                correct,
                progress.getStatus(),
                correct ? state.nextContentId(content) : null,
                play.getProgress(),
                state.allSolved());
    }

    /** 힌트 확인. 문제마다 처음 볼 때만 hint_count가 늘어난다. */
    @Transactional
    public HintResponse useHint(Long playId, Long contentId) {
        Play play = findPlay(playId);
        play.validatePlaying();
        PlayState state = loadState(play);
        Content content = state.findProblem(contentId);
        state.validateUnlocked(content);

        if (content.getHint() == null || content.getHint().isBlank()) {
            throw new BusinessException(ErrorCode.HINT_NOT_AVAILABLE);
        }

        ContentProgress progress = progressOf(state, play, content);
        if (progress.useHint()) {
            play.increaseHintCount();
        }
        return new HintResponse(contentId, content.getHint(), play.getHintCount());
    }

    /** 정답 보기. 힌트를 본 문제만 가능하고, 해당 문제는 해결 처리된다. 다시 호출해도 횟수는 한 번만 센다. */
    @Transactional
    public AnswerRevealResponse revealAnswer(Long playId, Long contentId) {
        Play play = findPlay(playId);
        play.validatePlaying();
        PlayState state = loadState(play);
        Content content = state.findProblem(contentId);
        state.validateUnlocked(content);

        ContentProgress progress = state.progress(content);
        if (progress == null || !progress.isHintUsed()) {
            throw new BusinessException(ErrorCode.HINT_NOT_USED);
        }
        if (progress.isCompleted() && !progress.isAnswerRevealed()) {
            throw new BusinessException(ErrorCode.ALREADY_SOLVED);
        }

        if (progress.revealAnswer(now())) {
            play.increaseAnswerRevealCount();
            play.updateProgress(state.solvedCount(), state.problemCount());
        }

        return new AnswerRevealResponse(
                contentId,
                content.getAnswer(),
                state.nextContentId(content),
                play.getProgress(),
                play.getAnswerRevealCount(),
                state.allSolved());
    }

    // ------------------------------------------------------------------
    // 종료 / 결과
    // ------------------------------------------------------------------

    /** 게임 클리어. 모든 문제를 해결해야 한다. */
    @Transactional
    public PlayEndResponse complete(Long playId) {
        Play play = findPlay(playId);
        play.validatePlaying();
        if (!loadState(play).allSolved()) {
            throw new BusinessException(ErrorCode.NOT_ALL_PROBLEMS_SOLVED);
        }
        play.complete(now());
        return toEndResponse(play);
    }

    /** 게임 나가기 (종료 확인 팝업의 '종료하기') */
    @Transactional
    public PlayEndResponse exit(Long playId) {
        Play play = findPlay(playId);
        play.exit(now());
        return toEndResponse(play);
    }

    /** 결과 화면. 클리어한 경우에만 순위가 있다. */
    public PlayResultResponse getResult(Long playId) {
        Play play = findPlay(playId);
        if (play.isPlaying()) {
            throw new BusinessException(ErrorCode.RESULT_NOT_AVAILABLE);
        }
        Room room = play.getRoom();
        boolean cleared = play.getStatus() == PlayStatus.COMPLETED;
        return new PlayResultResponse(
                play.getPlayId(),
                room.getRoomId(),
                room.getTitle(),
                play.getNickname(),
                play.getStatus(),
                play.elapsedSeconds(now()),
                play.getProgress(),
                play.getHintCount(),
                play.getAnswerRevealCount(),
                cleared ? playRepository.findRank(playId) : null,
                playRepository.countByRoomRoomIdAndStatus(room.getRoomId(), PlayStatus.COMPLETED));
    }

    // ------------------------------------------------------------------
    // 내부
    // ------------------------------------------------------------------

    private Play findPlay(Long playId) {
        return playRepository.findById(playId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAY_NOT_FOUND));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private PlayState loadState(Play play) {
        List<Content> contents = contentRepository.findByRoomRoomIdOrderBySequenceAsc(play.getRoom().getRoomId());
        Map<Long, ContentProgress> progressByContentId = new HashMap<>();
        for (ContentProgress progress : progressRepository.findByPlayPlayId(play.getPlayId())) {
            progressByContentId.put(progress.getContent().getContentId(), progress);
        }
        return new PlayState(contents, progressByContentId);
    }

    private ContentProgress progressOf(PlayState state, Play play, Content content) {
        ContentProgress progress = state.progress(content);
        if (progress == null) {
            progress = progressRepository.save(new ContentProgress(play, content));
            state.put(progress);
        }
        return progress;
    }

    private PlayResponse toPlayResponse(Play play) {
        PlayState state = loadState(play);
        return new PlayResponse(
                play.getPlayId(),
                play.getRoom().getRoomId(),
                play.getNickname(),
                play.getStatus(),
                play.getStartAt(),
                play.getEndAt(),
                play.elapsedSeconds(now()),
                play.getProgress(),
                play.getHintCount(),
                play.getAnswerRevealCount(),
                state.currentContentId());
    }

    private PlayEndResponse toEndResponse(Play play) {
        return new PlayEndResponse(
                play.getPlayId(),
                play.getStatus(),
                play.getProgress(),
                play.getEndAt(),
                play.elapsedSeconds(now()));
    }

    private static PlayContentStatus statusOf(Content content, Integer currentSequence) {
        if (currentSequence == null || content.getSequence() < currentSequence) {
            return PlayContentStatus.COMPLETED;
        }
        return content.getSequence().equals(currentSequence)
                ? PlayContentStatus.PLAYING
                : PlayContentStatus.LOCKED;
    }

    /** 공백을 무시하고 대소문자를 구분하지 않는다. (예: "up left" == "UPLEFT") */
    static boolean matches(String answer, String submitted) {
        if (answer == null || submitted == null) {
            return false;
        }
        return normalize(answer).equals(normalize(submitted));
    }

    private static String normalize(String value) {
        return value.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }
}
