package netzbegruenung.keycloak.authenticator.gateway;

import org.jboss.logging.Logger;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Map;

/**
 * SmsService implementation using Twilio REST API.
 */
public class TwilioSmsService implements SmsService {

    private static final Logger logger = Logger.getLogger(TwilioSmsService.class);

    private final String accountSid;
    private final String authToken;
    private final String from;

    public TwilioSmsService(Map<String, String> config) {
        this.accountSid = config.get("twilioAccountSid");
        this.authToken = config.get("twilioAuthToken");
        this.from = config.get("twilioFrom");
    }

    @Override
    public void send(String phoneNumber, String message) {
        try {
            String body = String.format("To=%s&From=%s&Body=%s",
                    URLEncoder.encode(phoneNumber, Charset.defaultCharset()),
                    URLEncoder.encode(from, Charset.defaultCharset()),
                    URLEncoder.encode(message, Charset.defaultCharset()));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json", accountSid)))
                    .header("Authorization", basicAuth(accountSid, authToken))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                logger.infof("Sent SMS via Twilio to %s", phoneNumber);
            } else {
                logger.errorf("Twilio error %s while sending SMS to %s: %s", response.statusCode(), phoneNumber, response.body());
            }
        } catch (Exception e) {
            logger.errorf(e, "Failed to send Twilio SMS to %s", phoneNumber);
        }
    }

    private static String basicAuth(String user, String pass) {
        String value = user + ":" + pass;
        return "Basic " + Base64.getEncoder().encodeToString(value.getBytes());
    }
}
