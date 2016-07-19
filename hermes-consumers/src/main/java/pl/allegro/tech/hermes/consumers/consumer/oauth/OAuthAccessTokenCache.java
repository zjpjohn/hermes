package pl.allegro.tech.hermes.consumers.consumer.oauth;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.allegro.tech.hermes.api.Subscription;
import pl.allegro.tech.hermes.api.SubscriptionOAuthPolicy;

import javax.inject.Inject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static pl.allegro.tech.hermes.api.SubscriptionOAuthPolicy.GrantType.RESOURCE_OWNER_USERNAME_PASSWORD;

public class OAuthAccessTokenCache {

    private static final Logger logger = LoggerFactory.getLogger(OAuthAccessTokenCache.class);

    private final LoadingCache<Subscription, OAuthAccessToken> subscriptionTokenCache;

    private final LoadingCache<String, RateLimiter> rateLimiters;

    @Inject
    public OAuthAccessTokenCache(OAuthResourceAccessTokenLoader OAuthResourceAccessTokenLoader,
                                 OAuthProviderTokenRequestRateLimiterLoader oAuthProviderTokenRequestRateLimiterLoader) {
        this.subscriptionTokenCache = CacheBuilder.newBuilder()
                .maximumSize(1000) // todo configure
                .build(OAuthResourceAccessTokenLoader);
        this.rateLimiters = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(oAuthProviderTokenRequestRateLimiterLoader);
    }

    public OAuthAccessToken getToken(Subscription subscription) {
        SubscriptionOAuthPolicy.GrantType grantType = subscription.getSubscriptionOAuthPolicy().getGrantType();
        if (grantType.equals(RESOURCE_OWNER_USERNAME_PASSWORD)) {
            try {
                initialRateLimiterAcquire(subscription);
                return subscriptionTokenCache.get(subscription);
            } catch (ExecutionException | UncheckedExecutionException e) {
                throw new OAuthTokenLoadingException(subscription.getQualifiedName(), e.getCause());
            }
        }
        throw new UnsupportedOAuthGrantTypeException(subscription.getQualifiedName(), grantType);
    }

    private void initialRateLimiterAcquire(Subscription subscription) throws ExecutionException {
        if (subscriptionTokenCache.getIfPresent(subscription) == null) {
            String providerName = subscription.getSubscriptionOAuthPolicy().getProviderName();
            rateLimiters.get(providerName).tryAcquire();
        }
    }

    public void invalidateToken(Subscription subscription) {
        String providerName = subscription.getSubscriptionOAuthPolicy().getProviderName();
        try {
            if (rateLimiters.get(providerName).tryAcquire()) {
                logger.info("Invalidating token of OAuth provider {}", providerName);
                subscriptionTokenCache.invalidate(subscription);
            }
        } catch (ExecutionException | UncheckedExecutionException e) {
            throw new OAuthProviderTokenRequestRateLimiterLoadingException(
                    subscription.getQualifiedName().toString(), providerName, e);
        }

    }
}
