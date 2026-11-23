-- 플레이 종료 상태 구분 (클리어 / 중도 나가기)
-- 제한시간은 두지 않으므로 FAILED는 사용하지 않는다.
CREATE TYPE play_status AS ENUM (
    'PLAYING',
    'COMPLETED',
    'EXITED'
);

ALTER TABLE plays
    ADD COLUMN status play_status NOT NULL DEFAULT 'PLAYING';

COMMENT ON COLUMN plays.status
    IS 'PLAYING: 진행 중, COMPLETED: 클리어(end_at 기록), EXITED: 중도 나가기(end_at 기록)';
