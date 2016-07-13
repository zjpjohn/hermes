package pl.allegro.tech.hermes.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import pl.allegro.tech.hermes.api.helpers.Patch;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.allegro.tech.hermes.api.PatchData.patchData;
import static pl.allegro.tech.hermes.api.SubscriptionOAuthPolicy.GrantType.CLIENT_CREDENTIALS;
import static pl.allegro.tech.hermes.api.SubscriptionOAuthPolicy.GrantType.RESOURCE_OWNER_USERNAME_PASSWORD;
import static pl.allegro.tech.hermes.api.SubscriptionPolicy.Builder.subscriptionPolicy;
import static pl.allegro.tech.hermes.test.helper.builder.SubscriptionBuilder.subscription;

public class SubscriptionTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldDeserializeSubscription() throws Exception {
        // given
        String json = "{" +
                "\"name\": \"test\", " +
                "\"topicName\": \"g1.t1\", " +
                "\"endpoint\": \"http://localhost:8888\"" +
                "}";

        // when
        Subscription subscription = mapper.readValue(json, Subscription.class);

        // then
        assertThat(subscription.getName()).isEqualTo("test");
        assertThat(subscription.getEndpoint().getEndpoint()).isEqualTo("http://localhost:8888");
    }

    @Test
    public void shouldDeserializeSubscriptionWithoutTopicName() throws Exception {
        // given
        String json = "{\"name\": \"test\", \"endpoint\": \"http://localhost:8888\"}";

        // when
        Subscription subscription = mapper.readValue(json, Subscription.class);

        // then
        assertThat(subscription.getName()).isEqualTo("test");
        assertThat(subscription.getEndpoint().getEndpoint()).isEqualTo("http://localhost:8888");
    }

    @Test
    public void shouldDeserializeSubscriptionWithoutBackoff() throws Exception {
        // given
        String json = "{\"name\": \"test\", \"endpoint\": \"http://localhost:8888\", \"subscriptionPolicy\": {\"messageTtl\": 100}}";

        // when
        Subscription subscription = mapper.readValue(json, Subscription.class);

        // then
        assertThat(subscription.getSerialSubscriptionPolicy().getMessageBackoff()).isEqualTo(100);
    }

    @Test
    public void shouldApplyPatchToSubscriptionPolicy() {
        //given
        PatchData patch = patchData().set("rate", 8).build();

        //when
        SubscriptionPolicy subscription = subscriptionPolicy()
                .withRate(1)
                .applyPatch(patch).build();

        //then
        assertThat(subscription.getRate()).isEqualTo(8);
    }

    @Test
    public void shouldAnonymizePassword() {
        // given
        Subscription subscription = subscription("group.topic", "subscription").withEndpoint("http://user:password@service/path").build();

        // when & then
        assertThat(subscription.anonymizePassword().getEndpoint()).isEqualTo(new EndpointAddress("http://user:*****@service/path"));
    }

    @Test
    public void shouldApplyPatchChangingSubscriptionOAuthPolicyGrantType() {
        // given
        Subscription subscription = subscription("group.topic", "subscription")
                .withSubscriptionOAuthPolicy(new SubscriptionOAuthPolicy(CLIENT_CREDENTIALS, "myProvider", "repo", null, null))
                .build();
        PatchData subscriptionOAuthPolicyPatchData = patchData()
                .set("grantType", "RESOURCE_OWNER_USERNAME_PASSWORD")
                .set("username", "user1")
                .set("password", "abc123")
                .build();
        PatchData patch = patchData()
                .set("subscriptionOAuthPolicy", subscriptionOAuthPolicyPatchData)
                .build();

        // when
        Subscription updated = Patch.apply(subscription, patch);

        // then
        SubscriptionOAuthPolicy updatedPolicy = updated.getSubscriptionOAuthPolicy();
        assertThat(updatedPolicy.getGrantType()).isEqualTo(RESOURCE_OWNER_USERNAME_PASSWORD);
        assertThat(updatedPolicy.getUsername()).isEqualTo("user1");
    }
}
