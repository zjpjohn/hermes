package pl.allegro.tech.hermes.consumers.consumer.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheLoader;
import org.apache.http.entity.ContentType;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import pl.allegro.tech.hermes.api.OAuthProvider;
import pl.allegro.tech.hermes.api.Subscription;
import pl.allegro.tech.hermes.api.SubscriptionOAuthPolicy;
import pl.allegro.tech.hermes.domain.oauth.OAuthProviderRepository;

import javax.inject.Inject;

import static pl.allegro.tech.hermes.consumers.consumer.oauth.OAuthRequestParams.*;

public class OAuthResourceAccessTokenLoader extends CacheLoader<Subscription, OAuthAccessToken> {

    private final HttpClient httpClient;

    private final OAuthProviderRepository oAuthProviderRepository;

    private final ObjectMapper objectMapper;

    @Inject
    public OAuthResourceAccessTokenLoader(HttpClient httpClient, OAuthProviderRepository oAuthProviderRepository,
                                          ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.oAuthProviderRepository = oAuthProviderRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public OAuthAccessToken load(Subscription subscription) throws Exception {
        SubscriptionOAuthPolicy oAuthPolicy = subscription.getSubscriptionOAuthPolicy();
        String providerName = oAuthPolicy.getProviderName();
        OAuthProvider oAuthProvider = oAuthProviderRepository.getOAuthProviderDetails(providerName);

        ContentResponse response = httpClient.newRequest(oAuthProvider.getTokenEndpoint())
                .method(HttpMethod.POST)
                .header(HttpHeader.KEEP_ALIVE, "true")
                .header(HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString())
                .param(GRANT_TYPE, RESOURCE_OWNER_USERNAME_PASSWORD_GRANT_TYPE_VALUE)
                .param(CLIENT_ID, oAuthProvider.getClientId())
                .param(CLIENT_SECRET, oAuthProvider.getClientSecret())
                .param(USERNAME, oAuthPolicy.getUsername())
                .param(PASSWORD, oAuthPolicy.getPassword())
                .send();
        if (response.getStatus() != HttpStatus.OK_200) {
            throw new OAuthTokenRequestException(subscription.getQualifiedName(), response.getContentAsString(),
                    response.getStatus());
        }
        OAuthAccessTokenResponse accessTokenResponse = objectMapper.readValue(response.getContentAsString(),
                OAuthAccessTokenResponse.class);
        return accessTokenResponse.toAccessToken();
    }
}
