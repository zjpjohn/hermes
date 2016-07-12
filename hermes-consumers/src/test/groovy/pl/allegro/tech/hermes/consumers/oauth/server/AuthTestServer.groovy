package pl.allegro.tech.hermes.consumers.oauth.server

import io.undertow.Handlers
import io.undertow.Undertow
import io.undertow.servlet.Servlets
import io.undertow.servlet.util.ImmediateInstanceFactory
import pl.allegro.tech.hermes.test.helper.util.Ports

import javax.servlet.Servlet

class AuthTestServer {

    public static final OAUTH2_TOKEN_ENDPOINT = '/oauth2/token'
    public static final OAUTH2_RESOURCE_ENDPOINT = '/oauth2/resource'

    private final AuthTestServerStorage storage = new AuthTestServerStorage()
    private final Undertow undertow
    private final port

    AuthTestServer() {
        port = Ports.nextAvailable()
        def tokenServlet = new AccessTokenServlet(storage)
        def resourceServlet = new ResourceServlet(storage)

        def deploymentInfo = Servlets.deployment()
                .setClassLoader(AuthTestServer.class.getClassLoader())
                .setContextPath("/")
                .setDeploymentName("OAuthTestServer")
                .addServlets(
                    Servlets.servlet("AuthTokenServlet", AccessTokenServlet.class, new ImmediateInstanceFactory<Servlet>(tokenServlet))
                            .addMapping(OAUTH2_TOKEN_ENDPOINT),
                    Servlets.servlet("ResourceServlet", ResourceServlet.class, new ImmediateInstanceFactory<Servlet>(resourceServlet))
                            .addMapping(OAUTH2_RESOURCE_ENDPOINT)
                )

        def deploymentManager = Servlets.defaultContainer().addDeployment(deploymentInfo)
        deploymentManager.deploy()
        def servletHandler = deploymentManager.start()

        def path = Handlers.path().addPrefixPath("/", servletHandler)
        this.undertow = Undertow.builder()
            .addHttpListener(port, "localhost")
            .setHandler(path)
            .build()
    }

    String tokenEndpoint() {
        return "http://localhost:$port$OAUTH2_TOKEN_ENDPOINT"
    }

    String resourceEndpoint(String username) {
        return "http://localhost:$port$OAUTH2_RESOURCE_ENDPOINT?username=$username"
    }

    def registerClient(String clientId, String clientSecret) {
        storage.addClient(clientId, clientSecret)
    }

    def registerResourceOwner(String username, String password) {
        storage.addResourceOwner(username, password)
    }

    String issueAccessToken(String username) {
        return storage.issueToken(username)
    }

    def clearStorage() {
        storage.clear()
    }

    def start() {
        undertow.start()
    }

    def stop() {
        undertow.stop()
    }
}
