-- =========================================================
-- EVADA 스키마 변경 (PostgreSQL / Flyway V3)
-- 목적: 게임(방탈출) 등록 시 대표이미지 URL을 저장할 컬럼 추가
--
-- description/difficulty/playTime은 컬럼으로 안 만듦
--   - description, difficulty: 대표이미지 안에 표현 (별도 저장 불필요)
--   - playTime: 스톱워치 방식(플레이 시작~종료 실측)이라 room 속성이 아니라
--     plays.start_at / plays.end_at로 이미 커버됨
-- =========================================================

ALTER TABLE rooms
    ADD COLUMN image_url VARCHAR(255);

COMMENT ON COLUMN rooms.image_url IS '게임(방탈출) 대표이미지 URL — 목록/상세에서 노출';