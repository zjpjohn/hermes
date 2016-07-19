package pl.allegro.tech.hermes.test.helper.oauth.server;

import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.issuer.UUIDValueGenerator;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class OAuthTestServerStorage {

    private static final Logger logger = LoggerFactory.getLogger(OAuthTestServerStorage.class);

    private final OAuthIssuer issuer;
    private final ConcurrentHashMap<String, String> clients;
    private final ConcurrentHashMap<String, String> owners;
    private final ConcurrentHashMap<String, List<String>> tokens;
    private final ConcurrentHashMap<String, AtomicInteger> accessCount;
    private final ConcurrentHashMap<String, AtomicInteger> tokenIssueCount;

    OAuthTestServerStorage() {
        issuer = new OAuthIssuerImpl(new UUIDValueGenerator());
        clients = new ConcurrentHashMap<>();
        owners = new ConcurrentHashMap<>();
        tokens = new ConcurrentHashMap<>();
        accessCount = new ConcurrentHashMap<>();
        tokenIssueCount = new ConcurrentHashMap<>();
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
            int i = incrementTokenIssueCount(username);
            logger.info("Token {} issued for user {}, count {}", token, username, i);
            return token;
        } catch (OAuthSystemException e) {
            throw new RuntimeException(e);
        }
    }

    private int incrementTokenIssueCount(String username) {
        if (tokenIssueCount.containsKey(username)) {
            return tokenIssueCount.get(username).incrementAndGet();
        } else {
            tokenIssueCount.putIfAbsent(username, new AtomicInteger(1));
            return 1;
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

    void clearAll() {
        clearClients();
        clearOwners();
        clearTokens();
        clearAccessCounters();
        clearTokenIssueCounters();
    }

    void clearClients() {
        clients.clear();
    }

    void clearOwners() {
        owners.clear();
    }

    void clearTokens() {
        tokens.clear();
    }

    void clearAccessCounters() {
        accessCount.clear();
    }

    void clearTokenIssueCounters() {
        tokenIssueCount.clear();
    }

    public void incrementResourceAccessCount(String username) {
        if (accessCount.containsKey(username)) {
            accessCount.get(username).incrementAndGet();
        } else {
            accessCount.putIfAbsent(username, new AtomicInteger(1));
        }
    }

    public int getResourceAccessCount(String username) {
        return accessCount.getOrDefault(username, new AtomicInteger(0)).get();
    }

    public int getTokenIssueCount(String username) {
        return tokenIssueCount.getOrDefault(username, new AtomicInteger(0)).get();
    }
}
