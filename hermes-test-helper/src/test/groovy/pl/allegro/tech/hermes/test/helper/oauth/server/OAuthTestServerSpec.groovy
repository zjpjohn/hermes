package pl.allegro.tech.hermes.test.helper.oauth.server

import org.apache.oltu.oauth2.client.OAuthClient
import org.apache.oltu.oauth2.client.URLConnectionClient
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest
import org.apache.oltu.oauth2.client.request.OAuthClientRequest
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse
import org.apache.oltu.oauth2.common.exception.OAuthProblemException
import org.apache.oltu.oauth2.common.message.types.GrantType
import spock.lang.Shared
import spock.lang.Specification

class OAuthTestServerSpec extends Specification {

    @Shared
    OAuthTestServer authServer

    @Shared
    OAuthClient authClient

    def setupSpec() {
        authClient = new OAuthClient(new URLConnectionClient())
        authServer = new OAuthTestServer()
        authServer.start()
    }

    def cleanupSpec() {
        authServer.stop()
        authClient.shutdown()
    }

    def setup() {
        authServer.clearStorage()
    }

    def "should not obtain access token when client is not registered"() {
        given:
        authServer.registerResourceOwner("hermes", "hermes123")

        when:
        def request = OAuthClientRequest.tokenLocation(authServer.getTokenEndpoint())
                .setGrantType(GrantType.PASSWORD)
                .setUsername("hermes")
                .setPassword("hermes123")
                .setClientId("unknown_client")
                .setClientSecret("abc")
                .buildQueryMessage()
        authClient.accessToken(request)

        then:
        OAuthProblemException exception = thrown()
        exception.responseStatus == 400
    }

    def "should not obtain access token when providing invalid password"() {
        given:
        authServer.registerClient("test_client", "abc")
        authServer.registerResourceOwner("hermes", "hermes123")

        when:
        def request = OAuthClientRequest.tokenLocation(authServer.getTokenEndpoint())
                .setGrantType(GrantType.PASSWORD)
                .setUsername("hermes")
                .setPassword("INVALID_PASSWORD")
                .setClientId("test_client")
                .setClientSecret("abc")
                .buildQueryMessage()
        authClient.accessToken(request)

        then:
        OAuthProblemException exception = thrown()
        exception.responseStatus == 400
    }

    def "should obtain access token"() {
        given:
        authServer.registerClient("test_client", "abc")
        authServer.registerResourceOwner("hermes", "hermes123")

        when:
        def request = OAuthClientRequest.tokenLocation(authServer.getTokenEndpoint())
                .setGrantType(GrantType.PASSWORD)
                .setUsername("hermes")
                .setPassword("hermes123")
                .setClientId("test_client")
                .setClientSecret("abc")
                .buildQueryMessage()
        def response = authClient.accessToken(request)

        then:
        response.accessToken
    }

    def "should obtain access token and get secured resource"() {
        given:
        authServer.registerClient("test_client", "abc")
        def username = 'hermes'
        authServer.registerResourceOwner(username, "hermes123")

        when:
        def tokenRequest = OAuthClientRequest.tokenLocation(authServer.getTokenEndpoint())
                .setGrantType(GrantType.PASSWORD)
                .setUsername(username)
                .setPassword("hermes123")
                .setClientId("test_client")
                .setClientSecret("abc")
                .buildQueryMessage()
        OAuthJSONAccessTokenResponse tokenResponse = authClient.accessToken(tokenRequest)

        then:
        tokenResponse.accessToken

        when:
        def resourceRequest = new OAuthBearerClientRequest(authServer.getResourceEndpoint(username))
                .setAccessToken(tokenResponse.accessToken)
                .buildHeaderMessage()
        OAuthResourceResponse resource = authClient.resource(resourceRequest, "POST", OAuthResourceResponse.class)

        then:
        resource.responseCode == 200
        resource.body == "this is the secret of $username"
    }

    def "should access resource"() {
        given:
        authServer.registerClient("test_client", "abc")
        def username = 'hermes'
        authServer.registerResourceOwner(username, "hermes123")
        def token = authServer.issueAccessToken(username)

        when:
        def resourceRequest = new OAuthBearerClientRequest(authServer.getResourceEndpoint(username))
                .setAccessToken(token)
                .buildHeaderMessage()
        OAuthResourceResponse resource = authClient.resource(resourceRequest, "POST", OAuthResourceResponse.class)

        then:
        resource.responseCode == 200
        resource.body == "this is the secret of $username"
    }

    def "should not access resource without valid token"() {
        given:
        authServer.registerClient("test_client", "abc")
        def username = 'hermes'
        authServer.registerResourceOwner(username, "hermes123")
        authServer.issueAccessToken(username)

        when:
        def resourceRequest = new OAuthBearerClientRequest(authServer.getResourceEndpoint(username))
                .setAccessToken("invalid-token")
                .buildHeaderMessage()
        OAuthResourceResponse response = authClient.resource(resourceRequest, "POST", OAuthResourceResponse.class)

        then:
        response.responseCode == 401
    }

    def "should count resource access count"() {
        given:
        authServer.registerClient("test_client", "abc")
        authServer.registerResourceOwner("hermes", "hermes123")
        def token = authServer.issueAccessToken("hermes")

        when:
        def resourceRequest = new OAuthBearerClientRequest(authServer.getResourceEndpoint("hermes"))
                .setAccessToken(token)
                .buildHeaderMessage()
        authClient.resource(resourceRequest, "POST", OAuthResourceResponse.class)
        authClient.resource(resourceRequest, "POST", OAuthResourceResponse.class)

        then:
        authServer.getResourceAccessCount("hermes") == 2
    }
}
