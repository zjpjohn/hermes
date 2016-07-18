package pl.allegro.tech.hermes.consumers.consumer.oauth;

import pl.allegro.tech.hermes.api.SubscriptionName;

public class OAuthTokenLoadingException extends RuntimeException {

    public OAuthTokenLoadingException(SubscriptionName subscriptionName, Throwable cause) {
        super(String.format("Could not load access token for subscription %s", subscriptionName.toString()), cause);
    }
}
