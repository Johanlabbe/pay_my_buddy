package com.example.myapp.entity;

import jakarta.persistence.*;

@Entity
public class UserConnection {

    @EmbeddedId
    private UserConnectionId id = new UserConnectionId();

    @MapsId("userId")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @MapsId("connectionId")
    @ManyToOne
    @JoinColumn(name = "connection_id")
    private User connection;

    public UserConnection() {
    }

    public UserConnection(User user, User connection) {
        this.user = user;
        this.connection = connection;
        this.id = new UserConnectionId(user.getId(), connection.getId());
    }

    public UserConnectionId getId() {
        return id;
    }

    public void setId(UserConnectionId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getConnection() {
        return this.connection;
    }

    public void setConnection(User connection) {
        this.connection = connection;
        this.id.setConnectionId(connection.getId());
    }
}
