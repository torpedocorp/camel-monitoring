/*                                                                              
 * Copyright 2018 Torpedo corp.                                                 
 *                                                                              
 * bizframe camel-monitoring project licenses this file to you under the Apache License,     
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:                   
 *                                                                              
 *   http://www.apache.org/licenses/LICENSE-2.0                                 
 *                                                                              
 * Unless required by applicable law or agreed to in writing, software          
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the     
 * License for the specific language governing permissions and limitations      
 * under the License.                                                           
 */ 

package kr.co.bizframe.esb.camel.monitoring.exchange;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.Route;
import org.apache.camel.management.event.ExchangeCompletedEvent;
import org.apache.camel.management.event.ExchangeFailedEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import kr.co.bizframe.esb.camel.monitoring.MonitoringRouteBuilder;

public class BizFrameExchangeTracer extends EventNotifierSupport {

	// dsl bean property
	private List<String> excludeRoutes;
	
	// TODO exchangeId pk
	private Map<String, FinishedExchangeInfo> finishedExchangeInfos = new ConcurrentHashMap<String, FinishedExchangeInfo>();

	public List<String> getExcludeRoutes() {
		if (excludeRoutes == null) {
			return Collections.emptyList();
		}

		return excludeRoutes;
	}

	public void setExcludeRoutes(List<String> excludeRoutes) {
		this.excludeRoutes = excludeRoutes;
	}
	
	public void setExcludeRoute(String excludeRoute) {
		if (this.excludeRoutes == null) {
			this.excludeRoutes = new ArrayList<String>();
		}
		this.excludeRoutes.add(excludeRoute);
	}
	
	public Collection<FinishedExchangeInfo> getFinishedExchangeInfos() {
		return finishedExchangeInfos.values();
	}
	
	public FinishedExchangeInfo removeExchangeInfo(String exchangeId) {
		return finishedExchangeInfos.remove(exchangeId);
	}
	
	@Handler
	public void sendFinishedExchangeInfos(Exchange exchange) {
		if (getFinishedExchangeInfos().size() == 0) {
			return;
		}
		Gson gson = new Gson();
		String json = gson.toJson(getFinishedExchangeInfos(), new TypeToken<List<FinishedExchangeInfo>>() {}.getType());
		StringBuilder sb = new StringBuilder();
		sb.append("{\"agentId\":");
		sb.append("\"");
		sb.append(System.getProperty("mas.id"));
		sb.append("\",");
		sb.append("\"data\":");
		sb.append(json);
		sb.append("}");
		String jsonString = sb.toString();
		exchange.getIn().setBody(jsonString);
		log.debug("=============sendFinishedExchangeInfos ========" + jsonString + "==== ");
	}
	
	@Handler
	public void clearFinishedExchangeInfos(Exchange exchange) {
		String response = "";
		try {
			response = IOUtils.toString(exchange.getIn().getBody(InputStream.class));
			log.debug("=============http out msg ========" + response + "==== ");

			List<String> savedIds = new Gson().fromJson(response, new TypeToken<ArrayList<String>>() {}.getType());
			
			if (savedIds == null) {

			} else {
				for (String id : savedIds) {
					FinishedExchangeInfo removeMsg = removeExchangeInfo(id);
					if (removeMsg != null) {
						log.debug("========clearFinishedExchangeInfos() " + removeMsg.getExchangeId() + " clear");
					}
				}
			}

			// TODO evictor policy,,,,,,			
			log.info("========remained FinishedExchangeInfos() " + getFinishedExchangeInfos().size());
			
		} catch (Throwable e) {
			log.error("clearFinishedExchangeInfos exchange error " + e.getMessage(), e);
			return;
		}
	}

	
	public void setFinishedExchangeInfo(Exchange exchange, boolean success) {
		Route sendTraceRoute = exchange.getContext().getRoute(MonitoringRouteBuilder.ROUTE_ID);
		if (sendTraceRoute == null) {			
			return;
		}
		
		FinishedExchangeInfo msg = new FinishedExchangeInfo(exchange);
		msg.setFinished(new Date());
		msg.setSuccess(success);
		if (success) {
			
		} else {
			try {
				Exception ex = exchange.getException();
				String exceptionMessage = ex.getMessage() + " at " + ex.getStackTrace()[0];
				msg.setErrorMsg(exceptionMessage);
			} catch (Throwable e) {

			}
		}
		
		this.finishedExchangeInfos.put(msg.getExchangeId(), msg);
	}
	

	private boolean isExclude(String routeId) {
		return getExcludeRoutes().contains(routeId);
	}
	

	protected void doStart() throws Exception {
		// filter out unwanted events
		/*
		 * setIgnoreCamelContextEvents(true); 
		 * setIgnoreServiceEvents(true);
		 * setIgnoreRouteEvents(true); 
		 * setIgnoreExchangeCreatedEvent(false); 
		 * setIgnoreExchangeSentEvents(true);
		 * setIgnoreExchangeCompletedEvent(false);
		 * setIgnoreExchangeFailedEvents(false);
		 * setIgnoreExchangeRedeliveryEvents(true);
		 */
	}

	@Override
	public boolean isEnabled(EventObject event) {
		return event instanceof ExchangeCompletedEvent || event instanceof ExchangeFailedEvent;

		/*
		 * ExchangeCreatedEvent -> ing
		 * ExchangeSendingEvent
		 * ExchangeSentEvent
		 * ExchangeCompletedEvent -> success
		 * ExchangeFailedEvent - > fail
		 */
	}

	@Override
	public void notify(EventObject event) throws Exception {

		if (event instanceof ExchangeCompletedEvent) {
			ExchangeCompletedEvent event0 = (ExchangeCompletedEvent) event;
			Exchange exchange = event0.getExchange();
			String routeId = exchange.getFromRouteId();			
			if (isExclude(routeId)) {
				return;
			}

			setFinishedExchangeInfo(exchange, true);
		}

		if (event instanceof ExchangeFailedEvent) {
			ExchangeFailedEvent event0 = (ExchangeFailedEvent) event;
			Exchange exchange = event0.getExchange();
			String routeId = exchange.getFromRouteId();
			if (isExclude(routeId)) {
				return;
			}
			
			setFinishedExchangeInfo(exchange, false);
		}
	}
}
