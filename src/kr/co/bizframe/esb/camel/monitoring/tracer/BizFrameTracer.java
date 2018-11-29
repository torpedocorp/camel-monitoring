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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.camel.CamelContext;
import org.apache.camel.processor.interceptor.TraceEventHandler;
import org.apache.camel.processor.interceptor.TraceFormatter;
import org.apache.camel.processor.interceptor.Tracer;
import org.apache.camel.spi.InterceptStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BizFrameTracer extends Tracer {

	private static final String JPA_TRACE_EVENT_MESSAGE = "kr.co.bizframe.esb.camel.monitoring.tracer.BizFrameJpaTraceEventMessage";
	private static Logger log = LoggerFactory.getLogger(BizFrameTracer.class);
	
	private String logName = BizFrameTracer.class.getName();
	private final List<TraceEventHandler> traceHandlers = new CopyOnWriteArrayList<>();
	private String jpaTraceEventMessageClassName = JPA_TRACE_EVENT_MESSAGE;

	public BizFrameTracer() {
		traceHandlers.add(new BizFrameTraceEventHandler(this));
		
	}

	public static Tracer createTracer(CamelContext context) {
        Tracer tracer = new BizFrameTracer();
        // lets see if we have a formatter if so use it
        TraceFormatter formatter = context.getRegistry().lookupByNameAndType("traceFormatter", TraceFormatter.class);
        if (formatter != null) {
            tracer.setFormatter(formatter);
        }
        return tracer;
    }
	
	public static Tracer getTracer(CamelContext context) {
        List<InterceptStrategy> list = context.getInterceptStrategies();
        for (InterceptStrategy interceptStrategy : list) {
            if (interceptStrategy instanceof BizFrameTracer) {
            	log.debug("==========interceptStrategy====================" + interceptStrategy);
                return (Tracer) interceptStrategy;
            }
        }
        return null;
    }
	
	public String getLogName() {
		return logName;
	}

	/**
	 * 
	 * @return the first trace event handler
	 */
	@Deprecated
	public TraceEventHandler getTraceHandler() {
		return traceHandlers.get(0);
	}

	/**
	 * 
	 * @return list of tracehandlers
	 */
	public List<TraceEventHandler> getTraceHandlers() {
		return traceHandlers;
	}

	/**
	 * Set the object to be used to perform tracing.
	 * <p/>
	 * Use this to take more control of how trace events are persisted. Setting
	 * the traceHandler provides a simpler mechanism for controlling tracing
	 * than the TraceInterceptorFactory. The TraceHandler should only be set
	 * before any routes are created, hence this method is not thread safe.
	 */
	@Deprecated
	public void setTraceHandler(TraceEventHandler traceHandler) {
		this.traceHandlers.clear();
		this.traceHandlers.add(traceHandler);
	}

	/**
	 * Add the given tracehandler
	 */
	public void addTraceHandler(TraceEventHandler traceHandler) {
		this.traceHandlers.add(traceHandler);
	}

	/**
	 * Remove the given tracehandler
	 */
	public void removeTraceHandler(TraceEventHandler traceHandler) {
		this.traceHandlers.remove(traceHandler);
	}

	public String getJpaTraceEventMessageClassName() {
		return jpaTraceEventMessageClassName;
	}

	/**
	 * Set the fully qualified name of the class to be used by the JPA event
	 * tracing.
	 * <p/>
	 * The class must exist in the classpath and be available for dynamic
	 * loading. The class name should only be set before any routes are created,
	 * hence this method is not thread safe.
	 */
	public void setJpaTraceEventMessageClassName(String jpaTraceEventMessageClassName) {
		this.jpaTraceEventMessageClassName = jpaTraceEventMessageClassName;
	}

	public void start() throws Exception {
		// noop
	}

	public void stop() throws Exception {
		traceHandlers.clear();
	}

	@Override
	public String toString() {
		return "BizFrameTracer";
	}
}
