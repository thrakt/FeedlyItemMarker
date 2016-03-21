package thrakt;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import thrakt.entity.FeedlyItem;
import thrakt.entity.ProfileResponse;
import thrakt.entity.StreamsContentsResponse;
import thrakt.entity.TokenResponse;

@Component
public class FeedlyService {

    private static final Logger LOG = LoggerFactory
            .getLogger(FeedlyService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${REFRESH_TOKEN}")
    private String refreshToken;

    @Value("${CLIENT_SECRET}")
    private String clientSecret;

    @SuppressWarnings("unchecked")
    public Integer execMarkEntities(
            Function<List<FeedlyItem>, List<FeedlyItem>>... filter) {

        if (StringUtils.isEmpty(refreshToken)) {
            throw new RuntimeException("required enviroment: REFRESH_TOKEN");
        }
        if (StringUtils.isEmpty(clientSecret)) {
            throw new RuntimeException("required enviroment: CLIENT_SECRET");
        }

        LOG.info("get access token.");
        String accessToken = this.getFeedlyAccessToken();

        LOG.info("get user id.");
        String userId = this.getUserId(accessToken);

        LOG.info("get items.");
        List<FeedlyItem> items = this.getItems(userId, accessToken);

        LOG.info("start check items.");
        List<String> markIdList = Stream.of(filter).map(f -> f.apply(items))
                .flatMap(List::stream).map(FeedlyItem::getId).distinct()
                .collect(Collectors.toList());

        LOG.info("mark extra items.");
        this.markAsReadEntities(markIdList, accessToken);

        return markIdList.size();
    }

    String getFeedlyAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> postData = new LinkedMultiValueMap<>(4);
        postData.add("refresh_token", refreshToken);
        postData.add("client_id", "feedly");
        postData.add("client_secret", clientSecret);
        postData.add("grant_type", "refresh_token");

        HttpEntity<?> requestEntity = new HttpEntity<>(postData, headers);

        TokenResponse tokenResponse = restTemplate.exchange(
                "https://cloud.feedly.com/v3/auth/token", HttpMethod.POST,
                requestEntity, TokenResponse.class).getBody();

        return tokenResponse.getAccessToken();
    }

    String getUserId(String accessToken) {
        HttpEntity<?> requestEntity = new HttpEntity<>(
                this.getAuthHttpHeader(accessToken));

        ProfileResponse profileResponse = restTemplate.exchange(
                "https://cloud.feedly.com/v3/profile", HttpMethod.GET,
                requestEntity, ProfileResponse.class).getBody();

        return profileResponse.getId();
    }

    HttpHeaders getAuthHttpHeader(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        return headers;
    }

    List<FeedlyItem> getItems(String userId, String accessToken) {
        HttpEntity<?> requestEntity = new HttpEntity<>(
                this.getAuthHttpHeader(accessToken));

        StreamsContentsResponse contentsResponse = restTemplate.exchange(
                "https://cloud.feedly.com/v3/streams/contents?streamId=user/"
                        + userId + "/category/global.all"
                        + "&count=1000&ranked=oldest&unreadOnly=true",
                HttpMethod.GET, requestEntity, StreamsContentsResponse.class)
                .getBody();

        return contentsResponse.getItems();
    }

    void markAsReadEntities(List<String> idList, String accessToken) {
        if (idList.isEmpty()) {
            return;
        }

        HttpHeaders headers = this.getAuthHttpHeader(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"type\":\"entries\",\"action\":\"markAsRead\",\"entryIds\":["
                + idList.stream().map(i -> "\"" + i + "\"")
                        .collect(Collectors.joining(",")) + "]}";

        HttpEntity<?> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate plainRestTemplate = new RestTemplate();
        plainRestTemplate.exchange("https://cloud.feedly.com/v3/markers",
                HttpMethod.POST, requestEntity, Void.class);
    }

}
