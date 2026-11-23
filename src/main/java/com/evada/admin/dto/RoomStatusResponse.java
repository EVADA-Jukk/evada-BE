package com.evada.admin.dto;

import com.evada.admin.entity.Room;

public record RoomStatusResponse(Long roomId, Room.Status status) {
}
