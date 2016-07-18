package pl.allegro.tech.hermes.consumers.consumer.oauth;

import pl.allegro.tech.hermes.api.SubscriptionName;

public class OAuthTokenRequestException extends RuntimeException {

    public OAuthTokenRequestException(SubscriptionName subscriptionName, String contentAsString, int status) {
        super(String.format("Access token request for %s failed with status %d, message: %s",
                subscriptionName, status, contentAsString));
    }
}
