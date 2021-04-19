package net.lucamasini;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("testcase")
public class TestCaseResource {

    private final static Logger log = LoggerFactory.getLogger(TestCaseResource.class);

    @Inject
    @RestClient
    MockService mockService;

    @Inject
    @RestClient
    HeadersService headersService;

    @Inject
    ObjectMapper mapper;

    @GET
    @Path("1")
    @Produces(MediaType.APPLICATION_JSON)
    @Blocking
    public ResponseFromMock testCase1() throws MyCustomException {
        return mockService.callMockNoReactive(401, new ResponseFromMock());
    }

    @GET
    @Path("2")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<ResponseFromMock> testCase2() /*throws MyCustomException*/ {
        return mockService.callMock(401, new ResponseFromMock())
                .onFailure().call(t -> {
                    log.info("exception calling mock", t);

                    return Uni.createFrom().item(new ResponseFromMock());
                })
                ;
    }

    @GET
    @Path("3")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<ResponseFromMock> testCase3() throws MyCustomException {
        return mockService.callMockForm(401, new ResponseFromMock())
        .onFailure().call(t -> {
            log.info("exception calling mock", t);

            return Uni.createFrom().item(new ResponseFromMock());
        })
        ;
    }

    @GET
    @Path("4")
    @Produces(MediaType.APPLICATION_JSON)
    @Blocking
    public Response testCase4() {
        return headersService.dumpHeaders("paramValue");
    }

    @GET
    @Path("5")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<String> testCase5() {
        return headersService.dumpHeadersUni("paramValue");
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response dumpHeaders(@Context HttpHeaders headers) {

        final var array = mapper.createArrayNode();

        for(var header: headers.getRequestHeaders().entrySet()) {
            final var node = mapper.createObjectNode();
            node.put(header.getKey(), header.getValue().toString());

            array.add(node);
        }

        return Response.ok(array).build();
    }

}
