package pl.allegro.tech.hermes.api.endpoints;

import pl.allegro.tech.hermes.api.OAuthProvider;
import pl.allegro.tech.hermes.api.PatchData;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("oAuthProviders")
public interface OAuthProviderEndpoint {

    @GET
    @Produces(APPLICATION_JSON)
    List<String> list();

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    Response create(OAuthProvider oAuthProvider);

    @PUT
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Path("/{oAuthProviderName}")
    Response update(@PathParam("oAuthProviderName") String name, PatchData patch);

    @GET
    @Produces(APPLICATION_JSON)
    @Path("/{oAuthProviderName}")
    OAuthProvider get(@PathParam("oAuthProviderName") String oAuthProviderName);

    @DELETE
    @Produces(APPLICATION_JSON)
    @Path("/{oAuthProviderName}")
    Response remove(@PathParam("oAuthProviderName") String oAuthProviderName);
}
