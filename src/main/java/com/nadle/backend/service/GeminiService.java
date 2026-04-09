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
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String endpoint;
    private final boolean mock;

    public GeminiService(RestTemplate restTemplate,
                         ObjectMapper objectMapper,
                         @Value("${gemini-api.api-key}") String apiKey,
                         @Value("${gemini-api.endpoint}") String endpoint,
                         @Value("${gemini-api.mock:false}") boolean mock) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.endpoint = endpoint;
        this.mock = mock;
    }

    /**
     * 관광지 목록과 여행 시간을 바탕으로 Gemini AI에 코스 추천을 요청한다.
     *
     * @param spots      주변 관광지 목록
     * @param duration   여행 예상 시간 (분)
     * @param spotCount  추천받을 관광지 수
     * @return AI 추천 코스 응답
     */
    public RouteRecommendResponse recommendCourse(List<TourSpotItem> spots, int duration, int spotCount) {
        if (mock) {
            log.info("Gemini mock 모드 - 실제 API 호출 생략");
            return buildMockResponse(spots, duration, spotCount);
        }
        String prompt = buildPrompt(spots, duration, spotCount);
        String geminiResponse = callGeminiApi(prompt);
        return parseGeminiResponse(geminiResponse, duration);
    }

    /**
     * 테스트용 mock 응답을 생성한다.
     */
    private RouteRecommendResponse buildMockResponse(List<TourSpotItem> spots, int duration, int spotCount) {
        List<SpotDto> selectedSpots = new ArrayList<>();
        int count = Math.min(spotCount, spots.size());
        for (int i = 0; i < count; i++) {
            TourSpotItem s = spots.get(i);
            selectedSpots.add(new SpotDto(s.getContentId(), s.getTitle(), i + 1, "(mock) 추천 관광지", s.getMapx(), s.getMapy()));
        }
        return new RouteRecommendResponse(duration, "(mock) 테스트용 자전거 여행 코스", selectedSpots);
    }

    /**
     * Gemini에 전달할 프롬프트를 구성한다.
     */
    private String buildPrompt(List<TourSpotItem> spots, int duration, int spotCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 자전거 여행 코스 추천 전문가입니다.\n");
        sb.append("아래 관광지 목록에서 자전거로 ").append(duration).append("분 동안 여행하기 좋은 ");
        sb.append(spotCount).append("개의 관광지를 선택하여 최적의 코스를 추천해주세요.\n\n");
        sb.append("조건:\n");
        sb.append("- 이동 거리와 시간을 고려하여 효율적인 동선으로 구성하세요.\n");
        sb.append("- 각 관광지 방문 이유를 한 문장으로 간결하게 설명하세요.\n");
        sb.append("- 전체 코스를 한 줄로 소개하는 aiSummary를 작성하세요.\n\n");

        sb.append("관광지 목록 (JSON):\n");
        sb.append("[\n");
        for (int i = 0; i < spots.size(); i++) {
            TourSpotItem spot = spots.get(i);
            sb.append("  {");
            sb.append("\"contentId\": \"").append(spot.getContentId()).append("\", ");
            sb.append("\"title\": \"").append(spot.getTitle()).append("\", ");
            sb.append("\"mapx\": ").append(spot.getMapx()).append(", ");
            sb.append("\"mapy\": ").append(spot.getMapy());
            if (spot.getAddr1() != null) {
                sb.append(", \"addr\": \"").append(spot.getAddr1()).append("\"");
            }
            sb.append("}");
            if (i < spots.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]\n\n");

        sb.append("반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요:\n");
        sb.append("{\n");
        sb.append("  \"aiSummary\": \"코스 한줄 소개\",\n");
        sb.append("  \"spots\": [\n");
        sb.append("    {\n");
        sb.append("      \"contentId\": \"관광지ID\",\n");
        sb.append("      \"title\": \"관광지명\",\n");
        sb.append("      \"sequence\": 1,\n");
        sb.append("      \"reason\": \"추천 이유\",\n");
        sb.append("      \"mapx\": 경도값,\n");
        sb.append("      \"mapy\": 위도값\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}");

        return sb.toString();
    }

    /**
     * Gemini API를 호출하고 텍스트 응답을 반환한다.
     */
    private String callGeminiApi(String prompt) {
        String url = endpoint + "?key=" + apiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(Map.of("text", prompt))
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "topP", 0.95,
                        "maxOutputTokens", 8192
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        log.info("Gemini API 호출 시작");

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

        if (response == null) {
            throw new RuntimeException("Gemini API 응답이 없습니다.");
        }

        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            @SuppressWarnings("unchecked")
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            String text = (String) parts.get(0).get("text");

            log.info("Gemini 응답 수신 완료");
            return text;

        } catch (Exception e) {
            log.error("Gemini 응답 구조 파싱 실패: {}", e.getMessage());
            throw new RuntimeException("Gemini API 응답 파싱에 실패했습니다.");
        }
    }

    /**
     * Gemini 텍스트 응답을 RouteRecommendResponse로 파싱한다.
     */
    private RouteRecommendResponse parseGeminiResponse(String jsonText, int duration) {
        try {
            String cleanedJson = jsonText.trim();
            if (cleanedJson.startsWith("```")) {
                cleanedJson = cleanedJson.replaceAll("^```json|```$", "").trim();
            }
        
            JsonNode root = objectMapper.readTree(cleanedJson);

            String aiSummary = root.path("aiSummary").asText();

            List<SpotDto> spots = new ArrayList<>();
            JsonNode spotsNode = root.path("spots");
            for (JsonNode spotNode : spotsNode) {
                SpotDto spot = new SpotDto(
                        spotNode.path("contentId").asText(),
                        spotNode.path("title").asText(),
                        spotNode.path("sequence").asInt(),
                        spotNode.path("reason").asText(),
                        spotNode.path("mapx").asDouble(),
                        spotNode.path("mapy").asDouble()
                );
                spots.add(spot);
            }

            return new RouteRecommendResponse(duration, aiSummary, spots);

        } catch (Exception e) {
            log.error("Gemini JSON 파싱 실패: {}", e.getMessage());
            throw new RuntimeException("AI 응답을 파싱하는 데 실패했습니다.");
        }
    }
}
