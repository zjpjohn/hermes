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
import pl.allegro.tech.hermes.integration.env.SharedServices;
import pl.allegro.tech.hermes.integration.helper.Assertions;
import pl.allegro.tech.hermes.test.helper.builder.SubscriptionBuilder;
import pl.allegro.tech.hermes.test.helper.endpoint.RemoteServiceEndpoint;
import pl.allegro.tech.hermes.test.helper.oauth.server.OAuthTestServer;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static javax.ws.rs.core.Response.Status.CREATED;
import static pl.allegro.tech.hermes.api.SubscriptionOAuthPolicy.GrantType.RESOURCE_OWNER_USERNAME_PASSWORD;
import static pl.allegro.tech.hermes.integration.test.HermesAssertions.assertThat;

public class OAuthIntegrationTest extends IntegrationTest {

    private RemoteServiceEndpoint remoteService;

    private Assertions assertions;

    private OAuthTestServer oAuthTestServer;

    @BeforeClass
    public void initialize() throws IOException {
        assertions = new Assertions(SharedServices.services().zookeeper());
        oAuthTestServer = new OAuthTestServer();
        oAuthTestServer.start();
    }

    @AfterClass
    public void tearDown() {
        oAuthTestServer.stop();
    }

    @BeforeMethod
    public void initializeAlways() {
        oAuthTestServer.clearStorage();
    }

    @Test
    public void shouldPublishAndConsumeMessage() {
        // given
        oAuthTestServer.registerClient("client1", "secret1");
        oAuthTestServer.registerResourceOwner("testUser1", "password1");
        Topic topic = operations.buildTopic("publishAndConsumeOAuthGroup", "topic");
        operations.createOAuthProvider(new OAuthProvider("provider1", oAuthTestServer.getTokenEndpoint(), "client1", "secret1"));
        SubscriptionOAuthPolicy subscriptionOAuthPolicy = new SubscriptionOAuthPolicy(RESOURCE_OWNER_USERNAME_PASSWORD, "provider1", "scope", "testUser1", "password1");
        Subscription subscription = SubscriptionBuilder.subscription(topic, "subscription")
                .withEndpoint(oAuthTestServer.getResourceEndpoint("testUser1"))
                .withSubscriptionOAuthPolicy(subscriptionOAuthPolicy).build();
        operations.createSubscription(topic, subscription);

        // when
        Response response = publisher.publish(topic.getQualifiedName(), "hello world");

        // then
        assertThat(response).hasStatus(CREATED);
        wait.awaitAtMost(Duration.TEN_SECONDS).until(() -> oAuthTestServer.getResourceAccessCount("testUser1") == 1);
    }
}
