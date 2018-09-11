package kr.co.bizframe.esb.camel.monitoring.tracer;

import java.io.Serializable;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.RouteNode;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.interceptor.DefaultTraceEventMessage;
import org.apache.camel.processor.interceptor.TraceEventMessage;
import org.apache.camel.spi.TracedRouteNodes;
import org.apache.camel.util.MessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BizFrameTraceEventMessage implements Serializable, TraceEventMessage {
    private static final long serialVersionUID = -4549012920528941203L;
    private static Logger log = LoggerFactory.getLogger(BizFrameTraceEventMessage.class);
    
    private Date timestamp;
    private String fromEndpointUri;
    private String previousNode;
    private String toNode;
    private String exchangeId;
    private String shortExchangeId;
    private String exchangePattern;
    private String properties;
    private String headers;
    private String body;
    private String bodyType;
    private String outHeaders;
    private String outBody;
    private String outBodyType;
    private String causedByException;
    private String routeId;
    private String traceInOut;
    private final transient Exchange tracedExchange;

    /**
     * Creates a {@link DefaultTraceEventMessage} based on the given node it was traced while processing
     * the current {@link Exchange}
     *
     * @param toNode the node where this trace is intercepted
     * @param exchange the current {@link Exchange}
     */
    public BizFrameTraceEventMessage(final Date timestamp, final ProcessorDefinition<?> toNode, final Exchange exchange) {
        this.tracedExchange = exchange;
        Message in = exchange.getIn();

        // need to use defensive copies to avoid Exchange altering after the point of interception
        this.timestamp = timestamp;
        this.fromEndpointUri = exchange.getFromEndpoint() != null ? exchange.getFromEndpoint().getEndpointUri() : null;
        this.previousNode = extractFromNode(exchange);
        //this.toNode = extractToNode(exchange);        
        this.toNode = toNode.getId();
        this.exchangeId = exchange.getExchangeId();
        this.routeId = exchange.getFromRouteId();
        this.shortExchangeId = extractShortExchangeId(exchange);
        this.exchangePattern = exchange.getPattern().toString();
        this.properties = exchange.getProperties().isEmpty() ? null : exchange.getProperties().toString();
        this.headers = in.getHeaders().isEmpty() ? null : in.getHeaders().toString();
        // We should not turn the message body into String
        this.body = MessageHelper.extractBodyForLogging(in, "");
        this.bodyType = MessageHelper.getBodyTypeName(in);
        if (exchange.hasOut()) {
            Message out = exchange.getOut();
            this.outHeaders = out.getHeaders().isEmpty() ? null : out.getHeaders().toString();
            this.outBody = MessageHelper.extractBodyAsString(out);
            this.outBodyType = MessageHelper.getBodyTypeName(out);
        }
        this.causedByException = extractCausedByException(exchange);
        this.traceInOut = extractInout(exchange);
    }

    // Implementation
    //---------------------------------------------------------------

    private static String extractInout(Exchange exchange) {
    	return exchange.getProperty(BizFrameTraceEventHandler.BIZFRAME_TRACER_INOUT_PROPERTY, String.class);
    }
    
    private static String extractCausedByException(Exchange exchange) {
        Throwable cause = exchange.getException();
        if (cause == null) {
            cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
        }

        if (cause != null) {
            return cause.toString();
        } else {
            return null;
        }
    }

    private static String extractShortExchangeId(Exchange exchange) {
        return exchange.getExchangeId().substring(exchange.getExchangeId().indexOf("/") + 1);
    }

    private static String extractFromNode(Exchange exchange) {
        if (exchange.getUnitOfWork() != null) {
            TracedRouteNodes traced = exchange.getUnitOfWork().getTracedRouteNodes();
            if (traced != null) {
                RouteNode last = traced.getSecondLastNode();
                return last != null ? last.getLabel(exchange) : null;
            }
        }
        return null;
    }

    private static String extractToNode(Exchange exchange) {
        if (exchange.getUnitOfWork() != null) {
            TracedRouteNodes traced = exchange.getUnitOfWork().getTracedRouteNodes();
            if (traced != null) {
                RouteNode last = traced.getLastNode();
                return last != null ? last.getLabel(exchange) : null;
            }
        }
        return null;
    }

    // Properties
    //---------------------------------------------------------------

    public Date getTimestamp() {
        return timestamp;
    }

    public String getFromEndpointUri() {
        return fromEndpointUri;
    }

    public String getPreviousNode() {
        return previousNode;
    }

    public String getToNode() {
        return toNode;
    }

    public String getExchangeId() {
        return exchangeId;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getShortExchangeId() {
        return shortExchangeId;
    }

    public String getExchangePattern() {
        return exchangePattern;
    }

    public String getProperties() {
        return properties;
    }

    public String getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public String getBodyType() {
        return bodyType;
    }

    public String getOutBody() {
        return outBody;
    }

    public String getOutBodyType() {
        return outBodyType;
    }

    public String getOutHeaders() {
        return outHeaders;
    }

    public String getCausedByException() {
        return causedByException;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setFromEndpointUri(String fromEndpointUri) {
        this.fromEndpointUri = fromEndpointUri;
    }

    public void setPreviousNode(String previousNode) {
        this.previousNode = previousNode;
    }

    public void setToNode(String toNode) {
        this.toNode = toNode;
    }

    public void setExchangeId(String exchangeId) {
        this.exchangeId = exchangeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public void setShortExchangeId(String shortExchangeId) {
        this.shortExchangeId = shortExchangeId;
    }

    public void setExchangePattern(String exchangePattern) {
        this.exchangePattern = exchangePattern;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public void setOutBody(String outBody) {
        this.outBody = outBody;
    }

    public void setOutBodyType(String outBodyType) {
        this.outBodyType = outBodyType;
    }

    public void setOutHeaders(String outHeaders) {
        this.outHeaders = outHeaders;
    }

    public void setCausedByException(String causedByException) {
        this.causedByException = causedByException;
    }

	public Exchange getTracedExchange() {
		return tracedExchange;
	}

	public String getTraceInOut() {
		return traceInOut;
	}

	public void setTraceInOut(String traceInOut) {
		this.traceInOut = traceInOut;
	}

	@Override
    public String toString() {
        return "TraceEventMessage[" + exchangeId + "] on node: " + toNode;
    }
}