package pl.allegro.tech.hermes.consumers.oauth.server

import org.apache.oltu.oauth2.as.issuer.OAuthIssuer
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl
import org.apache.oltu.oauth2.as.issuer.UUIDValueGenerator

import java.util.concurrent.ConcurrentHashMap

class AuthTestServerStorage {

    private final OAuthIssuer issuer
    private final ConcurrentHashMap<String, String> clients
    private final ConcurrentHashMap<String, String> owners
    private final ConcurrentHashMap<String, List<String>> tokens

    AuthTestServerStorage() {
        issuer = new OAuthIssuerImpl(new UUIDValueGenerator())
        clients = new ConcurrentHashMap<>()
        owners = new ConcurrentHashMap<>()
        tokens = new ConcurrentHashMap<>()
    }

    void addClient(String clientId, String clientSecret) {
        clients.put(clientId, clientSecret)
    }

    void addResourceOwner(String username, String password) {
        owners.put(username, password)
    }

    boolean clientExists(String clientId, String clientSecret) {
        return clients.get(clientId) == clientSecret
    }

    boolean resourceOwnerExists(String username, String password) {
        return owners.get(username) == password
    }

    String issueToken(String username) {
        def token = issuer.accessToken()
        addAccessToken(username, token)
        return token
    }

    private addAccessToken(String username, String token) {
        if (tokens.containsKey(username)) {
            tokens.get(username).add(token)
        } else {
            tokens.put(username, [token])
        }
    }

    boolean accessTokenExists(String username, String token) {
        if (username == null || !tokens.containsKey(username)) {
            return false
        }
        return tokens.get(username).contains(token)
    }

    void clear() {
        clients.clear()
        owners.clear()
        tokens.clear()
    }
}
