package oba.backend.server.service;

import lombok.RequiredArgsConstructor;
import oba.backend.server.dto.AiRequestDto;
import oba.backend.server.dto.AiResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class AiService {

    private final WebClient webClient;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    public AiResponseDto callPythonServer(AiRequestDto request) {
        return webClient.post()
                .uri(aiServerUrl + "/analyze")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiResponseDto.class)
                .block(); // (비동기 원하면 block() 대신 subscribe())
    }
}
