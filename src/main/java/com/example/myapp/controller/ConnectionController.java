package com.example.myapp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.myapp.dto.ConnectionDTO;
import com.example.myapp.service.ConnectionService;

import jakarta.validation.Valid;

@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping("/api/users/{userId}/connections")
public class ConnectionController {

  private final ConnectionService connectionService;

  public ConnectionController(ConnectionService connectionService) {
    this.connectionService = connectionService;
  }

  @GetMapping
  public List<ConnectionDTO> listConnections(@PathVariable Long userId) {
    return connectionService.getConnections(userId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ConnectionDTO addConnection(
      @PathVariable Long userId,
      @Valid @RequestBody ConnectionDTO dto) {
    dto.setUserId(userId);
    return connectionService.add(dto);
  }

  @DeleteMapping("/{friendId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeConnection(
      @PathVariable Long userId,
      @PathVariable Long connectionId) {
    connectionService.remove(userId, connectionId);
  }
}

