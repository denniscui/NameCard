package com.linkedcontact;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

public class ImgUtil {
	private Context context;
	public static int MAXBUFSIZE = 4096;

	public ImgUtil(Context c) {
		context = c;
	}

	public static boolean saveBufferToPhone(Context context, byte[] filebuf,
			String path) {

		try {
			File file = new File(path);
			FileOutputStream fileOutput = new FileOutputStream(file);
			int pos = 0;
			int len = MAXBUFSIZE;
			while (pos < filebuf.length) {
				if (pos + MAXBUFSIZE > filebuf.length)
					len = filebuf.length - pos;
				fileOutput.write(filebuf, pos, len);
				// Log.v("write file buffer="+len, filebuf.toString());
				pos += len;
			}
			fileOutput.close();
		} catch (Exception e) {
			Log.v("File IO Error", e.toString());
			return false;
		}

		// Log.v("done writing file", filename);
		return true;
	}

	public static String makePixFilename(CharSequence title, String extension) {
		String parentdir;
		// parentdir = "/sdcard/DCIM/camera";
		// File parentDirFile = new File(parentdir);
		// parentDirFile.mkdirs();

		parentdir = "/mnt/sdcard/media/images";
		File parentDirFile = new File(parentdir);
		parentDirFile.mkdirs();
		if (!parentDirFile.isDirectory())
			parentdir = "/sdcard";

		String filename = "";
		for (int i = 0; i < title.length(); i++) {
			if (Character.isLetterOrDigit(title.charAt(i))) {
				filename += title.charAt(i);
			}
		}
		String path = null;
		for (int i = 0; i < 100; i++) {
			String testPath;
			if (i > 0)
				testPath = parentdir + "/" + filename + i + extension;
			else
				testPath = parentdir + "/" + filename + extension;

			try {
				RandomAccessFile f = new RandomAccessFile(new File(testPath),
						"r");
			} catch (Exception e) {
				path = testPath;
				break;
			}
		}
		return path;
	}

	public byte[] getResizedImageData(Uri uri, int h, int w) {
		if (uri == null)
			return null;
		int widthLimit = w;
		int heightLimit = h;
		InputStream input = null;
		int mWidth = 0;
		int mHeight = 0;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			input = context.getContentResolver().openInputStream(uri);
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(input, null, opt);
			mWidth = opt.outWidth;
			mHeight = opt.outHeight;
		} catch (FileNotFoundException e) {
			Bitmap bm = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.defaulticon);
			bm.compress(CompressFormat.JPEG, 100, os);
			return os == null ? null : os.toByteArray();
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (IOException e) {
					// Ignore
					Log.e("IOException",
							"IOException caught while closing stream", e);
				}
			}
		}

		int scaleFactor = 1;
		while ((mWidth / scaleFactor > (int) (1.5 * widthLimit))
				|| (mHeight / scaleFactor > (int) (1.5 * heightLimit))) {
			scaleFactor *= 2;
		}
		try {

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = scaleFactor;
			input = context.getContentResolver().openInputStream(uri);
			Bitmap b = null;
			// int quality = MessageUtils.IMAGE_COMPRESSION_QUALITY;
			try {
				b = BitmapFactory.decodeStream(input, null, options);
				if (b == null) {
					return null;
				}
				float scalex = ((float) options.outWidth) / widthLimit;
				float scaley = ((float) options.outHeight) / heightLimit;
				float scale = 1.0f;
				if (scalex > scaley)
					scale = scalex;
				else
					scale = scaley;
				if (scale > 1.0f) {
					// The decoder does not support the inSampleSize option.
					// Scale the bitmap using Bitmap library.
					int scaledWidth = (int) (options.outWidth / scale + 0.5f);
					int scaledHeight = (int) (options.outHeight / scale + 0.5f);

					// if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
					Log.v("TAG", "getResizedImageData: retry scaling using "
							+ "Bitmap.createScaledBitmap: w=" + scaledWidth
							+ ", h=" + scaledHeight);
					// }

					b = Bitmap.createScaledBitmap(b, scaledWidth, scaledHeight,
							false);
					if (b == null) {
						return null;
					}
				}

			} catch (java.lang.OutOfMemoryError e) {
				Log.w("TAG",
						"getResizedImageData - image too big (OutOfMemoryError), will try "
								+ " with smaller scale factor, cur scale factor: "
								+ scaleFactor);
				// fall through and keep trying with a smaller scale factor.
			}
			b.compress(CompressFormat.JPEG, 80, os);
			return os == null ? null : os.toByteArray();
		} catch (FileNotFoundException e) {
			Log.e("TAG", e.getMessage(), e);
			return null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					Log.e("TAG", e.getMessage(), e);
				}
			}
		}
	}
}
