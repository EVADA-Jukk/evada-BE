-- =========================================================
-- EVADA 스키마 (PostgreSQL / Flyway V1)
--
-- 반영 사항
--   1) ROOMS / CONTENTS / PLAYS / CONTENT_PROGRESS
--      모두 단일 컬럼 PK 사용
--
--   2) CONTENT_PROGRESS의 room_id 컬럼 없음
--      -> play_id -> plays.room_id
--      -> content_id -> contents.room_id
--         를 통해 방 정보 조회
--
--   3) CONTENT_PROGRESS
--      UNIQUE(play_id, content_id)
--      -> 한 플레이에서 같은 콘텐츠 진행 기록 중복 방지
--
--   4) CONTENTS
--      UNIQUE(room_id, sequence)
--      -> 한 방 안에서 콘텐츠 순서 중복 방지
--
--   5) PostgreSQL 네이티브 ENUM 사용
--
--   6) solved_at 기본값 오타 수정
--
--   7) answer / submitted_answer VARCHAR(255)
--      image_url TEXT
--
--   8) progress / hint_count SMALLINT
--
--   9) progress 범위 및 hint_count CHECK 추가
--
--  10) 중복 인덱스 제거
--
--  11) rooms.status
--      PUBLIC  : 사용자에게 공개 / 참여 가능
--      PRIVATE : 관리자만 확인 가능
-- =========================================================


-- ---------------------------------------------------------
-- ENUM 타입 정의
-- ---------------------------------------------------------

CREATE TYPE room_status AS ENUM (
    'PUBLIC',
    'PRIVATE'
);

CREATE TYPE content_type AS ENUM (
    'STORY',
    'PROBLEM'
);

CREATE TYPE content_input_type AS ENUM (
    'NUM',
    'DIRECTION',
    'STRING'
);

CREATE TYPE progress_status AS ENUM (
    'PLAYING',
    'COMPLETED'
);


-- ---------------------------------------------------------
-- ROOMS (방)
-- ---------------------------------------------------------

CREATE TABLE rooms (
    room_id     BIGSERIAL PRIMARY KEY,
    title       VARCHAR(20) NOT NULL,
    status      room_status NOT NULL DEFAULT 'PUBLIC',
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP
);

COMMENT ON COLUMN rooms.room_id
    IS '각 방에 부여되는 고유 id';

COMMENT ON COLUMN rooms.title
    IS '방 제목';

COMMENT ON COLUMN rooms.status
    IS 'PUBLIC: 사용자에게 공개 및 참여 가능, PRIVATE: 관리자만 확인 가능';

COMMENT ON COLUMN rooms.created_at
    IS '해당 방이 생성된 날짜';

COMMENT ON COLUMN rooms.updated_at
    IS '해당 방이 수정된 날짜';


-- ---------------------------------------------------------
-- rooms.updated_at 자동 갱신 트리거
-- ---------------------------------------------------------

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_rooms_updated_at
    BEFORE UPDATE ON rooms
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------
-- CONTENTS (콘텐츠)
-- ---------------------------------------------------------

CREATE TABLE contents (
    content_id  BIGSERIAL PRIMARY KEY,

    room_id     BIGINT NOT NULL
        REFERENCES rooms(room_id),

    sequence    INTEGER NOT NULL,

    type        content_type NOT NULL,

    answer      VARCHAR(255),

    input_type  content_input_type,

    image_url   TEXT,

    hint        VARCHAR(255),

    CONSTRAINT uq_contents_room_sequence
        UNIQUE (room_id, sequence)
);

COMMENT ON COLUMN contents.content_id
    IS '각 콘텐츠에 부여되는 고유 id';

COMMENT ON COLUMN contents.room_id
    IS '각 콘텐츠가 속한 방의 고유 id';

COMMENT ON COLUMN contents.sequence
    IS '방 안에서의 콘텐츠 진행 순서';

COMMENT ON COLUMN contents.type
    IS 'STORY, PROBLEM';

COMMENT ON COLUMN contents.answer
    IS '문제의 정답';

COMMENT ON COLUMN contents.input_type
    IS 'NUM, DIRECTION, STRING';

COMMENT ON COLUMN contents.image_url
    IS '콘텐츠 이미지 경로';

COMMENT ON COLUMN contents.hint
    IS '문제의 힌트 내용';


-- ---------------------------------------------------------
-- PLAYS (플레이)
-- ---------------------------------------------------------

CREATE TABLE plays (
    play_id     BIGSERIAL PRIMARY KEY,

    room_id     BIGINT NOT NULL
        REFERENCES rooms(room_id),

    nickname    VARCHAR(20) NOT NULL,

    start_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    end_at      TIMESTAMP,

    progress    SMALLINT NOT NULL DEFAULT 0
                    CHECK (progress BETWEEN 0 AND 100),

    hint_count  SMALLINT NOT NULL DEFAULT 0
                    CHECK (hint_count >= 0)
);

COMMENT ON COLUMN plays.play_id
    IS '각 플레이에 부여되는 고유 id';

COMMENT ON COLUMN plays.room_id
    IS '플레이가 진행되는 방의 고유 id';

COMMENT ON COLUMN plays.nickname
    IS '플레이어 닉네임';

COMMENT ON COLUMN plays.start_at
    IS '플레이 시작 시간';

COMMENT ON COLUMN plays.end_at
    IS '플레이 종료 시간';

COMMENT ON COLUMN plays.progress
    IS '게임 진행률 (%)';

COMMENT ON COLUMN plays.hint_count
    IS '힌트 사용 개수';


-- ---------------------------------------------------------
-- CONTENT_PROGRESS (콘텐츠 진행 관리)
-- ---------------------------------------------------------

CREATE TABLE content_progress (
    progress_id      BIGSERIAL PRIMARY KEY,

    play_id          BIGINT NOT NULL
        REFERENCES plays(play_id),

    content_id       BIGINT NOT NULL
        REFERENCES contents(content_id),

    submitted_answer VARCHAR(255),

    status           progress_status NOT NULL,

    solved_at        TIMESTAMP,

    CONSTRAINT uq_progress_play_content
        UNIQUE (play_id, content_id)
);

COMMENT ON COLUMN content_progress.progress_id
    IS '각 진행 기록에 부여되는 고유 id';

COMMENT ON COLUMN content_progress.play_id
    IS '각 플레이에 부여되는 고유 id';

COMMENT ON COLUMN content_progress.content_id
    IS '진행 중인 콘텐츠의 고유 id';

COMMENT ON COLUMN content_progress.submitted_answer
    IS '해당 문제를 풀기 위해 제출한 답안';

COMMENT ON COLUMN content_progress.status
    IS 'PLAYING, COMPLETED';

COMMENT ON COLUMN content_progress.solved_at
    IS '문제를 해결한 시간';


-- ---------------------------------------------------------
-- INDEX
-- ---------------------------------------------------------

-- 방별 플레이 조회
CREATE INDEX idx_plays_room_id
    ON plays(room_id);

-- 콘텐츠별 진행 기록 조회
CREATE INDEX idx_content_progress_content_id
    ON content_progress(content_id);
