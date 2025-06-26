import org.springframework.core.io.AiModelClientsService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@Service
public class AiModelClientService {

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> predictPersonalColor(String imagePath) {
        String url = "http://localhost:5000/predict"; // Flask 서버 주소

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new AiModelClientsService(imagePath));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("AI 서버 예측 실패: " + response.getStatusCode());
        }
    }
}
