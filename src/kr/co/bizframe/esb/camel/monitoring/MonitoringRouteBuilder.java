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

package kr.co.bizframe.esb.camel.monitoring;

import static kr.co.bizframe.esb.camel.monitoring.util.TracerUtils.getLoggingLevel;
import static kr.co.bizframe.esb.camel.monitoring.util.TracerUtils.getTraceExchangeExcludeRoutes;
import static kr.co.bizframe.esb.camel.monitoring.util.TracerUtils.getTraceExchangeServerEndpoint;
import static kr.co.bizframe.esb.camel.monitoring.util.TracerUtils.getTraceExchangeTimer;
import static kr.co.bizframe.esb.camel.monitoring.util.TracerUtils.getTraceExcludeRoutes;
import static kr.co.bizframe.esb.camel.monitoring.util.TracerUtils.isJPAEnable;
import static kr.co.bizframe.esb.camel.monitoring.util.TracerUtils.isTraceEnable;
import static kr.co.bizframe.esb.camel.monitoring.util.TracerUtils.isTraceExchangeEnable;

import java.net.ConnectException;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jpa.JpaEndpoint;
import org.apache.camel.processor.interceptor.DefaultTraceFormatter;

import kr.co.bizframe.esb.camel.monitoring.exchange.BizFrameExchangeTracer;
import kr.co.bizframe.esb.camel.monitoring.exchange.ExchangeTracerErrorProcessor;
import kr.co.bizframe.esb.camel.monitoring.tracer.BizFrameJpaTraceEventMessage;
import kr.co.bizframe.esb.camel.monitoring.tracer.BizFrameTracer;
import kr.co.bizframe.esb.camel.monitoring.tracer.RouteIdPredicate;

public class MonitoringRouteBuilder extends RouteBuilder {
	
	// spring dsl bean inject
	/*@BeanInject("bizFrameExchangeTracer")
	private BizFrameExchangeTracer bizFrameExchangeTracer;*/
	
	public static final String ROUTE_ID = "sendTrace";
	public static final String TRACE_JPA_PERSISTENCEUNIT = "bizframeTracer";
	public static final String JPA_ENDPOINT_ID = "traced";
	public static final String EXCHANGE_TRACER_ID = "bizFrameExchangeTracer";
	
    public void configure() {    	
		
    	// jmx
		/*<jmxAgent id="agent" createConnector="true" registryPort="11099" />*/
    	
		// tracer node 
		/*<bean id="camelTracer" class="org.apache.camel.processor.interceptor.Tracer">
			<property name="useJpa" value="true" />
			<property name="destination" ref="traced" />
			<property name="logLevel" value="OFF" />
			<property name="traceOutExchanges" value="true" />
		</bean>

		<bean id="traceFormatter"
			class="org.apache.camel.processor.interceptor.DefaultTraceFormatter">
			<property name="showOutBody" value="true" />
			<property name="showOutBodyType" value="true" />
		</bean>*/

		BizFrameTracer tracer = new BizFrameTracer();
		boolean enable = isTraceEnable();
		tracer.setEnabled(enable);
		LoggingLevel logLevel = getLoggingLevel();
		
		if (enable) {
			
			boolean jpaEnable = isJPAEnable();
			tracer.setUseJpa(jpaEnable);
			if (jpaEnable) {
		    	// trace jsp endpoint
		    	/*<endpoint id="traced" uri="jpa://kr.co.bizframe.esb.camel.tracer.BizFrameJpaTraceEventMessage?persistenceUnit=bizframeTracer"/>*/
		    	
				JpaEndpoint jpa = new JpaEndpoint();
				jpa.setCamelContext(getContext());
				jpa.setEntityType(BizFrameJpaTraceEventMessage.class);
				jpa.setPersistenceUnit(TRACE_JPA_PERSISTENCEUNIT);

				try {
					getContext().addEndpoint(JPA_ENDPOINT_ID, jpa);
					tracer.setDestination(jpa);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
			
			tracer.setTraceOutExchanges(true);
			tracer.setLogLevel(logLevel);
			
			RouteIdPredicate filter= new RouteIdPredicate();
			filter.setExcludeRoutes(getTraceExcludeRoutes());
			tracer.setTraceFilter(filter);
			
			DefaultTraceFormatter formatter = tracer.getDefaultTraceFormatter();
			formatter.setShowBreadCrumb(false); 
			formatter.setShowNode(false);
			formatter.setShowOutBody(true);
			formatter.setShowOutBodyType(true);	
		}
		
		getContext().addInterceptStrategy(tracer);
		
		// trace finished exchange info
		/*<bean id="bizFrameExchangeTracer" class="kr.co.bizframe.esb.camel.monitoring.exchange.BizFrameExchangeTracer">
		<property name="excludeRoutes">
			<list value-type="java.lang.String">
				<value>sendTrace</value>
			</list>
		</property>
		</bean>*/
		
		boolean exchangeEnable = isTraceExchangeEnable();
		if (exchangeEnable) {
			
			BizFrameExchangeTracer bizFrameExchangeTracer = new BizFrameExchangeTracer();			
			bizFrameExchangeTracer.setExcludeRoutes(getTraceExchangeExcludeRoutes());
			getContext().getManagementStrategy().addEventNotifier(bizFrameExchangeTracer);
			//getContext().getInjector().newInstance(BizFrameExchangeTracer.class, bizFrameExchangeTracer);		
			ExchangeTracerErrorProcessor errorProcessor = new ExchangeTracerErrorProcessor();
			errorProcessor.setBizFrameExchangeTracer(bizFrameExchangeTracer);
			
			// route config
	    /*<route id="sendTrace" trace="false">
			<from uri="timer://foo?fixedRate=true&amp;period=5000"/>
			<to uri="bean:bizFrameExchangeTracer?method=sendFinishedExchangeInfos"/>
			<choice>
		  	<when>
		  		<simple>${bodyAs(String).length} > 0</simple>
				<log message="in body size : ${bodyAs(String).length}"/>		
				<to uri="jetty:http://localhost:8083/trace"/>
				<log message="-----------------response body : ${in.body}"/>
				<to uri="bean:bizFrameExchangeTracer?method=clearFinishedExchangeInfos"/>
		  	</when>
		  </choice>
		  
		  <onException>
				<exception>java.net.ConnectException</exception>
				<handled><constant>true</constant></handled>
				<process ref="bizFrameExchangeTracerErrorProcessor" />
		  </onException>
		  
		</route>*/
			
	    	from("timer://foo?fixedRate=true&period=" + getTraceExchangeTimer())
	    	.onException(ConnectException.class)
	    	 	.handled(true)
	    	 	.process(errorProcessor)
	    	 	.end()
	    	.routeId(ROUTE_ID)
	    	.bean(bizFrameExchangeTracer, "sendFinishedExchangeInfos")
	    	//.to("bean:"+EXCHANGE_TRACER_ID+"?method=sendFinishedExchangeInfos")
	    	 .choice()
	         .when(simple("${bodyAs(String).length} > 0"))
	         	 .log(logLevel, "in body size : ${bodyAs(String).length}")
	             .to(getTraceExchangeServerEndpoint())	 	    	 
	             .log(logLevel, "tracer response body : ${in.body}")
	             .bean(bizFrameExchangeTracer, "clearFinishedExchangeInfos")
	         ;
		}
    }
}
