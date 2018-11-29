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

import java.lang.reflect.Type;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import kr.co.bizframe.esb.camel.monitoring.MonitoringRouteBuilder;

public class ExchangeTracerErrorProcessor implements Processor {

	private BizFrameExchangeTracer bizFrameExchangeTracer;
	
	public void setBizFrameExchangeTracer(BizFrameExchangeTracer bizFrameExchangeTracer) {
		this.bizFrameExchangeTracer = bizFrameExchangeTracer;
	}
	

	public BizFrameExchangeTracer getBizFrameExchangeTracer() {
		return bizFrameExchangeTracer;
	}

	private List<FinishedExchangeInfo> parseReceiveJson(String json) {
		Gson gson = new Gson();
		Type listType = new TypeToken<List<FinishedExchangeInfo>>() {}.getType();
		JsonParser parser = new JsonParser();
		JsonObject obj = parser.parse(json).getAsJsonObject();
		String dataJson = obj.get("data").getAsJsonArray().toString();
		List<FinishedExchangeInfo> infos = gson.fromJson(dataJson, listType);
		return infos;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		String routeId = exchange.getFromRouteId();
		
		if (MonitoringRouteBuilder.ROUTE_ID.equals(routeId)) {			
			try {
				String body = (String) exchange.getIn().getBody();
				List<FinishedExchangeInfo> infos = parseReceiveJson(body);
				System.out.println(getBizFrameExchangeTracer());
				for (FinishedExchangeInfo info : infos) {					
					System.out.println("remove : " + getBizFrameExchangeTracer().removeExchangeInfo(info.getExchangeId()));
				}
				
			} catch (Throwable e) {
				e.printStackTrace();
				// TODO  evictor....
			}
		}
		
	}
}
