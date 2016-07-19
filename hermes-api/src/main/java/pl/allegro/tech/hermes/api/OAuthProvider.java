package pl.allegro.tech.hermes.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.Objects;

import static pl.allegro.tech.hermes.api.constraints.Names.ALLOWED_NAME_REGEX;

public class OAuthProvider {

    private static final String ANONYMIZED_CLIENT_SECRET = "******";

    @NotEmpty
    @Pattern(regexp = ALLOWED_NAME_REGEX)
    private String name;

    @NotEmpty
    private String tokenEndpoint;

    @NotEmpty
    private String clientId;

    @NotEmpty
    private String clientSecret;

    @Min(1)
    private int tokenRequestDelay;

    @JsonCreator
    public OAuthProvider(@JsonProperty("name") String name,
                         @JsonProperty("tokenEndpoint") String tokenEndpoint,
                         @JsonProperty("clientId") String clientId,
                         @JsonProperty("clientSecret") String clientSecret,
                         @JsonProperty("tokenRequestDelay") int tokenRequestDelay) {
        this.name = name;
        this.tokenEndpoint = tokenEndpoint;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenRequestDelay = tokenRequestDelay;
    }

    public String getName() {
        return name;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public int getTokenRequestDelay() {
        return tokenRequestDelay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OAuthProvider that = (OAuthProvider) o;
        return tokenRequestDelay == that.tokenRequestDelay &&
                Objects.equals(name, that.name) &&
                Objects.equals(tokenEndpoint, that.tokenEndpoint) &&
                Objects.equals(clientId, that.clientId) &&
                Objects.equals(clientSecret, that.clientSecret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, tokenEndpoint, clientId, clientSecret, tokenRequestDelay);
    }

    public OAuthProvider anonymize() {
        return new OAuthProvider(name, tokenEndpoint, clientId, ANONYMIZED_CLIENT_SECRET, tokenRequestDelay);
    }

}
