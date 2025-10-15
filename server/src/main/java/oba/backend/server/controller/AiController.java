package oba.backend.server.controller;

import lombok.RequiredArgsConstructor;
import oba.backend.server.dto.AiRequestDto;
import oba.backend.server.dto.AiResponseDto;
import oba.backend.server.service.AiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/analyze")
    public AiResponseDto analyzeArticle(@RequestBody AiRequestDto request) {
        return aiService.callPythonServer(request);
    }
}
