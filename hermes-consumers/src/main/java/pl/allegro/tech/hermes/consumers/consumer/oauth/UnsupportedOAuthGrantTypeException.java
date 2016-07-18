package pl.allegro.tech.hermes.consumers.consumer.oauth;

import pl.allegro.tech.hermes.api.SubscriptionName;
import pl.allegro.tech.hermes.api.SubscriptionOAuthPolicy;

public class UnsupportedOAuthGrantTypeException extends UnsupportedOperationException {

    public UnsupportedOAuthGrantTypeException(SubscriptionName subscriptionName, SubscriptionOAuthPolicy.GrantType grantType) {
        super(String.format("Unsupported grant type %s for subscriptionName %s",
                grantType, subscriptionName.toString()));
    }
}
