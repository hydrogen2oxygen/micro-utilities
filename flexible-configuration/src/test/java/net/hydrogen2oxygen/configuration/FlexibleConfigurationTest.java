package net.hydrogen2oxygen.configuration;

import org.junit.Assert;
import org.junit.Test;

public class FlexibleConfigurationTest {

	@Test
	public void test() throws Exception {

		String[] args = { "test123", "-rawDataFolder=rawFolder" };
		FlexibleConfiguration argumentConfiguration = new FlexibleConfiguration(args);

		Assert.assertEquals("rawFolder", argumentConfiguration.get("rawDataFolder"));

		class TestClass {

			private String rawDataFolder;

			@SuppressWarnings("unused")
			public void setRawDataFolder(String r) {
				rawDataFolder = r;
			}

			public String getRawDataFolder() {
				return rawDataFolder;
			}
		}

		TestClass testClass = new TestClass();

		argumentConfiguration.initializeMemberVariables(testClass);

		Assert.assertEquals("rawFolder", testClass.getRawDataFolder());

		FlexibleConfiguration configurationFromFile = new FlexibleConfiguration("src/test/resources/config.properties");
		Assert.assertEquals("target", configurationFromFile.get("packageFolder"));
		System.out.println(configurationFromFile);
	}
}
