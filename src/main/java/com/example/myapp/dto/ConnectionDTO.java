package com.example.myapp.dto;

public class ConnectionDTO {
    private Long userId;
    private Long friendId;
    private String friendEmail;
    private String friendName;

    public ConnectionDTO() {
    }

    public ConnectionDTO(Long userId, Long friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }

    public ConnectionDTO(Long userId, Long friendId, String friendEmail, String friendName) {
        this.userId = userId;
        this.friendId = friendId;
        this.friendEmail = friendEmail;
        this.friendName = friendName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }

    public String getFriendEmail() {
        return friendEmail;
    }

    public void setFriendEmail(String friendEmail) {
        this.friendEmail = friendEmail;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }
}
