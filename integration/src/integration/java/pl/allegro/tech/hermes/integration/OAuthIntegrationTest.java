package pl.allegro.tech.hermes.integration;

import com.jayway.awaitility.Duration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pl.allegro.tech.hermes.api.OAuthProvider;
import pl.allegro.tech.hermes.api.Subscription;
import pl.allegro.tech.hermes.api.SubscriptionOAuthPolicy;
import pl.allegro.tech.hermes.api.Topic;
import pl.allegro.tech.hermes.test.helper.builder.SubscriptionBuilder;
import pl.allegro.tech.hermes.test.helper.oauth.server.OAuthTestServer;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static javax.ws.rs.core.Response.Status.CREATED;
import static pl.allegro.tech.hermes.api.SubscriptionOAuthPolicy.GrantType.RESOURCE_OWNER_USERNAME_PASSWORD;
import static pl.allegro.tech.hermes.integration.test.HermesAssertions.assertThat;

public class OAuthIntegrationTest extends IntegrationTest {

    private OAuthTestServer oAuthTestServer;

    SubscriptionOAuthPolicy resourceOwnersCredentialsPolicy = new SubscriptionOAuthPolicy(RESOURCE_OWNER_USERNAME_PASSWORD,
            "provider1", "scope", "testUser1", "password1");

    @BeforeClass
    public void initialize() throws IOException {
        oAuthTestServer = new OAuthTestServer();
        oAuthTestServer.start();
        oAuthTestServer.registerClient("client1", "secret1");
        oAuthTestServer.registerResourceOwner("testUser1", "password1");

        operations.createOAuthProvider(new OAuthProvider("provider1", oAuthTestServer.getTokenEndpoint(),
                "client1", "secret1", 2));
    }

    @AfterClass
    public void tearDown() {
        oAuthTestServer.stop();
    }

    @BeforeMethod
    public void initializeAlways() {
        oAuthTestServer.revokeAllTokens();
        oAuthTestServer.clearResourceAccessCounters();
        oAuthTestServer.clearTokenIssueCounters();
    }

    @Test
    public void shouldPublishAndSendMessageToOAuthSecuredEndpoint() {
        // given
        Topic topic = operations.buildTopic("publishAndConsumeOAuthGroup", "topic");
        Subscription subscription = SubscriptionBuilder.subscription(topic, "subscription")
                .withEndpoint(oAuthTestServer.getResourceEndpoint("testUser1"))
                .withSubscriptionOAuthPolicy(resourceOwnersCredentialsPolicy).build();
        operations.createSubscription(topic, subscription);

        // when
        Response response = publisher.publish(topic.getQualifiedName(), "hello world");
        assertThat(response).hasStatus(CREATED);

        // then
        wait.awaitAtMost(Duration.TEN_SECONDS).until(() -> oAuthTestServer.getResourceAccessCount("testUser1") == 1);
    }

    @Test
    public void shouldInvalidateRevokedTokenAndSendMessageUsingFreshOne() {
        // given
        Topic topic = operations.buildTopic("publishAndConsumeOAuthGroup2", "topic");
        Subscription subscription = SubscriptionBuilder.subscription(topic, "subscription")
                .withEndpoint(oAuthTestServer.getResourceEndpoint("testUser1"))
                .withSubscriptionOAuthPolicy(resourceOwnersCredentialsPolicy).build();
        operations.createSubscription(topic, subscription);

        // when
        Response response = publisher.publish(topic.getQualifiedName(), "hello world");
        assertThat(response).hasStatus(CREATED);

        // then
        wait.awaitAtMost(Duration.TEN_SECONDS).until(() -> oAuthTestServer.getResourceAccessCount("testUser1") == 1);

        // and when
        oAuthTestServer.revokeAllTokens();
        response = publisher.publish(topic.getQualifiedName(), "hello again");
        assertThat(response).hasStatus(CREATED);

        // then
        wait.awaitAtMost(Duration.TEN_SECONDS).until(() -> oAuthTestServer.getResourceAccessCount("testUser1") == 2);
    }

    @Test
    public void shouldNotRequestAccessTokenMoreFrequentlyThanConfiguredForOAuthProvider() {
        // given
        Topic topic = operations.buildTopic("publishAndConsumeOAuthGroup3", "topic");
        Subscription subscription = SubscriptionBuilder.subscription(topic, "subscription")
                .withEndpoint(oAuthTestServer.getResourceEndpoint("testUser1"))
                .withSubscriptionOAuthPolicy(resourceOwnersCredentialsPolicy).build();
        operations.createSubscription(topic, subscription);

        // when
        Response response = publisher.publish(topic.getQualifiedName(), "hello world");
        assertThat(response).hasStatus(CREATED);

        // then
        wait.awaitAtMost(Duration.TEN_SECONDS).until(() -> oAuthTestServer.getResourceAccessCount("testUser1") == 1);

        // and when
        oAuthTestServer.revokeAllTokens();
        for (int i = 0; i < 20; i++) {
            publisher.publish(topic.getQualifiedName(), "hello again");
        }
        wait.awaitAtMost(Duration.TEN_SECONDS).until(() -> oAuthTestServer.getResourceAccessCount("testUser1") > 1);
        oAuthTestServer.revokeAllTokens();
        for (int i = 0; i < 20; i++) {
            publisher.publish(topic.getQualifiedName(), "hello again");
        }

        // then
        wait.awaitAtMost(Duration.TEN_SECONDS).until(() -> oAuthTestServer.getResourceAccessCount("testUser1") == 41);
        assertThat(oAuthTestServer.getTokenIssueCount("testUser1")).isEqualTo(3);
    }
}
