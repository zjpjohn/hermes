package pl.allegro.tech.hermes.consumers.consumer.oauth;

import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.RateLimiter;
import pl.allegro.tech.hermes.api.OAuthProvider;
import pl.allegro.tech.hermes.domain.oauth.OAuthProviderRepository;

import javax.inject.Inject;

public class OAuthProviderTokenRequestRateLimiterLoader extends CacheLoader<String, RateLimiter> {

    private final OAuthProviderRepository oAuthProviderRepository;

    @Inject
    public OAuthProviderTokenRequestRateLimiterLoader(OAuthProviderRepository oAuthProviderRepository) {
        this.oAuthProviderRepository = oAuthProviderRepository;
    }

    @Override
    public RateLimiter load(String oAuthProviderName) throws Exception {
        OAuthProvider oAuthProvider = oAuthProviderRepository.getOAuthProviderDetails(oAuthProviderName);
        return RateLimiter.create(1.0 / oAuthProvider.getTokenRequestDelay());
    }
}
