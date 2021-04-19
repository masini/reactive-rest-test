package net.lucamasini;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient
@RegisterProvider(CustomLoggingFilter.class)
@RegisterProvider(CustomClientExceptionHandler.class)
public interface HeadersService {

    @POST
    @Path("testcase")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    Response dumpHeaders(@FormParam("param1") String param1);

    @POST
    @Path("testcase")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<String> dumpHeadersUni(@FormParam("param1") String param1);
}
