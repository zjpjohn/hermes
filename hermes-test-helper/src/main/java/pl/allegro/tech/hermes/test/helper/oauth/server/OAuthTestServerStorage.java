package pl.allegro.tech.hermes.test.helper.oauth.server;

import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.issuer.UUIDValueGenerator;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

class OAuthTestServerStorage {

    private final OAuthIssuer issuer;
    private final ConcurrentHashMap<String, String> clients;
    private final ConcurrentHashMap<String, String> owners;
    private final ConcurrentHashMap<String, List<String>> tokens;

    OAuthTestServerStorage() {
        issuer = new OAuthIssuerImpl(new UUIDValueGenerator());
        clients = new ConcurrentHashMap<>();
        owners = new ConcurrentHashMap<>();
        tokens = new ConcurrentHashMap<>();
    }

    void addClient(String clientId, String clientSecret) {
        clients.put(clientId, clientSecret);
    }

    void addResourceOwner(String username, String password) {
        owners.put(username, password);
    }

    boolean clientExists(String clientId, String clientSecret) {
        return clients.containsKey(clientId) && clients.get(clientId).equals(clientSecret);
    }

    boolean resourceOwnerExists(String username, String password) {
        return owners.containsKey(username) && owners.get(username).equals(password);
    }

    String issueToken(String username) {
        try {
            String token = issuer.accessToken();
            addAccessToken(username, token);
            return token;
        } catch (OAuthSystemException e) {
            throw new RuntimeException(e);
        }
    }

    private void addAccessToken(String username, String token) {
        if (tokens.containsKey(username)) {
            tokens.get(username).add(token);
        } else {
            List<String> userTokens = new ArrayList<>();
            userTokens.add(token);
            tokens.put(username, userTokens);
        }
    }

    boolean accessTokenExists(String username, String token) {
        if (username == null || !tokens.containsKey(username)) {
            return false;
        }
        return tokens.get(username).contains(token);
    }

    void clear() {
        clients.clear();
        owners.clear();
        tokens.clear();
    }
}
