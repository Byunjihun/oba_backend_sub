package oba.backend.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiResponseDto {
    private String result; // GPT 응답 텍스트 전체
}
