package com.example.springai.service;

import java.util.List;

public record ChatResult(String answer, List<String> contexts, String tool) {
}
