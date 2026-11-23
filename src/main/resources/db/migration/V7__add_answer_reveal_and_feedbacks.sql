-- =========================================================
-- V7: 정답 보기 / 힌트 사용 여부 / 제보함
-- =========================================================

-- 플레이별 정답 보기 횟수 (힌트 사용 후에만 가능, 결과·랭킹에 반영)
ALTER TABLE plays
    ADD COLUMN answer_reveal_count SMALLINT NOT NULL DEFAULT 0
        CHECK (answer_reveal_count >= 0);

COMMENT ON COLUMN plays.answer_reveal_count
    IS '정답 보기를 사용한 문제 수';

-- 문제별 힌트 사용 / 정답 보기 여부 (중복 카운트 방지, 정답 보기 조건 확인용)
ALTER TABLE content_progress
    ADD COLUMN hint_used       BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN answer_revealed BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN content_progress.hint_used
    IS '이 플레이에서 해당 문제의 힌트를 확인했는지';

COMMENT ON COLUMN content_progress.answer_revealed
    IS '이 플레이에서 해당 문제의 정답 보기를 사용했는지';


-- ---------------------------------------------------------
-- FEEDBACKS (제보함: 문의 / 제보 / 테마 신청)
-- ---------------------------------------------------------

CREATE TYPE feedback_type AS ENUM (
    'INQUIRY',
    'REPORT',
    'THEME_REQUEST'
);

CREATE TABLE feedbacks (
    feedback_id BIGSERIAL PRIMARY KEY,
    type        feedback_type NOT NULL,
    content     VARCHAR(1000) NOT NULL,
    room_id     BIGINT REFERENCES rooms(room_id),
    play_id     BIGINT REFERENCES plays(play_id),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN feedbacks.type
    IS 'INQUIRY: 문의, REPORT: 오류·문제 제보, THEME_REQUEST: 테마 신청';

COMMENT ON COLUMN feedbacks.room_id
    IS '제보한 방 (선택)';

COMMENT ON COLUMN feedbacks.play_id
    IS '제보 당시 플레이 (선택)';

CREATE INDEX idx_feedbacks_type_created_at
    ON feedbacks(type, created_at DESC);

-- 방 목록 통계(플레이 수, 평균 클리어 시간) 집계용
CREATE INDEX idx_plays_room_id_status
    ON plays(room_id, status);
