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
