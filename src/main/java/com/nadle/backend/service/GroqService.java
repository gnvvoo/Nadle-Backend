package com.nadle.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nadle.backend.dto.QuizResponse;
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

    public RouteRecommendResponse recommendCourse(List<TourSpotItem> spots, int duration, String requirements) {
        if (mock) {
            log.info("Groq mock 모드 - 실제 API 호출 생략");
            return buildMockResponse(spots, duration);
        }
        String prompt = buildPrompt(spots, duration, requirements);
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
                "max_tokens", 1024
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

    private String buildPrompt(List<TourSpotItem> spots, int duration, String requirements) {
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 자전거 여행 코스 추천 전문가입니다.\n");
        sb.append("아래 관광지 목록에서 자전거로 ").append(duration).append("분 동안 여행하기 좋은 관광지를 선택하여 최적의 코스를 추천해주세요.\n\n");
        sb.append("조건:\n");
        sb.append("- 이동 거리와 시간을 고려하여 효율적인 동선으로 구성하세요.\n");
        sb.append("- 이동 거리와 시간을 고려하여 반드시 2~3개의 관광지로 구성하세요.\n");
        sb.append("- 각 관광지 방문 이유를 한 문장으로 간결하게 설명하세요.\n");
        sb.append("- 전체 코스를 한 줄로 소개하는 aiSummary를 작성하세요.\n");
        sb.append("- 모든 응답은 반드시 한국어로 작성하세요.\n");
        sb.append(duration).append("분.");
        if (requirements != null && !requirements.isBlank()) {
            sb.append(" 요구사항: ").append(requirements).append(".");
        }
        sb.append("\n");

        List<TourSpotItem> candidates = spots.size() > 15 ? spots.subList(0, 15) : spots;
        sb.append("[");
        for (int i = 0; i < candidates.size(); i++) {
            TourSpotItem spot = candidates.get(i);
            sb.append("{\"id\":\"").append(spot.getContentId()).append("\"");
            sb.append(",\"name\":\"").append(spot.getTitle()).append("\"");
            sb.append(",\"x\":").append(spot.getMapx());
            sb.append(",\"y\":").append(spot.getMapy()).append("}");
            if (i < candidates.size() - 1) sb.append(",");
        }
        sb.append("]\n");
        sb.append("반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요:\n"); 
        sb.append("JSON만 출력:{\"aiSummary\":\"...\",\"spots\":[{\"contentId\":\"\",\"title\":\"\",\"sequence\":1,\"reason\":\"\",\"mapx\":0.0,\"mapy\":0.0}]}");

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

    public QuizResponse generateQuiz(String title, String overview) {
        String cleaned = overview.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
        String truncated = cleaned.length() > 800 ? cleaned.substring(0, 800) : cleaned;

        String prompt = "한국어로만 응답. 아래 관광지 설명을 읽고 OX 퀴즈 1개를 만들어라.\n"
                + "조건:\n"
                + "- 문제는 반드시 '~이다.' 형태의 서술문으로 끝낼 것\n"
                + "- 관광지 이름을 문제에 포함할 것\n"
                + "- 설명에서 직접 근거를 찾을 수 있는 문제만 출제할 것 (뻔하거나 이름만 묻는 문제 금지)\n"
                + "- 정답(answer)은 true 또는 false 중 설명 내용에 맞는 것으로 설정할 것\n"
                + "- 해설은 설명에서 근거를 직접 인용하여 2문장 이내로 작성할 것\n\n"
                + "예시1) {\"question\":\"경복궁은 조선 태조 때 창건된 궁궐이다.\",\"answer\":true,\"explanation\":\"경복궁은 1395년 태조 이성계가 창건하였습니다. 조선 왕조 최초의 궁궐입니다.\"}\n"
                + "예시2) {\"question\":\"불국사는 신라 시대가 아닌 고려 시대에 창건된 사찰이다.\",\"answer\":false,\"explanation\":\"불국사는 신라 경덕왕 10년(751년)에 창건되었습니다. 고려 시대가 아닌 신라 시대의 건축물입니다.\"}\n\n"
                + "관광지: " + title + "\n"
                + "설명: " + truncated + "\n"
                + "JSON만 출력:{\"question\":\"문제\",\"answer\":true,\"explanation\":\"해설\"}";

        log.info("Groq 퀴즈 생성 요청 - 관광지: {}", title);
        String responseText = callGroqApi(prompt);

        try {
            String cleanedJson = responseText.trim().replaceAll("(?s)^```json|^```|```$", "").trim();
            JsonNode root = objectMapper.readTree(cleanedJson);
            return new QuizResponse(
                    root.path("question").asText(),
                    root.path("answer").asBoolean(),
                    root.path("explanation").asText()
            );
        } catch (Exception e) {
            log.error("퀴즈 응답 파싱 실패: {}", e.getMessage());
            throw new RuntimeException("퀴즈 생성에 실패했습니다.");
        }
    }

    private RouteRecommendResponse buildMockResponse(List<TourSpotItem> spots, int duration) {
        List<SpotDto> selected = new ArrayList<>();
        int count = Math.min(3, spots.size());
        for (int i = 0; i < count; i++) {
            TourSpotItem s = spots.get(i);
            selected.add(new SpotDto(s.getContentId(), s.getTitle(), i + 1, "(mock) 추천 관광지", s.getMapx(), s.getMapy()));
        }
        return new RouteRecommendResponse(duration, "(mock) 테스트용 자전거 여행 코스", selected);
    }
}
