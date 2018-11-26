package kr.co.bizframe.esb.camel.monitoring.tracer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.Service;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.interceptor.TraceEventHandler;
import org.apache.camel.processor.interceptor.TraceEventMessage;
import org.apache.camel.processor.interceptor.TraceInterceptor;
import org.apache.camel.util.IntrospectionSupport;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.ServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BizFrameTraceEventHandler implements TraceEventHandler, Service {
	private static Logger log = LoggerFactory.getLogger(BizFrameTraceEventHandler.class);

	private Producer traceEventProducer;
	private Class<?> jpaTraceEventMessageClass;
	private String jpaTraceEventMessageClassName;

	private final BizFrameTracer tracer;	 
	public static final String BIZFRAME_TRACER_INOUT_PROPERTY =  "bizframe_inout";
	
	public BizFrameTraceEventHandler(BizFrameTracer tracer) {
		this.tracer = tracer;
	}

	private synchronized void loadJpaTraceEventMessageClass(Exchange exchange) {
		if (jpaTraceEventMessageClass == null) {
			jpaTraceEventMessageClassName = tracer.getJpaTraceEventMessageClassName();
		}
		if (jpaTraceEventMessageClass == null) {
			jpaTraceEventMessageClass = exchange.getContext().getClassResolver()
					.resolveClass(jpaTraceEventMessageClassName);
			if (jpaTraceEventMessageClass == null) {
				throw new IllegalArgumentException("Cannot find class: " + jpaTraceEventMessageClassName
						+ ". Make sure camel-jpa.jar is in the classpath.");
			}
		}
	}

	private synchronized Producer getTraceEventProducer(Exchange exchange) throws Exception {
		if (traceEventProducer == null) {
			// create producer when we have access the camel context (we dont in
			// doStart)
			Endpoint endpoint = tracer.getDestination() != null ? tracer.getDestination()
					: exchange.getContext().getEndpoint(tracer.getDestinationUri());
			traceEventProducer = endpoint.createProducer();
			ServiceHelper.startService(traceEventProducer);
		}
		return traceEventProducer;
	}
	
	@Override
	public void traceExchange(ProcessorDefinition<?> node, Processor target, TraceInterceptor traceInterceptor,
			Exchange exchange) throws Exception {
		if (tracer.getDestination() != null || tracer.getDestinationUri() != null) {
			// create event exchange and add event information
			Date timestamp = new Date();
			Exchange event = new DefaultExchange(exchange);
			event.setProperty(Exchange.TRACE_EVENT_NODE_ID, node.getId());
			event.setProperty(Exchange.TRACE_EVENT_TIMESTAMP, timestamp);
			// keep a reference to the original exchange in case its needed
			event.setProperty(Exchange.TRACE_EVENT_EXCHANGE, exchange);

			// create event message to sent as in body containing event
			// information such as
			// from node, to node, etc.
			TraceEventMessage msg = new BizFrameTraceEventMessage(timestamp, node, exchange);

			// should we use ordinary or jpa objects
			if (tracer.isUseJpa()) {
				if (log.isTraceEnabled()) {
					log.trace("Using class: {} for tracing event messages", this.jpaTraceEventMessageClassName);
				}

				// load the jpa event message class
				loadJpaTraceEventMessageClass(exchange);
				// create a new instance of the event message class
				Object jpa = ObjectHelper.newInstance(jpaTraceEventMessageClass);

				// copy options from event to jpa
				Map<String, Object> options = new HashMap<>();
				IntrospectionSupport.getProperties(msg, options, null);
				IntrospectionSupport.setProperties(exchange.getContext().getTypeConverter(), jpa, options);
				// and set the timestamp as its not a String type
				IntrospectionSupport.setProperty(exchange.getContext().getTypeConverter(), jpa, "timestamp", msg.getTimestamp());

				event.getIn().setBody(jpa);
			} else {
				event.getIn().setBody(msg);
			}

			// marker property to indicate its a tracing event being routed in
			// case
			// new Exchange instances is created during trace routing so we can
			// check
			// for this marker when interceptor also kick in during routing of
			// trace events
			event.setProperty(Exchange.TRACE_EVENT, Boolean.TRUE);
			try {
				// process the trace route
				getTraceEventProducer(exchange).process(event);
			} catch (Exception e) {
				// log and ignore this as the original Exchange should be
				// allowed to continue
				log.error("Error processing trace event (original Exchange will continue): " + event, e);
			}
		}
	}

	@Override
	public Object traceExchangeIn(ProcessorDefinition<?> node, Processor target, TraceInterceptor traceInterceptor,
			Exchange exchange) throws Exception {		
		log.debug("=====traceExchangeIn==============" + node.getIndex() + ", " + node.getId());
		exchange.setProperty(BIZFRAME_TRACER_INOUT_PROPERTY, "in");
		traceExchange(node, target, traceInterceptor, exchange);
		return null;
	}

	@Override
	public void traceExchangeOut(ProcessorDefinition<?> node, Processor target, TraceInterceptor traceInterceptor,
			Exchange exchange, Object traceState) throws Exception {
		log.debug("=====traceExchangeOut =============" + node.getIndex() + ", " + node.getId());
		exchange.setProperty(BIZFRAME_TRACER_INOUT_PROPERTY, "out");
		traceExchange(node, target, traceInterceptor, exchange);
	}

	@Override
	public void start() throws Exception {
		traceEventProducer = null;
	}

	@Override
	public void stop() throws Exception {
		if (traceEventProducer != null) {
			ServiceHelper.stopService(traceEventProducer);
		}
	}

}
