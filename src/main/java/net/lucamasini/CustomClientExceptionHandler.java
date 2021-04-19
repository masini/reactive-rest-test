package net.lucamasini;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

//@Provider
@ConstrainedTo(RuntimeType.CLIENT)
public class CustomClientExceptionHandler implements ResponseExceptionMapper<MyCustomException> {

    private final static Logger log = LoggerFactory.getLogger(CustomClientExceptionHandler.class);

    public CustomClientExceptionHandler() {
        log.info("instantiated");
    }

    @Override
    public MyCustomException toThrowable(Response response) {
        log.info("*********************** toThrowable {}", response.getStatus());
        return new MyCustomException();
    }

    @Override
    public boolean handles(int status, MultivaluedMap<String, Object> headers) {
        log.info("status={}", status);
        return status == 401;
    }
}
