package com.linkedcontact;

public class StringUtils {

	public static String removeReturnTrim(String s) {
		String str = s.replace("\n", " ");
		return str.trim();
	}

}
