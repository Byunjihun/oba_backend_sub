package oba.backend.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiRequestDto {
    private Long articleId;
    private String title;
    private String content;
}
