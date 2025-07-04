package com.example.myapp.session;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import com.example.myapp.dto.UserDTO;

@Component
@SessionScope
public class AuthSession {
  private String authHeader;
  private UserDTO user;

  public void clear() { authHeader = null; user = null; }
  public boolean isAuthenticated() { return authHeader != null; }
  public String getAuthHeader() {
    return authHeader;
  }
  public void setAuthHeader(String authHeader) {
    this.authHeader = authHeader;
  }
  public UserDTO getUser() {
    return user;
  }
  public void setUser(UserDTO user) {
    this.user = user;
  }
}
