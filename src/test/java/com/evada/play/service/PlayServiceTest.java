package com.evada.play.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.evada.admin.entity.Content;
import com.evada.admin.entity.ContentInputType;
import com.evada.admin.entity.ContentType;
import com.evada.admin.entity.Room;
import com.evada.admin.repository.ContentRepository;
import com.evada.admin.repository.RoomRepository;
import com.evada.common.exception.BusinessException;
import com.evada.common.exception.ErrorCode;
import com.evada.play.dto.AnswerRevealResponse;
import com.evada.play.dto.AnswerSubmitResponse;
import com.evada.play.dto.HintResponse;
import com.evada.play.dto.PlayContentStatus;
import com.evada.play.dto.PlayEndResponse;
import com.evada.play.dto.PlayStartRequest;
import com.evada.play.entity.ContentProgress;
import com.evada.play.entity.Play;
import com.evada.play.entity.PlayStatus;
import com.evada.play.entity.ProgressStatus;
import com.evada.play.repository.ContentProgressRepository;
import com.evada.play.repository.PlayRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 방 구성: STORY(1) → PROBLEM(2, 정답 "1234", 힌트 있음) → STORY(3) → PROBLEM(4, 정답 "up left", 힌트 없음)
 */
class PlayServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime START = LocalDateTime.of(2026, 11, 12, 21, 0, 0);

    private final Clock clock = Clock.fixed(START.plusMinutes(10).atZone(ZONE).toInstant(), ZONE);

    private final FakePlayRepository playRepository = new FakePlayRepository();
    private final FakeProgressRepository progressRepository = new FakeProgressRepository();
    private final RoomRepository roomRepository = org.mockito.Mockito.mock(RoomRepository.class);
    private final ContentRepository contentRepository = org.mockito.Mockito.mock(ContentRepository.class);

    private PlayService playService;
    private Room room;
    private Content story1;
    private Content problem2;
    private Content story3;
    private Content problem4;

    @BeforeEach
    void setUp() {
        playService = new PlayService(playRepository.mock, progressRepository.mock, roomRepository, contentRepository, clock);

        room = Room.builder().title("어둠의 방").imageKey("rooms/a.png").build();
        ReflectionTestUtils.setField(room, "roomId", 1L);
        room.changeStatus(Room.Status.PUBLIC);

        story1 = content(1L, 1, ContentType.STORY, null, null);
        problem2 = content(2L, 2, ContentType.PROBLEM, "1234", "첫 번째 힌트");
        story3 = content(3L, 3, ContentType.STORY, null, null);
        problem4 = content(4L, 4, ContentType.PROBLEM, "up left", null);

        given(roomRepository.findById(1L)).willReturn(Optional.of(room));
        given(contentRepository.findByRoomRoomIdOrderBySequenceAsc(1L))
                .willReturn(List.of(story1, problem2, story3, problem4));
    }

    @Test
    void 게임_시작하면_PLAYING이고_첫_화면부터() {
        var response = playService.start(new PlayStartRequest(1L, "  탈출러 "));

        assertThat(response.status()).isEqualTo(PlayStatus.PLAYING);
        assertThat(response.nickname()).isEqualTo("탈출러");
        assertThat(response.startAt()).isEqualTo(START.plusMinutes(10));
        assertThat(response.currentContentId()).isEqualTo(1L);
    }

    @Test
    void 비공개_방은_시작할_수_없다() {
        room.changeStatus(Room.Status.PRIVATE);

        assertError(() -> playService.start(new PlayStartRequest(1L, "탈출러")), ErrorCode.ROOM_PRIVATE);
    }

    @Test
    void 오답이면_correct_false이고_재입력_가능() {
        Play play = startedPlay();

        AnswerSubmitResponse wrong = playService.submitAnswer(play.getPlayId(), 2L, "0000");
        assertThat(wrong.correct()).isFalse();
        assertThat(wrong.status()).isEqualTo(ProgressStatus.PLAYING);
        assertThat(wrong.nextContentId()).isNull();

        AnswerSubmitResponse right = playService.submitAnswer(play.getPlayId(), 2L, " 12 34 ");
        assertThat(right.correct()).isTrue();
        assertThat(right.nextContentId()).isEqualTo(3L);
        assertThat(right.progress()).isEqualTo(50);
        assertThat(right.allSolved()).isFalse();
    }

    @Test
    void 정답은_공백과_대소문자를_무시한다() {
        assertThat(PlayService.matches("up left", "UPLEFT")).isTrue();
        assertThat(PlayService.matches("1234", "1235")).isFalse();
    }

    @Test
    void 앞_문제를_안_풀면_다음_문제는_잠겨있다() {
        Play play = startedPlay();

        assertError(() -> playService.submitAnswer(play.getPlayId(), 4L, "upleft"), ErrorCode.CONTENT_LOCKED);
    }

    @Test
    void 스토리에는_정답을_제출할_수_없다() {
        Play play = startedPlay();

        assertError(() -> playService.submitAnswer(play.getPlayId(), 1L, "x"), ErrorCode.CONTENT_NOT_PROBLEM);
    }

    @Test
    void 이미_푼_문제는_다시_제출할_수_없다() {
        Play play = startedPlay();
        playService.submitAnswer(play.getPlayId(), 2L, "1234");

        assertError(() -> playService.submitAnswer(play.getPlayId(), 2L, "1234"), ErrorCode.ALREADY_SOLVED);
    }

    @Test
    void 힌트는_문제마다_처음_볼_때만_카운트된다() {
        Play play = startedPlay();

        HintResponse first = playService.useHint(play.getPlayId(), 2L);
        HintResponse again = playService.useHint(play.getPlayId(), 2L);

        assertThat(first.hint()).isEqualTo("첫 번째 힌트");
        assertThat(again.hintCount()).isEqualTo(1);
    }

    @Test
    void 힌트가_없는_문제는_힌트를_볼_수_없다() {
        Play play = startedPlay();
        playService.submitAnswer(play.getPlayId(), 2L, "1234");

        assertError(() -> playService.useHint(play.getPlayId(), 4L), ErrorCode.HINT_NOT_AVAILABLE);
    }

    @Test
    void 정답_보기는_힌트를_본_뒤에만_가능하다() {
        Play play = startedPlay();

        assertError(() -> playService.revealAnswer(play.getPlayId(), 2L), ErrorCode.HINT_NOT_USED);
    }

    @Test
    void 정답_보기를_하면_문제가_해결되고_횟수는_한_번만_센다() {
        Play play = startedPlay();
        playService.useHint(play.getPlayId(), 2L);

        AnswerRevealResponse first = playService.revealAnswer(play.getPlayId(), 2L);
        AnswerRevealResponse again = playService.revealAnswer(play.getPlayId(), 2L);

        assertThat(first.answer()).isEqualTo("1234");
        assertThat(first.nextContentId()).isEqualTo(3L);
        assertThat(first.progress()).isEqualTo(50);
        assertThat(again.answerRevealCount()).isEqualTo(1);
        // 정답 보기로 해결했으니 다음 문제가 열린다
        assertThat(playService.submitAnswer(play.getPlayId(), 4L, "upleft").correct()).isTrue();
    }

    @Test
    void 직접_푼_문제는_정답_보기를_할_수_없다() {
        Play play = startedPlay();
        playService.useHint(play.getPlayId(), 2L);
        playService.submitAnswer(play.getPlayId(), 2L, "1234");

        assertError(() -> playService.revealAnswer(play.getPlayId(), 2L), ErrorCode.ALREADY_SOLVED);
    }

    @Test
    void 모든_문제를_풀어야_클리어할_수_있다() {
        Play play = startedPlay();
        playService.submitAnswer(play.getPlayId(), 2L, "1234");

        assertError(() -> playService.complete(play.getPlayId()), ErrorCode.NOT_ALL_PROBLEMS_SOLVED);

        AnswerSubmitResponse last = playService.submitAnswer(play.getPlayId(), 4L, "upleft");
        assertThat(last.allSolved()).isTrue();
        assertThat(last.nextContentId()).isNull();

        ReflectionTestUtils.setField(play, "startAt", START); // 10분 전에 시작한 것으로
        PlayEndResponse end = playService.complete(play.getPlayId());
        assertThat(end.status()).isEqualTo(PlayStatus.COMPLETED);
        assertThat(end.progress()).isEqualTo(100);
        assertThat(end.elapsedSeconds()).isEqualTo(600);
    }

    @Test
    void 나가기_후에는_제출할_수_없고_결과는_볼_수_있다() {
        Play play = startedPlay();
        assertError(() -> playService.getResult(play.getPlayId()), ErrorCode.RESULT_NOT_AVAILABLE);

        PlayEndResponse end = playService.exit(play.getPlayId());
        assertThat(end.status()).isEqualTo(PlayStatus.EXITED);

        assertError(() -> playService.submitAnswer(play.getPlayId(), 2L, "1234"), ErrorCode.PLAY_ALREADY_ENDED);
        assertError(() -> playService.exit(play.getPlayId()), ErrorCode.PLAY_ALREADY_ENDED);
        assertThat(playService.getResult(play.getPlayId()).rank()).isNull();
    }

    @Test
    void 진행_상태는_푼_문제_다음_화면이_현재_화면이다() {
        Play play = startedPlay();
        playService.submitAnswer(play.getPlayId(), 2L, "1234");

        var contents = playService.getPlayContents(play.getPlayId());

        assertThat(contents).extracting(c -> c.status()).containsExactly(
                PlayContentStatus.COMPLETED, PlayContentStatus.COMPLETED,
                PlayContentStatus.PLAYING, PlayContentStatus.LOCKED);
        assertThat(playService.getPlay(play.getPlayId()).currentContentId()).isEqualTo(3L);
    }

    // ------------------------------------------------------------------

    private Play startedPlay() {
        Long playId = playService.start(new PlayStartRequest(1L, "탈출러")).playId();
        return playRepository.store.get((int) (playId - 1));
    }

    private Content content(Long id, int sequence, ContentType type, String answer, String hint) {
        Content content = Content.builder()
                .room(room)
                .sequence(sequence)
                .type(type)
                .answer(answer)
                .inputType(type == ContentType.PROBLEM ? ContentInputType.STRING : null)
                .hint(hint)
                .imageKey("contents/" + id + ".png")
                .build();
        ReflectionTestUtils.setField(content, "contentId", id);
        return content;
    }

    private static void assertError(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(expected);
    }

    /** save/findById를 메모리에 저장하는 PlayRepository mock */
    static class FakePlayRepository {
        final List<Play> store = new ArrayList<>();
        final PlayRepository mock = org.mockito.Mockito.mock(PlayRepository.class);

        FakePlayRepository() {
            given(mock.save(any(Play.class))).willAnswer(invocation -> {
                Play play = invocation.getArgument(0);
                if (play.getPlayId() == null) {
                    ReflectionTestUtils.setField(play, "playId", (long) store.size() + 1);
                    store.add(play);
                }
                return play;
            });
            given(mock.findById(any())).willAnswer(invocation -> {
                long id = invocation.getArgument(0);
                return id >= 1 && id <= store.size() ? Optional.of(store.get((int) id - 1)) : Optional.empty();
            });
        }
    }

    /** save/findByPlayPlayId를 메모리에 저장하는 ContentProgressRepository mock */
    static class FakeProgressRepository {
        final List<ContentProgress> store = new ArrayList<>();
        final ContentProgressRepository mock = org.mockito.Mockito.mock(ContentProgressRepository.class);

        FakeProgressRepository() {
            given(mock.save(any(ContentProgress.class))).willAnswer(invocation -> {
                ContentProgress progress = invocation.getArgument(0);
                store.add(progress);
                return progress;
            });
            given(mock.findByPlayPlayId(any())).willAnswer(invocation -> {
                Long playId = invocation.getArgument(0);
                return store.stream().filter(p -> p.getPlay().getPlayId().equals(playId)).toList();
            });
        }
    }
}
