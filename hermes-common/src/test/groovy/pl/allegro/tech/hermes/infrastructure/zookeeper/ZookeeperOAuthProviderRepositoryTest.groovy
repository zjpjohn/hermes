package pl.allegro.tech.hermes.infrastructure.zookeeper

import pl.allegro.tech.hermes.api.OAuthProvider
import pl.allegro.tech.hermes.domain.oauth.OAuthProviderRepository
import pl.allegro.tech.hermes.test.IntegrationTest

class ZookeeperOAuthProviderRepositoryTest extends IntegrationTest {

    private OAuthProviderRepository repository = new ZookeeperOAuthProviderRepository(zookeeper(), mapper, paths)

    def setup() {
        if (zookeeper().checkExists().forPath(paths.oAuthProvidersPath())) {
            zookeeper().delete().deletingChildrenIfNeeded().forPath(paths.oAuthProvidersPath())
        }
    }

    def "should create oauth provider"() {
        def myProvider = new OAuthProvider("myProvider", "http://example.com/token", "client123", "pass123")
        when:
        repository.createOAuthProvider(myProvider)
        wait.untilOAuthProviderCreated(myProvider.name)

        then:
        repository.listOAuthProviderNames().contains(myProvider.name)
    }

    def "should update oauth provider"() {
        def myProvider = new OAuthProvider("myProvider", "http://example.com/token", "client123", "pass123")
        given:
        repository.createOAuthProvider(myProvider)
        wait.untilOAuthProviderCreated(myProvider.name)

        when:
        def updatedProvider = new OAuthProvider("myProvider", "http://example.com/token-updated", "client123", "pass123-updated")
        repository.updateOAuthProvider(updatedProvider)

        then:
        def actualProvider = repository.getOAuthProviderDetails(myProvider.name)
        actualProvider.tokenEndpoint == "http://example.com/token-updated"
        actualProvider.clientSecret == "pass123-updated"
    }

    def "should list all oauth providers"() {
        given:
        def myProvider = new OAuthProvider("myProvider", "http://example.com/token", "client123", "pass123")
        def myOtherProvider = new OAuthProvider("myOtherProvider", "http://example.com/token2", "client1234", "pass1234")

        when:
        repository.createOAuthProvider(myProvider)
        wait.untilOAuthProviderCreated(myProvider.name)
        repository.createOAuthProvider(myOtherProvider)
        wait.untilOAuthProviderCreated(myOtherProvider.name)

        then:
        repository.listOAuthProviderNames().containsAll([myProvider.name, myOtherProvider.name])
        repository.listOAuthProviders().containsAll([myProvider, myOtherProvider])
    }

    def "should remove oauth provider"() {
        given:
        def myProvider = new OAuthProvider("myProvider", "http://example.com/token", "client123", "pass123")
        repository.createOAuthProvider(myProvider)
        wait.untilOAuthProviderCreated(myProvider.name)

        when:
        repository.removeOAuthProvider(myProvider.name)

        then:
        !repository.oAuthProviderExists(myProvider.name)
    }
}
