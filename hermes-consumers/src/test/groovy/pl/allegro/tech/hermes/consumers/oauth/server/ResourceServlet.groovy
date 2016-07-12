package pl.allegro.tech.hermes.consumers.oauth.server

import io.undertow.util.Headers
import org.apache.oltu.oauth2.common.exception.OAuthProblemException
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest
import org.apache.oltu.oauth2.rs.response.OAuthRSResponse

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ResourceServlet extends HttpServlet {

    private final AuthTestServerStorage storage

    ResourceServlet(AuthTestServerStorage storage) {
        this.storage = storage
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp)
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            def request = new OAuthAccessResourceRequest(req)

            def username = req.getParameter("username")
            validateAccessToken(username, request.accessToken)
            resp.setHeader(Headers.CONTENT_TYPE, "text/plain")
            sendResponse(resp, "this is the secret of " + username, 200)
        } catch (OAuthProblemException e) {
            def response = OAuthRSResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED).buildJSONMessage()
            resp.setHeader(Headers.CONTENT_TYPE, "application/json")
            sendResponse(resp, response.getBody(), response.getResponseStatus())
        }
    }

    private void sendResponse(HttpServletResponse resp, String body, int statusCode) {
        resp.setStatus(statusCode)
        resp.getWriter().write(body)
        resp.getWriter().flush()
        resp.getWriter().close()
    }

    def validateAccessToken(String username, String token) {
        if (!storage.accessTokenExists(username, token)) {
            throw new OAuthProblemException("Invalid username or access token")
        }
    }
}
