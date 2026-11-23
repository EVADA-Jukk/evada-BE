-- V4__rename_room_image_url_to_image_key.sql
ALTER TABLE rooms RENAME COLUMN image_url TO image_key;