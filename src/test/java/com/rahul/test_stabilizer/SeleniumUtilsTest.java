package com.rahul.test_stabilizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

class SeleniumUtilsTest {

	private static WebDriver webDriver;

	@BeforeAll
	public static void setup() {

		Properties config = new Properties();
		try (InputStream input = new FileInputStream("src/test/resources/config.properties")) {
			config.load(input);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.setProperty("webdriver.chrome.driver", config.getProperty("chrome_driver_path"));
		webDriver = new ChromeDriver();

	}

	@AfterAll
	public static void cleanUp() {
		webDriver.quit();
	}

	@Test
	void perfomTask_untilConditionMet() {

		final AtomicInteger counter = new AtomicInteger(0);
		SeleniumUtils.performTaskUntil(this.webDriver, driver -> counter.incrementAndGet(),
				driver -> counter.get() == 4, 4, 100, TimeUnit.MILLISECONDS);

		assertEquals(counter.get(), 4);
	}

	@Test
	void throwTimeoutException_whenConditionNotMet_InNoOfAttempts() {

		final AtomicInteger counter = new AtomicInteger(0);
		assertThrows(TimeoutException.class, () -> SeleniumUtils.performTaskUntil(this.webDriver,
				driver -> counter.incrementAndGet(), driver -> counter.get() == 4, 3, 100, TimeUnit.MILLISECONDS));
	}

	@Test
	void performTask_untilSuccessfull() {

		final AtomicInteger counter = new AtomicInteger(0);
		final AtomicBoolean isTaskExecuted = new AtomicBoolean(false);

		SeleniumUtils.performTaskUntilNoException(this.webDriver, driver -> {
			if (counter.get() < 3) {
				counter.incrementAndGet();
				throw new Error();
			}
			isTaskExecuted.set(true);
		}, 1L, 10L);

		assertEquals(true, isTaskExecuted.get());
	}

	@Test
	void shouldThrowTimeoutException_IfTaskIsNotCompletedWithinTimeout() {

		assertThrows(TimeoutException.class, () -> {
			SeleniumUtils.performTaskUntilNoException(this.webDriver, driver -> {
				throw new Error();
			}, 1L, 3L);
		});
	}
}
