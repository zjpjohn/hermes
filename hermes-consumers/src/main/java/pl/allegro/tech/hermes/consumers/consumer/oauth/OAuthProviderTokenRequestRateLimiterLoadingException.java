package pl.allegro.tech.hermes.consumers.consumer.oauth;

public class OAuthProviderTokenRequestRateLimiterLoadingException extends RuntimeException {

    public OAuthProviderTokenRequestRateLimiterLoadingException(String subscriptionName,
                                                                String oAuthProviderName,
                                                                Throwable cause) {
        super(String.format("Could not load oAuthProvider %s rate limiter for subscription %s",
                oAuthProviderName, subscriptionName), cause);
    }
}
