package com.linkedcontact;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;

public class VCard {

	private String name;
	private String lastName;
	private String formattedName;
	private String email;
	private String cellPhone;
	private String otherPhone;
	private Context context;
	private long localId;

	public VCard(String N, String FN, String LN, String EMAIL, String CELL,
			String PHONE, Context c, long id) {
		name = N;
		formattedName = FN;
		email = EMAIL;
		cellPhone = CELL;
		otherPhone = PHONE;
		context = c;
		localId = id;
		lastName = LN;
	}

	public byte[] toByteArray() {
		byte[] bytes;
		String vCard = formattedName + " " + lastName + " " + name + " "
				+ email + " " + cellPhone + " " + otherPhone;
		bytes = vCard.getBytes();
		return bytes;
	}

	public File writeVCard() throws IOException {
		File f = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath());

		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(f + "/" + String.valueOf(localId) + ".vcf"));

		if (f.exists()) {
			String str = "BEGIN:VCARD\n" + "VERSION:2.1\n" + "FN:"
					+ formattedName + "\n" + "N:" + lastName + ";" + name
					+ "\n" + "EMAIL:" + email + "\n" + "TEL;CELL:" + cellPhone
					+ "\n" + "TEL;WORK:" + otherPhone + "\n" + "END:VCARD";
			out.write(str.getBytes());

			out.flush();
			out.close();
		}

		File outputFile = new File(f + "/" + cellPhone + ".vcf");
		return outputFile;
	}
}
