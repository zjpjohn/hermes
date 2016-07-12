package pl.allegro.tech.hermes.consumers.oauth.server

import io.undertow.util.Headers
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest
import org.apache.oltu.oauth2.as.response.OAuthASResponse
import org.apache.oltu.oauth2.common.exception.OAuthProblemException
import org.apache.oltu.oauth2.common.message.OAuthResponse
import org.apache.oltu.oauth2.common.message.types.TokenType

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AccessTokenServlet extends HttpServlet {

    private final AuthTestServerStorage storage

    AccessTokenServlet(AuthTestServerStorage storage) {
        this.storage = storage
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            OAuthTokenRequest request = new OAuthTokenRequest(req)
            validateClientCredentials(request)
            validateResourceOwnerCredentials(request)

            def token = storage.issueToken(request.username)
            def response = OAuthASResponse.tokenResponse(200)
                    .setAccessToken(token)
                    .setTokenType(TokenType.BEARER.toString())
                    .buildJSONMessage()
            sendResponse(resp, response)

        } catch (OAuthProblemException e) {
            def response = OAuthASResponse.errorResponse(400)
                    .error(e)
                    .buildJSONMessage()
            sendResponse(resp, response)
        }
    }

    private void sendResponse(HttpServletResponse resp, OAuthResponse response) {
        resp.addHeader(Headers.CONTENT_TYPE, "application/json")
        resp.setStatus(response.getResponseStatus())
        resp.getWriter().write(response.getBody())
        resp.getWriter().flush()
        resp.getWriter().close()
    }

    void validateClientCredentials(OAuthTokenRequest request) {
        if (!storage.clientExists(request.clientId, request.clientSecret)) {
            throw new OAuthProblemException(String.format("No client with name %s registered or invalid client secret provided", request.clientId))
        }
    }

    void validateResourceOwnerCredentials(OAuthTokenRequest request) {
        if (!storage.resourceOwnerExists(request.username, request.password)) {
            throw new OAuthProblemException(String.format("No resource owner with name %s registered or invalid password provided", request.username))
        }
    }
}