package com.santander.farmatrack.util;

public record LoginRequest(
    String username,
    String password
) {}