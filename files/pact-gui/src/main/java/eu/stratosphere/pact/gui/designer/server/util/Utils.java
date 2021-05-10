package eu.stratosphere.pact.gui.designer.server.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

/**
 * Many little helper functions needed on server side (during pact program
 * compilation and file up/download)
 * 
 * @author MathiasNitzsche@gmail.com
 */
public class Utils {
	private static Logger Log = Logger.getLogger("");

	public static String loadFileContent(String filePath) throws IOException {
		File file = new File(filePath);
		return loadFileContent(file);
	}

	/**
	 * Load a text file contents as a <code>String<code>.
	 * This method does not perform enconding conversions
	 * 
	 * @param file
	 *            The input file
	 * @return The file contents as a <code>String</code>
	 * @exception IOException
	 *                IO Error
	 */

	public static String loadFileContent(File file) throws IOException {
		// if (log != null) {
		// log.info("loadFileContent " + file.getAbsolutePath());
		// }
		if (file.exists()) {
			int len;
			char[] chr = new char[4096];
			final StringBuffer buffer = new StringBuffer();
			final FileReader reader = new FileReader(file);
			try {
				while ((len = reader.read(chr)) > 0) {
					buffer.append(chr, 0, len);
				}
			} finally {
				reader.close();
			}
			return buffer.toString();
		} else {
			// if (log != null) {
			// log.error("file " + file.getAbsolutePath() + " doesn't exist");
			// }
			return null;
		}
	}

	public static String getApplicationPath(HttpServletRequest request, String subPath) {
		String path = request.getSession().getServletContext().getRealPath(subPath);
		path = path.replace("\\", "/");
		path = path.replace("\\/+", "/");
		return path;
	}

	public static void writeBinaryFile(String fileName, byte[] content) throws IOException {
		BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(content));
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fileName));
		byte[] ioBuf = new byte[4096];
		int bytesRead;
		while ((bytesRead = in.read(ioBuf)) != -1)
			out.write(ioBuf, 0, bytesRead);
		out.close();
		in.close();
	}

	public static File createAndAppendFile(String path, String fileContent, boolean append) {
		try {
			FileWriter fstream = new FileWriter(path, append);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(fileContent);
			out.close(); // Close the output stream
		} catch (IOException e) {
			// no handling
		}
		return new File(path);
	}

	public static File createFile(String path, String fileContent) {
		return createAndAppendFile(path, fileContent, false);
	}

	public static String formatPath(String absolutePath) {
		if (absolutePath != null) {
			absolutePath = absolutePath.replace("\\", "/");
			absolutePath = absolutePath.replaceAll("\\/+", "/");
		}
		return absolutePath;
	}

	public static File createTempDirectory(String prefix) {
		File tempDir = null;

		try {
			tempDir = File.createTempFile(prefix, Long.toString(System.nanoTime()));

			if (!(tempDir.delete())) {
				throw new IOException("Could not delete temp file: " + tempDir.getAbsolutePath());
			}

			if (!(tempDir.mkdir())) {
				throw new IOException("Could not create temp directory: " + tempDir.getAbsolutePath());
			}
		} catch (IOException e) {

		}

		return tempDir;
	}

	public static boolean delete(String dir) {
		return delete(new File(dir));
	}

	// recursively delete a folder / file
	// from: http://www.exampledepot.com/egs/java.io/DeleteDir.html
	public static boolean delete(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = delete(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}

	public static String inputStreamToString(InputStream in) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[1024];
		for (int n; (n = in.read(b)) != -1;) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
	}

	public static byte[] inputStreamToByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[4096]; // 16384

		while ((nRead = in.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		buffer.flush();

		return buffer.toByteArray();
	}

	private static int BUFSIZE = 4096;

	public static void streamFileToBrowser(File file, HttpServletResponse response) {
		if (file != null && file.exists()) {
			try {
				ServletOutputStream outputStream = response.getOutputStream();
				if (file.isFile()) {
					response.setContentType(new MimetypesFileTypeMap().getContentType(file));
					response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
					response.setContentLength((int) file.length());
					copy(file, outputStream);
				} else {
					// C:/Users/Mathias/AppData/Local/Temp/pact_gui_1071182028551481295337667810873221
					response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + ".zip\"");
					response.setContentType("application/zip");
					zipFolderToOutputStream(file, outputStream);
				}
				outputStream.flush();
				outputStream.close();
			} catch (IOException ex) {
				Log.warning("Error in file streaming: " + ex.getMessage());
				sendError(response, HttpServletResponse.SC_NOT_FOUND);
			}
		} else {
			sendError(response, HttpServletResponse.SC_NOT_FOUND);
		}
	}

	// http://stackoverflow.com/questions/1399126/java-util-zip-recreating-directory-structure
	public static void zipFolderToOutputStream(File directory, OutputStream out) throws IOException {
		URI base = directory.toURI();
		Deque<File> queue = new LinkedList<File>();
		queue.push(directory);
		Closeable res = out;
		try {
			ZipOutputStream zout = new ZipOutputStream(out);
			res = zout;
			while (!queue.isEmpty()) {
				directory = queue.pop();
				for (File kid : directory.listFiles()) {
					String name = base.relativize(kid.toURI()).getPath();
					if (kid.isDirectory()) {
						queue.push(kid);
						name = name.endsWith("/") ? name : name + "/";
						zout.putNextEntry(new ZipEntry(name));
					} else {
						zout.putNextEntry(new ZipEntry(name));
						copy(kid, zout);
						zout.closeEntry();
					}
				}
			}
		} finally {
			res.close();
		}
	}

	private static void copy(File file, OutputStream out) throws IOException {
		InputStream in = new FileInputStream(file);
		try {
			copy(in, out);
		} finally {
			in.close();
		}
	}

	private static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[BUFSIZE];
		int readCount = 0;
		while (true) {
			readCount = in.read(buffer);
			if (readCount < 0) {
				break;
			}
			out.write(buffer, 0, readCount);
		}
	}

	public static void sendError(HttpServletResponse response, int httpCode) {
		try {
			response.sendError(httpCode);
		} catch (IOException ex) {
			Log.warning(ex.getMessage());
		}
	}

	public static String base64Encode(byte[] bytes) {
		return DatatypeConverter.printBase64Binary(bytes);
	}

	public static byte[] base64Decode(String string) {
		return DatatypeConverter.parseBase64Binary(string);
	}
}
