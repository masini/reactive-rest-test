package net.lucamasini;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Provider
public class CustomLoggingFilter implements ClientRequestFilter, ClientResponseFilter, WriterInterceptor {

    private final static Logger log = LoggerFactory.getLogger(CustomLoggingFilter.class);

    private static final String NOTIFICATION_PREFIX = "* ";

    private static final String REQUEST_PREFIX = "> ";

    private static final String RESPONSE_PREFIX = "< ";

    private static final String REQUEST_START_TIME_PROPERTY = CustomLoggingFilter.class.getName() + ".requestStartTime";

    private static final String ENTITY_LOGGER_PROPERTY = CustomLoggingFilter.class.getName() + ".entityLogger";

    private static final String LOGGING_ID_PROPERTY = CustomLoggingFilter.class.getName() + ".id";

    private static final Comparator<Map.Entry<String, List<String>>> COMPARATOR = (o1, o2) -> o1.getKey().compareToIgnoreCase(o2.getKey());

    private static final int MAX_ENTITY_SIZE = 8 * 1024;

    private final AtomicLong _id = new AtomicLong(0);

    @Override
    public void filter(final ClientRequestContext context) {
        final long id = _id.incrementAndGet();
        context.setProperty(LOGGING_ID_PROPERTY, id);

        final StringBuilder b = new StringBuilder();

        printRequestLine(b, "Sending client request", id, context.getMethod(), context.getUri());
        printPrefixedHeaders(b, id, REQUEST_PREFIX, context.getStringHeaders());

        if (context.hasEntity()) {
            final OutputStream stream = new LoggingStream(b, context.getEntityStream());
            context.setEntityStream(stream);
            context.setProperty(ENTITY_LOGGER_PROPERTY, stream);
            // not calling log(b) here - it will be called by the interceptor
        } else {
            log(b);
        }

        context.setProperty(REQUEST_START_TIME_PROPERTY, System.currentTimeMillis());
    }

    @Override
    public void filter(final ClientRequestContext requestContext, final ClientResponseContext responseContext)
            throws IOException {
        long latency = System.currentTimeMillis() - (long) requestContext.getProperty(REQUEST_START_TIME_PROPERTY);

        final Object requestId = requestContext.getProperty(LOGGING_ID_PROPERTY);
        final long id = requestId != null ? (Long) requestId : _id.incrementAndGet();

        logLatency(latency, id, requestContext.getMethod(), requestContext.getUri());

        final StringBuilder b = new StringBuilder();

        printResponseLine(b, "Client response received", id, responseContext.getStatus());
        printPrefixedHeaders(b, id, RESPONSE_PREFIX, responseContext.getHeaders());

        if (responseContext.hasEntity()) {
            responseContext.setEntityStream(logInboundEntity(b, responseContext.getEntityStream()));
        }

        log(b);
    }

    @Override
    public void aroundWriteTo(final WriterInterceptorContext writerInterceptorContext)
            throws IOException, WebApplicationException {
        final LoggingStream stream = (LoggingStream) writerInterceptorContext.getProperty(ENTITY_LOGGER_PROPERTY);
        writerInterceptorContext.proceed();
        if (stream != null) {
            log(stream.getStringBuilder());
        }
    }

    private void logLatency(final long latency, final long id, final String method, final URI uri) {
        StringBuilder b = new StringBuilder();
        printLatencyLine(b, latency, id, method, uri);
        if (latency > 500) {
            log(b);
        } else {
            log(b);
        }
    }

    private void printLatencyLine(final StringBuilder b, final long latency, final long id, final String method, final URI uri) {
        prefixId(b, id)
                .append(NOTIFICATION_PREFIX)
                .append("Call to ")
                .append(method)
                .append(" ")
                .append(uri.toASCIIString())
                .append(" took ")
                .append(latency)
                .append(" milliseconds")
                .append("\n");
    }

    private void printRequestLine(final StringBuilder b, final String note, final long id, final String method, final URI uri) {
        prefixId(b, id)
                .append(NOTIFICATION_PREFIX)
                .append(note)
                .append(" on thread ")
                .append(Thread.currentThread().getName())
                .append("\n");
        prefixId(b, id)
                .append(REQUEST_PREFIX)
                .append(method)
                .append(" ")
                .append(uri.toASCIIString())
                .append("\n");
    }

    private void printResponseLine(final StringBuilder b, final String note, final long id, final int status) {
        prefixId(b, id)
                .append(NOTIFICATION_PREFIX)
                .append(note)
                .append(" on thread ").append(Thread.currentThread().getName()).append("\n");
        prefixId(b, id)
                .append(RESPONSE_PREFIX)
                .append(status)
                .append("\n");
    }

    private StringBuilder prefixId(final StringBuilder b, final long id) {
        b.append(id).append(" ");
        return b;
    }

    private void printPrefixedHeaders(final StringBuilder b,
                                      final long id,
                                      final String prefix,
                                      final MultivaluedMap<String, String> headers) {
        for (final Map.Entry<String, List<String>> headerEntry : getSortedHeaders(headers.entrySet())) {
            final List<?> val = headerEntry.getValue();
            final String header = headerEntry.getKey();

            if (val.size() == 1) {
                prefixId(b, id).append(prefix).append(header).append(": ").append(val.get(0)).append("\n");
            } else {
                final StringBuilder sb = new StringBuilder();
                boolean add = false;
                for (final Object s : val) {
                    if (add) {
                        sb.append(',');
                    }
                    add = true;
                    sb.append(s);
                }
                prefixId(b, id).append(prefix).append(header).append(": ").append(sb.toString()).append("\n");
            }
        }
    }

    private Set<Map.Entry<String, List<String>>> getSortedHeaders(final Set<Map.Entry<String, List<String>>> headers) {
        final TreeSet<Map.Entry<String, List<String>>> sortedHeaders = new TreeSet<>(COMPARATOR);
        sortedHeaders.addAll(headers);
        return sortedHeaders;
    }

    private InputStream logInboundEntity(final StringBuilder b, InputStream stream) throws IOException {
        if (!stream.markSupported()) {
            stream = new BufferedInputStream(stream);
        }
        stream.mark(MAX_ENTITY_SIZE + 1);
        final byte[] entity = new byte[MAX_ENTITY_SIZE + 1];
        final int entitySize = stream.read(entity);
        if (entitySize > 0) {
            b.append(new String(entity, 0, Math.min(entitySize, MAX_ENTITY_SIZE), StandardCharsets.UTF_8));
            if (entitySize > MAX_ENTITY_SIZE) {
                b.append("...more...");
            }
            b.append('\n');
        }
        stream.reset();
        return stream;
    }

    private void log(final StringBuilder b) {
        log.info(b.toString());
    }

    private class LoggingStream extends FilterOutputStream {

        private final StringBuilder b;

        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        LoggingStream(final StringBuilder b, final OutputStream inner) {
            super(inner);
            this.b = b;
        }

        StringBuilder getStringBuilder() {
            // write entity to the builder
            final byte[] entity = baos.toByteArray();

            b.append(new String(entity, 0, Math.min(entity.length, CustomLoggingFilter.this.MAX_ENTITY_SIZE), StandardCharsets.UTF_8));
            if (entity.length > CustomLoggingFilter.this.MAX_ENTITY_SIZE) {
                b.append("...more...");
            }
            b.append('\n');

            return b;
        }

        @Override
        public void write(final int i) throws IOException {
            if (baos.size() <= CustomLoggingFilter.this.MAX_ENTITY_SIZE) {
                baos.write(i);
            }
            out.write(i);
        }

    }

}
