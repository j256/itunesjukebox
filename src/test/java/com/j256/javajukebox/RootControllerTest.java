package com.j256.javajukebox;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Test;

public class RootControllerTest {

	@Test
	public void test() {
		InputStream stream = getClass().getResourceAsStream("index.html");
		assertNotNull(stream);
	}
}
