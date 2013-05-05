package com.linkedcontact;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.telephony.PhoneNumberUtils;

public class PhoneNumUtil {

	public static boolean isNanpNumber(String s) {
		boolean isValid = false;

		String expression = "^[1]?[2-9]\\d{2}[2-9]\\d{6}$";
		CharSequence inputStr = s;
		Pattern pattern = Pattern.compile(expression);
		Matcher matcher = pattern.matcher(inputStr);
		if (matcher.matches()) {
			isValid = true;
		}
		return isValid;
	}

	public static String processNumber(String s) {
		String receiverNumber = s;
		receiverNumber = receiverNumber.replaceAll(" ", "");

		for (char c : receiverNumber.toCharArray()) {
			if (!PhoneNumberUtils.isISODigit(c)) {
				receiverNumber = receiverNumber.replace(c + "", "");
			}
		}

		if (PhoneNumUtil.isNanpNumber(receiverNumber)) {
			// PhoneNumberUtils.formatNanpNumber(receiver.getText());
			// receiverNumber = receiver.getText().toString();

			if (receiverNumber.length() == 11 && receiverNumber.startsWith("1"))
				receiverNumber = receiverNumber.substring(1);
		}

		return receiverNumber;
	}
	
	public static boolean isValidPhoneNum(String phoneNumber) {
		boolean isValid = false;
		String expression = "[0-9, ]*";
		CharSequence inputStr = phoneNumber;
		Pattern pattern = Pattern.compile(expression);
		Matcher matcher = pattern.matcher(inputStr);
		if (matcher.matches()
				&& PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
			isValid = true;
		}
		return isValid;
	}

}
