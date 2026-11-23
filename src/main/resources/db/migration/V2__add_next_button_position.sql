-- =========================================================
-- EVADA 스키마 변경 (PostgreSQL / Flyway V2)
--
-- 목적: 콘텐츠(방 안의 각 화면)마다 "다음 화면으로 넘어가는 버튼"의
--       위치를 관리자 페이지에서 조정할 수 있도록 좌표 컬럼 추가
--
-- 설계 포인트:
--   - 픽셀이 아닌 이미지 기준 상대 좌표(%)로 저장 -> 화면 크기 무관하게 동일 위치 렌더링
--   - 버튼 터치 영역(너비/높이)도 % 단위로 함께 저장 (선택 사항, 필요 없으면
--     next_button_width / next_button_height 관련 라인만 지우면 됩니다)
--   - 기획팀이 아직 위치를 지정하지 않은 콘텐츠는 NULL -> 프론트에서 기본 위치로 폴백
-- =========================================================

ALTER TABLE contents
    ADD COLUMN next_button_x      NUMERIC(5,2),  -- 이미지 가로 기준 % (0.00 ~ 100.00)
    ADD COLUMN next_button_y      NUMERIC(5,2),  -- 이미지 세로 기준 % (0.00 ~ 100.00)
    ADD COLUMN next_button_width  NUMERIC(5,2),  -- 버튼 터치 영역 가로 % (선택)
    ADD COLUMN next_button_height NUMERIC(5,2);  -- 버튼 터치 영역 세로 % (선택)

COMMENT ON COLUMN contents.next_button_x      IS '다음 화면 버튼 X 위치 (이미지 가로 기준 %, 0~100)';
COMMENT ON COLUMN contents.next_button_y      IS '다음 화면 버튼 Y 위치 (이미지 세로 기준 %, 0~100)';
COMMENT ON COLUMN contents.next_button_width  IS '다음 화면 버튼 터치 영역 가로 % (선택, NULL이면 프론트 기본값 사용)';
COMMENT ON COLUMN contents.next_button_height IS '다음 화면 버튼 터치 영역 세로 % (선택, NULL이면 프론트 기본값 사용)';

-- 값 범위 검증 (0~100 밖의 값이 들어가는 실수 방지)
ALTER TABLE contents
    ADD CONSTRAINT chk_next_button_x_range
        CHECK (next_button_x IS NULL OR (next_button_x >= 0 AND next_button_x <= 100)),
    ADD CONSTRAINT chk_next_button_y_range
        CHECK (next_button_y IS NULL OR (next_button_y >= 0 AND next_button_y <= 100)),
    ADD CONSTRAINT chk_next_button_width_range
        CHECK (next_button_width IS NULL OR (next_button_width >= 0 AND next_button_width <= 100)),
    ADD CONSTRAINT chk_next_button_height_range
        CHECK (next_button_height IS NULL OR (next_button_height >= 0 AND next_button_height <= 100));
