package net.hydrogen2oxygen.configuration;


import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FlexibleConfiguration {

	private Map<String, String> keyValues = new HashMap<>();

	public FlexibleConfiguration(String configurationFile) {

		try {
			Properties prop = new Properties();
			InputStream input = new FileInputStream(configurationFile);
			prop.load(input);

			for (Object key : prop.keySet()) {
				keyValues.put((String) key, prop.getProperty((String) key));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public FlexibleConfiguration(String[] args) throws IllegalArgumentException, IllegalAccessException {

		initOption(args);
	}

	public String get(String key) {

		return keyValues.get(key);
	}

	private void initOption(String[] args) throws IllegalArgumentException, IllegalAccessException {

		for (String argument : args) {

			if (argument.contains("-") && argument.contains("=")) {

				String part[] = argument.split("=");

				keyValues.put(part[0].replaceAll("-", "").trim(), part[1].trim());
			}
		}

		for (Field field : this.getClass().getDeclaredFields()) {

			if (keyValues.containsKey(field.getName())) {

				field.set(this, keyValues.get(field.getName()));
			}
		}
	}

	public void initializeMemberVariables(Object object) throws Exception {

		for (Field field : object.getClass().asSubclass(object.getClass()).getDeclaredFields()) {

			String value = keyValues.get(field.getName());

			if (value != null) {
				Method setterMethod = object.getClass().getMethod("set" + firstCharacterBig(field.getName()),
				        String.class);
				setterMethod.invoke(object, value);
			}
		}
	}

	private String firstCharacterBig(String name) {

		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}
}