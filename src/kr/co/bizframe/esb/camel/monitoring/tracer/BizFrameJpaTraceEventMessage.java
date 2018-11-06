package kr.co.bizframe.esb.camel.monitoring.tracer;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.camel.Exchange;
import org.apache.camel.processor.interceptor.TraceEventMessage;

/**
 * A JPA based {@link org.apache.camel.processor.interceptor.TraceEventMessage}
 * that is capable of persisting trace event into a database.
 */
@Entity
@Table(name = "BIZFRAME_CAMEL_MESSAGETRACED")
public class BizFrameJpaTraceEventMessage implements TraceEventMessage, Serializable {
	private static final long serialVersionUID = -3577516047575267548L;

	@Id
	private String id;
	
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
	private String agentId;
	private String traceInOut;

	public BizFrameJpaTraceEventMessage() {
		this.id = UUID.randomUUID().toString();
		this.agentId = System.getProperty("mas.id");
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getPreviousNode() {
		return previousNode;
	}

	public void setPreviousNode(String previousNode) {
		this.previousNode = previousNode;
	}

	public String getFromEndpointUri() {
		return fromEndpointUri;
	}

	public void setFromEndpointUri(String fromEndpointUri) {
		this.fromEndpointUri = fromEndpointUri;
	}

	public String getToNode() {
		return toNode;
	}

	public void setToNode(String toNode) {
		this.toNode = toNode;
	}

	public String getExchangeId() {
		return exchangeId;
	}

	public void setExchangeId(String exchangeId) {
		this.exchangeId = exchangeId;
	}

	public String getShortExchangeId() {
		return shortExchangeId;
	}

	public void setShortExchangeId(String shortExchangeId) {
		this.shortExchangeId = shortExchangeId;
	}

	public String getExchangePattern() {
		return exchangePattern;
	}

	public void setExchangePattern(String exchangePattern) {
		this.exchangePattern = exchangePattern;
	}

	// @Lob
	@Column(columnDefinition = "VARCHAR(32672)")
	public String getProperties() {
		return properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}

	// @Lob
	@Column(columnDefinition = "VARCHAR(32672)")
	public String getHeaders() {
		return headers;
	}

	public void setHeaders(String headers) {
		this.headers = headers;
	}

	// @Lob
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getBodyType() {
		return bodyType;
	}

	public void setBodyType(String bodyType) {
		this.bodyType = bodyType;
	}

	// @Lob
	public String getOutBody() {
		return outBody;
	}

	public void setOutBody(String outBody) {
		this.outBody = outBody;
	}

	public String getOutBodyType() {
		return outBodyType;
	}

	public void setOutBodyType(String outBodyType) {
		this.outBodyType = outBodyType;
	}

	public String getOutHeaders() {
		return outHeaders;
	}

	public void setOutHeaders(String outHeaders) {
		this.outHeaders = outHeaders;
	}

	// @Lob
	@Column(columnDefinition = "VARCHAR(32672)")
	public String getCausedByException() {
		return causedByException;
	}

	public void setCausedByException(String causedByException) {
		this.causedByException = causedByException;
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	@Transient
	public Exchange getTracedExchange() {
		return null;
	}

	public String getTraceInOut() {
		return traceInOut;
	}

	public void setTraceInOut(String traceInOut) {
		this.traceInOut = traceInOut;
	}

	@Override
	public String toString() {
		return "TraceEventMessage[" + getExchangeId() + "] on node: " + getToNode();
	}

}
