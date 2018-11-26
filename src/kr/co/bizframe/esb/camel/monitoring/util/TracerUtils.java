package kr.co.bizframe.esb.camel.monitoring.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.camel.LoggingLevel;
import org.apache.commons.io.IOUtils;

import kr.co.bizframe.esb.camel.monitoring.MonitoringRouteBuilder;

public class TracerUtils {

	private static final String CONFIG_FILE = "camel-monitoring.properties";
	private static Properties properties = null;
	
	static {
		UUID.randomUUID();
		try {
			initConfig();
		} catch (Throwable e1) {
			throw new ExceptionInInitializerError(e1);
		}
	}
	
	public static void initConfig() throws Throwable {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream is = null;
		File f = null;

		try {
			URL url = classLoader.getResource(CONFIG_FILE);

			if (url == null) {
				String confPath = System.getProperty("camle-monitoring.conf");
				if (confPath != null) {
					f = new File(confPath);
				}
			} else {
				f = new File(url.getFile());
			}

			if (f == null) {
				return;
			}

			is = new FileInputStream(f);
			properties = new Properties();
			properties.load(is);
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException ioe) {
			}
		}

		System.out.println("CAMEL MONITORING CONFIG_FILE " + f + " load !!");
	}

	public static File getWriteFileDir() {
		String dir = null;
		if (properties != null) {
			dir = properties.getProperty("file.save.dir");
		}
		if (dir == null || dir.length() == 0) {
			dir = "./../data/file";
		}
		File f= new File(dir);
		if (!f.exists()) {
			f.mkdirs();
		}
		return f;
	}
	
	public static boolean isEnable(String key, boolean defaultValue) {
		if (properties != null) {
			String value = trim(properties.getProperty(key));
			if (value != null) {
				try {
					return Boolean.parseBoolean(value);
				} catch (Throwable e) {					
				}
			}
		}
		// default
		return defaultValue;
	}
	
	public static boolean isTraceEnable() {
		return isEnable("tracer.enabled", true);
	}
	
	public static boolean isJPAEnable() {
		return isEnable("tracer.jpa", true);
	}	
	
	public static List<String> getTraceExcludeRoutes() {
		return getExcludeRoutes("tracer.exclude.routes");
	}	
	
	public static LoggingLevel getLoggingLevel() {
		if (properties != null) {
			String value = properties.getProperty("tracer.log.level");
			if (trim(value) != null) {
				try {
					return LoggingLevel.valueOf(value.toUpperCase());
				} catch (Throwable e) {

				}
			}
		}
		// default
		return LoggingLevel.OFF;
	}
	
	
	public static boolean isTraceExchangeEnable() {
		return isEnable("trace.exchange.enable", true);
	}
	
	public static List<String> getTraceExchangeExcludeRoutes() {
		return getExcludeRoutes("trace.exchange.exclude.routes");		
	}
	
	public static List<String> getExcludeRoutes(String key) {
		List<String> ids = new ArrayList<>();
		ids.add(MonitoringRouteBuilder.ROUTE_ID);
		if (properties != null) {
			String value = properties.getProperty(key);
			if (trim(value) != null) {
				try {
					ids.addAll(split(value, ","));
				} catch (Throwable e) {

				}
			}
		}
		
		// default
		return ids;
	}
	
	public static int getTraceExchangeTimer() {
		if (properties != null) {
			String value = properties.getProperty("trace.exchange.timer.period");
			if (trim(value) != null) {
				try {
					return Integer.parseInt(value) * 1000;
				} catch (Throwable e) {

				}
			}
		}
		// default(sec)
		return 60 * 1000;
	}		
	
	public static String getTraceExchangeServerEndpoint() {
		if (properties != null) {
			return properties.getProperty("trace.exchange.server.endpoint");

		}
		return null;
	}
	
	public static File saveFile(File parent, String id, String body) throws Exception {
		if (body == null) {
			return null;
		}
		File file = new File(parent, id);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			IOUtils.write(body, fos);
			IOUtils.closeQuietly(fos);
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		}
		return file;

	}
	
	public static List<String> split(String string, String delimiter) {
		if (string == null) {
			return Collections.emptyList();
		}

		StringTokenizer tokenizer = new StringTokenizer(string, delimiter);
		List<String> segments = new ArrayList<String>();
		while (tokenizer.hasMoreTokens()) {
			String val = trim(tokenizer.nextToken().trim());
			if (val != null) {
				segments.add(val);
			}
		}
		return segments;
	}
	
	public static String trim(String s) {
		return trim(s, null);
	}

	public static String trim(String s, String defaultValue) {
		if (s != null) {
			s = s.trim();
			if (s.length() == 0) {
				s = null;
			}
		}
		return (s != null) ? s : defaultValue;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(TracerUtils.getWriteFileDir().getAbsolutePath());
	}

}
