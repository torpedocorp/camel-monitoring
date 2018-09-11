package kr.co.bizframe.esb.camel.monitoring.tracer;
import static kr.co.bizframe.esb.camel.monitoring.util.TracerUtils.getWriteFileDir;
import static kr.co.bizframe.esb.camel.monitoring.util.TracerUtils.saveFile;

import java.io.File;
import java.util.UUID;

import org.apache.camel.Converter;
import org.apache.camel.processor.interceptor.jpa.JpaTraceEventMessage;

@Converter
public class JpaTraceEventMessageConverter {

	@Converter
	public static BizFrameJpaTraceEventMessage toBizFrameJpaTraceEventMessage(JpaTraceEventMessage jpaMessage)
			throws Throwable {
		
		BizFrameJpaTraceEventMessage result = new BizFrameJpaTraceEventMessage();
		//result.setId(jpaMessage.getId());
		result.setId(UUID.randomUUID().toString());
		result.setAgentId(System.getProperty("mas.id"));
		result.setTimestamp(jpaMessage.getTimestamp());
		result.setFromEndpointUri(jpaMessage.getFromEndpointUri());
		result.setPreviousNode(jpaMessage.getPreviousNode());		
		result.setToNode(jpaMessage.getToNode());
		result.setExchangeId(jpaMessage.getExchangeId());
		result.setShortExchangeId(jpaMessage.getShortExchangeId());
		result.setExchangePattern(jpaMessage.getExchangePattern());
		result.setProperties(jpaMessage.getProperties());
		result.setHeaders(jpaMessage.getHeaders());
		result.setBody(saveFileAndGetPath(jpaMessage.getExchangeId(), jpaMessage.getBody()));
		result.setBodyType(jpaMessage.getBodyType());
		result.setOutHeaders(jpaMessage.getOutHeaders());
		result.setOutBody(saveFileAndGetPath(jpaMessage.getExchangeId(), jpaMessage.getOutBody()));
		result.setOutBodyType(jpaMessage.getOutBodyType());
		result.setCausedByException(jpaMessage.getCausedByException());
		result.setRouteId(jpaMessage.getRouteId());
		return result;
	}

	private static String saveFileAndGetPath(String id, String body) throws Throwable {
		File parent = getWriteFileDir();
		File file = saveFile(parent, id, body);
		if (file == null) {
			return null;
		}
		return file.getCanonicalPath();
	}

}
