package net.lucamasini;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RegisterRestClient
@RegisterProvider(CustomClientExceptionHandler.class)
public interface MockService {

    @GET
    @Path("/{status}")
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<ResponseFromMock> callMock(@PathParam("status") int status);

    @POST
    @Path("/{status}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<ResponseFromMock> callMock(@PathParam("status") int status, ResponseFromMock body);

    @POST
    @Path("/{status}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    ResponseFromMock callMockNoReactive(@PathParam("status") int status, ResponseFromMock body) throws MyCustomException;

    @POST
    @Path("/{status}")
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<ResponseFromMock> callMockForm(@PathParam("status") int status, ResponseFromMock body) throws MyCustomException;

}
