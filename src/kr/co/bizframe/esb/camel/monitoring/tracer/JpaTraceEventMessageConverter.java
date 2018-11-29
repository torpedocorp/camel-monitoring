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
