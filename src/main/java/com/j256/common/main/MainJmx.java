package com.j256.common.main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import com.j256.simplejmx.common.JmxAttributeMethod;
import com.j256.simplejmx.common.JmxOperation;
import com.j256.simplejmx.common.JmxResource;

/**
 * Used to export various settings from the Main class that cannot be loaded from Spring.
 * 
 * @author graywatson
 */
@JmxResource(domainName = "j256.common", beanName = "Main", description = "Main Jmx class")
public class MainJmx {

	private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");

	private final long start = System.currentTimeMillis();
	private CountDownLatch shutdownLatch = new CountDownLatch(1);

	@JmxAttributeMethod(description = "Command line arguments")
	public String[] getArgs() {
		return Main.getInstance().getArgs();
	}

	@JmxAttributeMethod(description = "Spring configuration paths")
	public String[] getConfigPaths() {
		return Main.getInstance().getConfigPaths();
	}

	@JmxAttributeMethod(description = "Start time in millis.")
	public long getStartTimeMillis() {
		return start;
	}

	@JmxAttributeMethod(description = "Start time string")
	public String getStartTimeString() {
		return millisTimeToStart(start);
	}

	@JmxAttributeMethod(description = "Current time in millis.")
	public long getCurrentTimeMillis() {
		return System.currentTimeMillis();
	}

	@JmxAttributeMethod(description = "Current time string.")
	public String getCurrentTimeString() {
		return millisTimeToStart(System.currentTimeMillis());
	}

	@JmxAttributeMethod(description = "Run time in millis.")
	public long getRunTimeMillis() {
		return System.currentTimeMillis() - start;
	}

	@JmxAttributeMethod(description = "Run time string.")
	public String getRunTimeString() {
		Duration duration = new Duration(getRunTimeMillis());
		Period period = duration.toPeriod(PeriodType.dayTime());
		return String.format("%d+%02d:%02d:%02d.%03d", period.getDays(), period.getHours(), period.getMinutes(),
				period.getSeconds(), period.getMillis());
	}

	@JmxOperation(description = "Stop the application")
	public void shutdown() {
		shutdownLatch.countDown();
	}

	/**
	 * Wait for shutdown.
	 */
	public void waitForShutdown() {
		try {
			shutdownLatch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private String millisTimeToStart(long millis) {
		return dateFormat.format(new Date(millis));
	}
}
