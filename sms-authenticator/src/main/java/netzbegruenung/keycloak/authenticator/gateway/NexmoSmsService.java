package netzbegruenung.keycloak.authenticator.gateway;

import org.jboss.logging.Logger;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * SmsService implementation using Vonage (formerly Nexmo) API.
 */
public class NexmoSmsService implements SmsService {

    private static final Logger logger = Logger.getLogger(NexmoSmsService.class);

    private final String apiKey;
    private final String apiSecret;
    private final String from;

    public NexmoSmsService(Map<String, String> config) {
        this.apiKey = config.get("nexmoApiKey");
        this.apiSecret = config.get("nexmoApiSecret");
        this.from = config.get("nexmoFrom");
    }

    @Override
    public void send(String phoneNumber, String message) {
        try {
            String body = String.format("api_key=%s&api_secret=%s&to=%s&from=%s&text=%s",
                    URLEncoder.encode(apiKey, Charset.defaultCharset()),
                    URLEncoder.encode(apiSecret, Charset.defaultCharset()),
                    URLEncoder.encode(phoneNumber, Charset.defaultCharset()),
                    URLEncoder.encode(from, Charset.defaultCharset()),
                    URLEncoder.encode(message, Charset.defaultCharset()));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://rest.nexmo.com/sms/json"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                logger.infof("Sent SMS via Nexmo to %s", phoneNumber);
            } else {
                logger.errorf("Nexmo error %s while sending SMS to %s: %s", response.statusCode(), phoneNumber, response.body());
            }
        } catch (Exception e) {
            logger.errorf(e, "Failed to send Nexmo SMS to %s", phoneNumber);
        }
    }
}
