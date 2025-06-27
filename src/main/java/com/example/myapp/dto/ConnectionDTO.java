package com.example.myapp.dto;

public class ConnectionDTO {
    private Long userId;
    private Long friendId;
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public Long getConnectionId() {
        return friendId;
    }
    public void setConnectionId(Long friendId) {
        this.friendId = friendId;
    }
}
