package com.evada.play.controller;

import com.evada.play.dto.AnswerRevealResponse;
import com.evada.play.dto.AnswerSubmitRequest;
import com.evada.play.dto.AnswerSubmitResponse;
import com.evada.play.dto.HintResponse;
import com.evada.play.dto.PlayContentResponse;
import com.evada.play.dto.PlayEndResponse;
import com.evada.play.dto.PlayResponse;
import com.evada.play.dto.PlayResultResponse;
import com.evada.play.dto.PlayStartRequest;
import com.evada.play.dto.ProgressResponse;
import com.evada.play.dto.TimerResponse;
import com.evada.play.service.PlayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "플레이")
@RestController
@RequestMapping("/api/plays")
@RequiredArgsConstructor
public class PlayController {

    private final PlayService playService;

    @Operation(summary = "게임 시작", description = "닉네임 등록 + 플레이 생성 + 타이머 시작. PUBLIC 방만 가능.")
    @PostMapping
    public ResponseEntity<PlayResponse> start(@Valid @RequestBody PlayStartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playService.start(request));
    }

    @Operation(summary = "플레이 상태 조회", description = "재접속 시 currentContentId 화면부터 이어서 진행합니다.")
    @GetMapping("/{playId}")
    public PlayResponse getPlay(@PathVariable Long playId) {
        return playService.getPlay(playId);
    }

    @Operation(summary = "콘텐츠별 진행 상태", description = "COMPLETED: 지나온 화면(이전 버튼으로 이동 가능), PLAYING: 현재 화면, LOCKED: 아직 못 감")
    @GetMapping("/{playId}/contents")
    public List<PlayContentResponse> getPlayContents(@PathVariable Long playId) {
        return playService.getPlayContents(playId);
    }

    @Operation(summary = "문제 정답 제출", description = "문제 풀이 + 정답 검증 + 오답 처리. 오답이어도 200, correct=false. 공백·대소문자 무시.")
    @PostMapping("/{playId}/contents/{contentId}/submit")
    public AnswerSubmitResponse submit(
            @PathVariable Long playId,
            @PathVariable Long contentId,
            @Valid @RequestBody AnswerSubmitRequest request) {
        return playService.submitAnswer(playId, contentId, request.submittedAnswer());
    }

    @Operation(summary = "힌트 보기", description = "문제당 힌트 1개. 처음 볼 때만 hintCount가 늘어납니다.")
    @PostMapping("/{playId}/contents/{contentId}/hint")
    public HintResponse useHint(@PathVariable Long playId, @PathVariable Long contentId) {
        return playService.useHint(playId, contentId);
    }

    @Operation(summary = "정답 보기", description = "힌트를 본 문제만 가능. 해당 문제는 해결 처리되고 answerRevealCount가 늘어납니다.")
    @PostMapping("/{playId}/contents/{contentId}/answer")
    public AnswerRevealResponse revealAnswer(@PathVariable Long playId, @PathVariable Long contentId) {
        return playService.revealAnswer(playId, contentId);
    }

    @Operation(summary = "타이머 조회", description = "제한시간 없음. 서버 시간 기준 경과 시간(초).")
    @GetMapping("/{playId}/timer")
    public TimerResponse getTimer(@PathVariable Long playId) {
        return playService.getTimer(playId);
    }

    @Operation(summary = "진행률 조회")
    @GetMapping("/{playId}/progress")
    public ProgressResponse getProgress(@PathVariable Long playId) {
        return playService.getProgress(playId);
    }

    @Operation(summary = "게임 클리어", description = "모든 문제를 해결해야 합니다. status=COMPLETED")
    @PostMapping("/{playId}/complete")
    public PlayEndResponse complete(@PathVariable Long playId) {
        return playService.complete(playId);
    }

    @Operation(summary = "게임 나가기", description = "status=EXITED. 기록은 보존됩니다.")
    @PostMapping("/{playId}/exit")
    public PlayEndResponse exit(@PathVariable Long playId) {
        return playService.exit(playId);
    }

    @Operation(summary = "결과 조회", description = "클리어 또는 나가기 후 조회. rank는 클리어한 경우에만 (경과시간 → 힌트+정답보기 횟수 순).")
    @GetMapping("/{playId}/result")
    public PlayResultResponse getResult(@PathVariable Long playId) {
        return playService.getResult(playId);
    }
}
