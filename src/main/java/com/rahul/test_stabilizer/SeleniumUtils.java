package com.rahul.test_stabilizer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This utility class contains methods which can be used during execution of
 * Selenium functionalities which exhibit a "flaky" behavior. These method will
 * try to execute a particular action until a certain condition is met.
 *
 * <p>
 * These methods should be used when working with selenium functionalities that
 * are inconsistent. Example of such "flaky" functionalities can be a selenium
 * click and double-click, which may or may not work as expected.
 *
 * <p>
 * Wrapping such actions in these utility method help to eliminate such
 * behavior. The failure resulted from running test cases containing such
 * "flaky" actions will be more genuine.
 *
 * <p>
 * Refer to SeleniumUtilsTest for usage details
 */
public class SeleniumUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumUtils.class);

	/**
	 * This utility method helps to perform the "task" until the "isTaskCompleted"
	 * condition is met. The repetition will happen "noOfAttempts" times and after
	 * each attempt it will wait for "waitInterval" to check the condition.
	 *
	 * @param webDriver
	 * @param task
	 * @param isTaskCompleted
	 * @param noOfAttempts
	 * @param waitInterval
	 * @param waitIntervalTimeUnit
	 */
	public static <T extends WebDriver> void performTaskUntil(T webDriver, Consumer<T> task,
			Function<T, Boolean> isTaskCompleted, int noOfAttempts, long waitInterval, TimeUnit waitIntervalTimeUnit) {

		boolean performedTaskSuccessfully = false;
		int retryCount = 1;

		while (!performedTaskSuccessfully && retryCount <= noOfAttempts) {
			try {
				task.accept(webDriver);
				new FluentWait<>(webDriver).withTimeout(waitInterval, waitIntervalTimeUnit).until(isTaskCompleted);
				performedTaskSuccessfully = true;
			} catch (Throwable e) {
				performedTaskSuccessfully = false;
				LOGGER.debug("Failed in attempt " + retryCount, e);
			}
			retryCount++;
		}

		if (!performedTaskSuccessfully) {
			throw new TimeoutException("Task not completed in " + retryCount);
		}
	}

	/**
	 * This utility method will help you perform a task successfully, until an
	 * exception is thrown. It will throw TimeoutException when the task is not
	 * performed successfully within the "timeoutIntervalInSeconds"
	 *
	 * @param webDriver
	 * @param task
	 * @param poolingTimeInSeconds
	 * @param timeoutIntervalInSeconds
	 */
	public static <T extends WebDriver> void performTaskUntilNoException(T webDriver, Consumer<T> task,
			long poolingTimeInSeconds, long timeoutIntervalInSeconds) {

		new FluentWait<>(webDriver).withTimeout(Duration.ofSeconds(timeoutIntervalInSeconds))
				.pollingEvery(Duration.ofSeconds(poolingTimeInSeconds)).until(driver -> {
					boolean success = false;
					try {
						task.accept(webDriver);
						success = true;
					} catch (Throwable error) {
						LOGGER.debug("Failed to perform the task successfully", error);
						success = false;
					}
					return success;
				});
	}
}
