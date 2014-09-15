package com.ikantech.xmppsupport.util;

import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtils {

	private Properties mProperties;

	private static PropertiesUtils mPropertiesUtils = null;

	public static PropertiesUtils getInstance() {
		if (mPropertiesUtils == null) {
			mPropertiesUtils = new PropertiesUtils();
		}
		return mPropertiesUtils;
	}

	private PropertiesUtils() {
		InputStream inputStream = null;
		mProperties = new Properties();
		try {
			inputStream = getClass().getResourceAsStream(
					"/res/raw/config.properties");
			mProperties.load(inputStream);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
					inputStream = null;
				} catch (Exception e2) {
				}
			}
		}
	}

	public Properties getProperties() {
		return mProperties;
	}
}
