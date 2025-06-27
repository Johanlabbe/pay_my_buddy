package com.example.myapp.service;

import java.util.List;

import com.example.myapp.dto.ConnectionDTO;

public interface ConnectionService {
  List<ConnectionDTO> getConnections(Long userId);
  ConnectionDTO add(ConnectionDTO dto);
  void remove(Long userId, Long friendId);
}
