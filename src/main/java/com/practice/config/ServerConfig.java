package com.practice.config;

import com.practice.routing.Router;

public record ServerConfig(int port, Router router) {}