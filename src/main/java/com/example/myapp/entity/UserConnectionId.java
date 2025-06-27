package com.example.myapp.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserConnectionId implements Serializable {

    private Long userId;
    private Long connectionId;

    public UserConnectionId() {}

    public UserConnectionId(Long userId, Long connectionId) {
        this.userId = userId;
        this.connectionId = connectionId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getConnectionId() { return connectionId; }
    public void setConnectionId(Long connectionId) { this.connectionId = connectionId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserConnectionId)) return false;
        UserConnectionId that = (UserConnectionId) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(connectionId, that.connectionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, connectionId);
    }
}
