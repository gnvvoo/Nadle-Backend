package com.nadle.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nadle.backend.dto.RouteRecommendResponse;
import com.nadle.backend.dto.SpotDto;
import com.nadle.backend.dto.external.TourSpotItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    private static final Logger log = LoggerFactory.getLogger(GroqService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String endpoint;
    private final String model;
    private final boolean mock;

    public GroqService(RestTemplate restTemplate,
                       ObjectMapper objectMapper,
                       @Value("${groq-api.api-key}") String apiKey,
                       @Value("${groq-api.endpoint}") String endpoint,
                       @Value("${groq-api.model}") String model,
                       @Value("${groq-api.mock:false}") boolean mock) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.endpoint = endpoint;
        this.model = model;
        this.mock = mock;
    }

    public RouteRecommendResponse recommendCourse(List<TourSpotItem> spots, int duration, int spotCount, String requirements) {
        if (mock) {
            log.info("Groq mock 모드 - 실제 API 호출 생략");
            return buildMockResponse(spots, duration, spotCount);
        }
        String prompt = buildPrompt(spots, duration, spotCount, requirements);
        String responseText = callGroqApi(prompt);
        return parseResponse(responseText, duration);
    }

    private String callGroqApi(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7,
                "max_tokens", 8192
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        log.info("Groq API 호출 시작 (model: {})", model);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(endpoint, entity, Map.class);

        if (response == null) {
            throw new RuntimeException("Groq API 응답이 없습니다.");
        }

        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String text = (String) message.get("content");

            log.info("Groq 응답 수신 완료");
            return text;

        } catch (Exception e) {
            log.error("Groq 응답 파싱 실패: {}", e.getMessage());
            throw new RuntimeException("Groq API 응답 파싱에 실패했습니다.");
        }
    }

    private String buildPrompt(List<TourSpotItem> spots, int duration, int spotCount, String requirements) {
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 자전거 여행 코스 추천 전문가입니다.\n");
        sb.append("아래 관광지 목록에서 자전거로 ").append(duration).append("분 동안 여행하기 좋은 ");
        sb.append(spotCount).append("개의 관광지를 선택하여 최적의 코스를 추천해주세요.\n\n");
        sb.append("조건:\n");
        sb.append("- 이동 거리와 시간을 고려하여 효율적인 동선으로 구성하세요.\n");
        sb.append("- 각 관광지 방문 이유를 한 문장으로 간결하게 설명하세요.\n");
        sb.append("- 전체 코스를 한 줄로 소개하는 aiSummary를 작성하세요.\n");
        sb.append("- 모든 응답은 반드시 한국어로 작성하세요.\n");
        if (requirements != null && !requirements.isBlank()) {
            sb.append("- 사용자 요구사항: ").append(requirements).append("\n");
        }
        sb.append("\n");

        List<TourSpotItem> candidates = spots.size() > 20 ? spots.subList(0, 20) : spots;
        sb.append("관광지 목록 (JSON):\n[\n");
        for (int i = 0; i < candidates.size(); i++) {
            TourSpotItem spot = candidates.get(i);
            sb.append("  {");
            sb.append("\"id\": \"").append(spot.getContentId()).append("\", ");
            sb.append("\"name\": \"").append(spot.getTitle()).append("\", ");
            sb.append("\"x\": ").append(spot.getMapx()).append(", ");
            sb.append("\"y\": ").append(spot.getMapy());
            sb.append("}");
            if (i < candidates.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]\n\n");

        sb.append("반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요:\n");
        sb.append("{\"aiSummary\":\"코스 한줄 소개\",\"spots\":[{\"contentId\":\"관광지ID\",\"title\":\"관광지명\",\"sequence\":1,\"reason\":\"추천 이유\",\"mapx\":경도값,\"mapy\":위도값}]}");

        return sb.toString();
    }

    private RouteRecommendResponse parseResponse(String jsonText, int duration) {
        try {
            String cleaned = jsonText.trim().replaceAll("(?s)^```json|^```|```$", "").trim();

            JsonNode root = objectMapper.readTree(cleaned);
            String aiSummary = root.path("aiSummary").asText();

            List<SpotDto> spots = new ArrayList<>();
            for (JsonNode spotNode : root.path("spots")) {
                spots.add(new SpotDto(
                        spotNode.path("contentId").asText(),
                        spotNode.path("title").asText(),
                        spotNode.path("sequence").asInt(),
                        spotNode.path("reason").asText(),
                        spotNode.path("mapx").asDouble(),
                        spotNode.path("mapy").asDouble()
                ));
            }

            return new RouteRecommendResponse(duration, aiSummary, spots);

        } catch (Exception e) {
            log.error("Groq 응답 JSON 파싱 실패: {}", e.getMessage());
            throw new RuntimeException("AI 응답을 파싱하는 데 실패했습니다.");
        }
    }

    private RouteRecommendResponse buildMockResponse(List<TourSpotItem> spots, int duration, int spotCount) {
        List<SpotDto> selected = new ArrayList<>();
        int count = Math.min(spotCount, spots.size());
        for (int i = 0; i < count; i++) {
            TourSpotItem s = spots.get(i);
            selected.add(new SpotDto(s.getContentId(), s.getTitle(), i + 1, "(mock) 추천 관광지", s.getMapx(), s.getMapy()));
        }
        return new RouteRecommendResponse(duration, "(mock) 테스트용 자전거 여행 코스", selected);
    }
}
