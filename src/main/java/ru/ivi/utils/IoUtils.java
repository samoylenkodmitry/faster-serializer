package ru.ivi.utils;

import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Pair;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SuppressWarnings({ "WeakerAccess", "ResultOfMethodCallIgnored", "resource", "StatementWithEmptyBody", "IOResourceOpenedButNotSafelyClosed", "SameParameterValue" })
public final class IoUtils {
	
	private static final char UNICODE_BOM = '\uFEFF';
	private static final int DEFAULT_BUFFER_SIZE = 8192;
	private static final ConcurrentLinkedQueue<byte[]> BYTES_POOL = new ConcurrentLinkedQueue<>();
	
	private IoUtils() {
	}
	
	public static void skipUnicodeBom(final Reader reader) throws IOException {
		reader.mark(1);
		
		final char[] possibleBom = new char[1];
		
		reader.read(possibleBom);
		
		if (possibleBom[0] != UNICODE_BOM) {
			reader.reset();
		}
	}
	
	@NonNull
	public static FasterByteArrayOutputStream readByteStream(
		InputStream input, final boolean needToClose
	) throws IOException {
		final FasterByteArrayOutputStream outputStream = new FasterByteArrayOutputStream();
		
		if (input != null) {
			try {
				if (!(input instanceof BufferedInputStream)) {
					input = new BufferedInputStream(input);
				}
				
				final byte[] buffer = obtainByteBuffer();
				try {
					
					int bytesRead;
					
					while ((bytesRead = input.read(buffer)) >= 0) {
						outputStream.write(buffer, 0, bytesRead);
					}
				} finally {
					releaseByteBuffer(buffer);
				}
			} finally {
				if (needToClose) {
					try {
						input.close();
					} catch (final IOException ignore) {
					}
				}
			}
		}
		
		return outputStream;
	}
	
	@NonNull
	public static byte[] readBytes(final InputStream input, final boolean needToClose) throws IOException {
		return readByteStream(input, needToClose).toByteArray();
	}
	
	public static void writeBytes(
		final byte[] bytes, OutputStream output, final boolean needToClose
	) throws IOException {
		if (output != null && !ArrayUtils.isEmpty(bytes)) {
			try {
				if (!(output instanceof BufferedOutputStream)) {
					output = new BufferedOutputStream(output);
				}
				
				output.write(bytes);
				output.flush();
			} finally {
				if (needToClose) {
					try {
						output.close();
					} catch (final IOException ignore) {
					}
				}
			}
		}
	}
	
	public static void readFake(final InputStream input, final boolean needToClose) {
		if (input != null) {
			try {
				final BufferedInputStream bufferedInput = new BufferedInputStream(input);
				
				final byte[] buffer = obtainByteBuffer();
				
				try {
					while (bufferedInput.read(buffer) >= 0) {
					}
				} catch (final IOException e) {
//					L.e(e);
				} finally {
					releaseByteBuffer(buffer);
				}
			} finally {
				if (needToClose) {
					try {
						input.close();
					} catch (final IOException ignore) {
					}
				}
			}
		}
	}
	
	@NonNull
	public static Collection<String> readStrings(
		@NonNull final InputStream input, final boolean needToClose
	) throws IOException {
		final Collection<String> strings = new ArrayList<>();
		
		try {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			
			skipUnicodeBom(reader);
			
			String str;
			
			while ((str = reader.readLine()) != null) {
				strings.add(str);
			}
		} finally {
			if (needToClose) {
				try {
					input.close();
				} catch (final IOException ignore) {
				}
			}
		}
		
		return strings;
	}
	
	public static String readStreamAndClose(final InputStream input, final String encoding) {
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				try (FasterByteArrayOutputStream output = readByteStream(input, true)) {
					return output.toString(encoding);
				}
			} else {
				FasterByteArrayOutputStream output = null;
				try {
					output = readByteStream(input, true);
					return output.toString(encoding);
				} finally {
					if (output != null) {
						try {
							output.close();
						} catch (final IOException ignore) {
						}
					}
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String readStream(final InputStream input, final String encoding) {
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				try (FasterByteArrayOutputStream output = readByteStream(input, false)) {
					return output.toString(encoding);
				}
			} else {
				final FasterByteArrayOutputStream output = readByteStream(input, false);
				return output.toString(encoding);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String readString(
		final InputStream input, final String separator, final boolean needToClose
	) throws IOException {
		final StringBuilder builder = new StringBuilder();
		
		if (!TextUtils.isEmpty(separator)) {
			for (final String str : readStrings(input, needToClose)) {
				builder
					.append(str)
					.append(separator);
			}
		} else {
			for (final String str : readStrings(input, needToClose)) {
				builder
					.append(str);
			}
		}
		
		return builder.toString();
	}
	
	public static Pair<ZipFile, ZipEntry> findFileInApk(final String filename, final String apkPath) {
		if (!TextUtils.isEmpty(filename)) {
			ZipFile zipFile = null;
			Pair<ZipFile, ZipEntry> resultZipEntryPair = null;
			try {
				zipFile = new ZipFile(apkPath);
				final Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();
				
				final String loweredFileName = filename.toLowerCase();
				while (zipFileEntries.hasMoreElements()) {
					final ZipEntry zipEntry = zipFileEntries.nextElement();
					
					if (zipEntry.getName().toLowerCase().contains(loweredFileName)) {
						
						resultZipEntryPair = new Pair<>(zipFile, zipEntry);
						return resultZipEntryPair;
					}
				}
			} catch (final IOException e) {
				e.printStackTrace();
			} finally {
				if (resultZipEntryPair == null && zipFile != null) {
					try {
						zipFile.close();
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unused")
	public static boolean isFileExistInApk(final String filename, final String apkPath) {
		final Pair<ZipFile, ZipEntry> zipEntryPair = findFileInApk(filename, apkPath);
		
		if (zipEntryPair != null && zipEntryPair.first != null) {
			try {
				zipEntryPair.first.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean writeStreamToFile(final InputStream input, final String fileName) throws IOException {
		try {
			//noinspection resource
			final BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(fileName));
			try {
				//write
				final byte[] buf = obtainByteBuffer();
				try {
					int read;
					while ((read = input.read(buf)) > 0) {
						fileOut.write(buf, 0, read);
					}
					
					fileOut.flush();
				} finally {
					releaseByteBuffer(buf);
				}
				
				return true;
			} finally {
				try {
					fileOut.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static boolean tryResetStream(final InputStream in) {
		try {
			in.reset();
			return true;
		} catch (final IOException e) {
			e.printStackTrace();
			Tracer.logCallStack(" failed reset");
		}
		
		return false;
	}
	
	private static void releaseByteBuffer(final byte[] bytes) {
		BYTES_POOL.add(bytes);
	}
	
	private static byte[] obtainByteBuffer() {
		final byte[] bytes = BYTES_POOL.poll();
		return bytes == null ? new byte[DEFAULT_BUFFER_SIZE] : bytes;
	}
}
