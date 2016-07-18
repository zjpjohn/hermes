package pl.allegro.tech.hermes.consumers.consumer.oauth;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import pl.allegro.tech.hermes.api.Subscription;
import pl.allegro.tech.hermes.api.SubscriptionOAuthPolicy;

import javax.inject.Inject;
import java.util.concurrent.ExecutionException;

import static pl.allegro.tech.hermes.api.SubscriptionOAuthPolicy.GrantType.RESOURCE_OWNER_USERNAME_PASSWORD;

public class OAuthAccessTokenCache {

    private final LoadingCache<Subscription, OAuthAccessToken> subscriptionTokenCache;

    @Inject
    public OAuthAccessTokenCache(OAuthResourceAccessTokenLoader OAuthResourceAccessTokenLoader) {
        this.subscriptionTokenCache = CacheBuilder.newBuilder()
                .maximumSize(1000) // todo configure
                .build(OAuthResourceAccessTokenLoader);
    }

    public OAuthAccessToken getToken(Subscription subscription) {
        SubscriptionOAuthPolicy.GrantType grantType = subscription.getSubscriptionOAuthPolicy().getGrantType();
        if (grantType.equals(RESOURCE_OWNER_USERNAME_PASSWORD)) {
            try {
                return subscriptionTokenCache.get(subscription);
            } catch (ExecutionException | UncheckedExecutionException e) {
                throw new OAuthTokenLoadingException(subscription.getQualifiedName(), e.getCause());
            }
        }
        throw new UnsupportedOAuthGrantTypeException(subscription.getQualifiedName(), grantType);
    }
}
