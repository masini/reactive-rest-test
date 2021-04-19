package net.lucamasini;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

//@Provider
//@ConstrainedTo(RuntimeType.SERVER)
public class DefaultExceptionMapper implements ExceptionMapper<MyCustomException> {

    @Override
    public Response toResponse(MyCustomException ex) {

        return Response.status(209).build();
    }

}
