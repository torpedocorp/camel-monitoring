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

package kr.co.bizframe.esb.camel.monitoring.tracer;
import java.util.Collections;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;

public class RouteIdPredicate implements Predicate {

	private List<String> excludeRoutes;

	public List<String> getExcludeRoutes() {
		if (excludeRoutes == null) {
			return Collections.emptyList();
		}

		return excludeRoutes;
	}

	public void setExcludeRoutes(List<String> excludeRoutes) {
		this.excludeRoutes = excludeRoutes;
	}

	private boolean isExclude(String routeId) {
		return getExcludeRoutes().contains(routeId);
	}

	@Override
	public boolean matches(Exchange exchange) {
		String routeId = exchange.getFromRouteId();

		if (isExclude(routeId)) {
			return false;
		}
		return true;
	}

}
