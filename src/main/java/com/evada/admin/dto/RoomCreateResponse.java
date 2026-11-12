package com.evada.admin.dto;

import com.evada.admin.entity.Room;

public record RoomCreateResponse(
        Long roomId,
        String imageUrl
) {
    public static RoomCreateResponse of(Room room, String imageUrl) {
        return new RoomCreateResponse(room.getRoomId(), imageUrl);
    }
}