package kr.co.bizframe.esb.camel.monitoring.exchange;

import java.util.Date;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.camel.Exchange;

public class FinishedExchangeInfo {

	private String exchangeId;
	private String routeId;
	private Date created;
	private Date finished;
	private boolean success;
	private String errorMsg;

	/*
	 * String CORRELATION_ID = "CamelCorrelationId"; 
	 * String GROUPED_EXCHANGE = "CamelGroupedExchange";
	 */

	public FinishedExchangeInfo(Exchange exchange) {
		this.exchangeId = exchange.getExchangeId();
		this.routeId = exchange.getFromRouteId();
		this.created = exchange.getProperty(Exchange.CREATED_TIMESTAMP, Date.class);
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getExchangeId() {
		return exchangeId;
	}

	public void setExchangeId(String exchangeId) {
		this.exchangeId = exchangeId;
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getFinished() {
		return finished;
	}

	public void setFinished(Date finished) {
		this.finished = finished;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FinishedExchangeMessage [exchangeId=");
		builder.append(exchangeId);
		builder.append(", routeId=");
		builder.append(routeId);
		builder.append(", created=");
		builder.append(created);
		builder.append(", finished=");
		builder.append(finished);
		builder.append(", success=");
		builder.append(success);
		builder.append("]");
		return builder.toString();
	}

}
