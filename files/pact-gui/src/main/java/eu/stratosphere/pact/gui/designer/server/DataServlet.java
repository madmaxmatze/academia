package eu.stratosphere.pact.gui.designer.server;

import java.beans.ExceptionListener;
import java.beans.PersistenceDelegate;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import eu.stratosphere.pact.gui.designer.server.util.ByteArrayPersistenceDelegate;
import eu.stratosphere.pact.gui.designer.server.util.Utils;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;
import eu.stratosphere.pact.gui.designer.shared.model.JarFile;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgramManager;

/**
 * This is the only "real" servlet of the pact gui - used to up- and download
 * files
 * 
 * @author MathiasNitzsche@gmail.com
 * 
 */
public class DataServlet extends HttpServlet {
	private static final long serialVersionUID = 3145103445845417180L;
	private static Logger Log = Logger.getLogger("");

	/**
	 * This method handles all file uploaded (eg upload of a jar file, or
	 * opening a pactProgram.xml)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Log.info("DataServlet->doPost: FileUpload");

		// upload type is a hidden field in all upload forms to only handle .xml
		// files from the pactProgram upload form and to only handle .jar files
		// from the jar upload form
		String uploadType = null;

		// this is the actuall uploaded file type (is retrieved via
		// uploadObject.getContentType())
		String fileType = null;

		// file name of the uploaded file
		String fileName = null;

		// in case a jar file is uploaded - this variable saves the content
		byte[] jarFileContent = null;

		// in case a xml file is uploaded - this variable saves the deserialized
		// pact program object
		PactProgram pactProgram = null;

		// create ServletFileUpload object
		ServletFileUpload upload = new ServletFileUpload();

		// session of logged in user is needed
		HttpSession session = request.getSession();
		if (session == null) {
			Log.severe("Session not existing");
			throw new ServletException("NO session");
		} else {
			try {
				// iterate over all uploaded items, either they are files or
				// form fields
				FileItemIterator iterator = upload.getItemIterator(request);
				while (iterator.hasNext()) {
					FileItemStream item = iterator.next();
					InputStream inputStream = item.openStream();

					Log.info("Iterate over uploaded items. CurrentItem: contentType=" + item.getContentType()
							+ ", isFormField=" + item.isFormField());

					// the current field in the iteration is either a textual
					// form field
					if (item.isFormField()) {
						// the one and only form field which is important is
						// "uploadType"
						if ("uploadType".equalsIgnoreCase(item.getFieldName())) {
							Log.info("uploadType-field found");
							try {
								// save uploadType field value. Important to
								// later decide what to do with the file upload
								uploadType = Utils.inputStreamToString(inputStream);
								Log.info("uploadType value = '" + uploadType + "'");
							} catch (Exception e) {
								Log.severe("inputStreamToString: " + e.getMessage());
							}
						}

						// or a file item
					} else {
						Log.info("file item found");

						fileType = item.getContentType();
						Log.info("file type: " + fileType);

						fileName = item.getName();
						Log.info("File name: " + fileName);

						// only handle 2 sorts of files: .xml and .jar
						// fileType equals null with GoogleChrome Browser
						// fileType != null && fileType.contains("jar")

						// if a xml file (a serialized pact program)
						if (fileName != null && fileName.contains("xml")) {
							// deserialize from xml to pactProgram
							XMLDecoder decoder = null;
							try {
								Log.info("Try to deserialize XML Stream with XMLDecoder to create a pactProgram object");
								decoder = new XMLDecoder(inputStream);
								// special Exception Listener needed
								decoder.setExceptionListener(new ExceptionListener() {
									public void exceptionThrown(Exception ex) {
										Log.warning("Error in decoder.readObject: " + ex.getMessage());
										ex.printStackTrace();
									}
								});

								// DO the actual deserialisation
								pactProgram = (PactProgram) decoder.readObject();

								// if the deserialisation worked and a
								// pactProgram object was created, do embedded
								// jar file special handling
								// this is needed to avoid sending MegaBytes of
								// jar binaries to the client
								if (pactProgram != null) {
									Log.info("PactProgram loaded - now handle " + pactProgram.getJarFiles().size()
											+ " embedded jar file(s)");

									// iterate over all embedded jar files
									for (JarFile jarFile : pactProgram.getJarFiles().values()) {
										boolean jarSuccessfullySavedToSession = false;

										// test if content is existing
										if (jarFile.getContent() == null) {
											Log.severe("Jar file '" + jarFile.getName() + "' has no content");
										} else {
											Log.info("File found - hash:" + jarFile.getHash() + ", contentSize:"
													+ jarFile.getContent().length + ", name:" + jarFile.getName());

											// file content seems fine - so save
											// to session
											session.setAttribute(jarFile.getHash(), jarFile.getContent());
											// and set null in pactProgram
											// Object (to decrease object size,
											// when sending to client)
											jarFile.setContent(null);
											jarSuccessfullySavedToSession = true;
										}

										// if this did not work - probably the
										// whole pact program is corrupt - send
										// error
										if (!jarSuccessfullySavedToSession) {
											throw new Exception("Error loading jar file '" + jarFile.getName()
													+ "' when opening '" + fileName + "'");
										}
									}
								}
							} catch (Exception e) {
								Log.severe("Error during File decoding: " + e.getMessage());
								e.printStackTrace();
							} finally {
								// in every case close decoder afterwards - to
								// then close inputstream
								if (decoder != null) {
									decoder.close();
								}
							}

							// if the file item is a jar file
							// fileType=null with GoogleChrome Browser
							// fileType != null && fileType.contains("jar")
						} else if (fileName != null && fileName.contains("jar")) {
							Log.info("Jar file upload");
							jarFileContent = Utils.inputStreamToByteArray(inputStream);
							if (jarFileContent == null) {
								Log.severe("Error reading reading byte[] from input stream");
							}

							// the file type needs to be jar or xml - otherwise
							// send exception
						} else {
							Log.severe("Unknown file type uploaded");
							throw new ServletException("Unknown file type uploaded");
						}
					}
				}

				// error handling FileUpload
			} catch (FileUploadException e) {
				Log.severe("Error in Fileupload: " + e.getMessage());
				throw new ServletException("Error upload the file");
			}

			// /////////////////////////////////////////////////////////
			// after the input is analyzed and saved to variables - prepare
			// response for client
			String responseStr = "";

			// in case the uploadType is "pactProgram" (as .xml)
			if ("pactProgram".equalsIgnoreCase(uploadType) && pactProgram != null) {
				// save loaded pact program to session
				Log.info("PactProgram loaded - save jar to session with key: '" + "uploadedPactProgram"
						+ pactProgram.hashCode() + "'");
				session.setAttribute("uploadedPactProgram" + pactProgram.hashCode(), pactProgram);
				responseStr += "{\"programHash\" : \"" + pactProgram.hashCode() + "\"}";

				// in case the uploadType is "jar"
			} else if ("jar".equalsIgnoreCase(uploadType) && jarFileContent != null) {
				// save jar file to session - and tell client under which key
				String fileHash = fileName + jarFileContent.hashCode();
				session.setAttribute(fileHash, jarFileContent);
				Log.info("jar file '" + fileName + "' (" + jarFileContent.length + " bytes) saved to session:");

				responseStr += "{";
				responseStr += "\"fileHash\" : \"" + fileHash + "\",";
				responseStr += "\"fileName\" : \"" + fileName + "\"";
				responseStr += "}";
			}

			// send response to client
			Log.info("response str created on server and send to client '" + responseStr + "'");
			response.getWriter().write(responseStr);
		}
	}

	/**
	 * Download files - either a pactProgram as xml or all files/folder created
	 * during pactProgram compilation
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// user session needed
		HttpSession session = request.getSession();
		if (session == null) {
			Log.warning("session is null");
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} else {
			// if a file is requested (file/folde created during pactProgram
			// compilation)
			String file = request.getParameter("file");
			if (file != null) {
				if (file.contains("pact_gui")) {
					// this file HAS TO BE in a folder which contains "pact_gui"
					// ... to prevent at least a little bit that all files can
					// be are requested
					// TODO: INCREASE SECURITY
					Utils.streamFileToBrowser(new File(file), response);
				} else {
					Utils.sendError(response, HttpServletResponse.SC_FORBIDDEN);
				}

			} else {
				Log.info("Servlet for pact Program download");

				// if a whole pactProgram is requested
				String type = request.getParameter("type");
				Log.info("type: " + type);

				String pactId = request.getParameter("pactid");
				Log.info("pactId: " + pactId);

				String programManager = request.getParameter("programManager");
				Log.info("programManager: " + programManager);

				// get program manager from session
				// was "uploaded" before via ajax
				PactProgramManager pactProgramManager = (PactProgramManager) session.getAttribute("pactProgramManager"
						+ programManager);
				if (pactProgramManager != null) {
					// if manager exists - check for the single pactProgram
					PactProgram pactProgram = pactProgramManager.getPactProgramById(Integer.valueOf(pactId));
					if (pactProgram != null) {
						// for now the pact program can only be downloaded via
						// xml
						// this might change in the future
						if ("xml".equals(type)) {
							Log.info("Program to download was found: " + pactProgram);

							// prepare response
							response.setContentType("text/xml");
							// response.setContentLength( (int)f.length() );
							response.setHeader("Content-Disposition", "attachment; filename=\"" + pactProgram.getName()
									+ ".xml\"");

							// add jar files to pact program which are only
							// existing in session and not client side
							Log.info("embbed " + pactProgram.getJarFiles().size() + " jar files");
							for (JarFile jarFile : pactProgram.getJarFiles().values()) {
								Log.info("Add Jar File '" + jarFile.getName() + "'");
								// get file content from session
								byte[] jarFileContent = (byte[]) session.getAttribute(jarFile.getHash());

								// if file content not found in session -
								// something is very wrong - send error
								if (jarFileContent == null) {
									Log.severe("Jar file with hash '" + jarFile.getHash() + "' not found in session");
									response.sendError(HttpServletResponse.SC_NOT_FOUND);
								} else {
									// if file is found in session - add content
									// to jarFile object in pact program
									Log.info("Fill Jar file '" + jarFile.getName() + "' in pactProgram with "
											+ jarFileContent.length + "bytes");
									jarFile.setContent(jarFileContent);
								}
							}

							// use a special handling for byte[] of jar files in
							// pact program during xml serialisation
							final ByteArrayPersistenceDelegate del = new ByteArrayPersistenceDelegate();

							// serialize file to xml
							// http://java.sun.com/products/jfc/tsc/articles/persistence4/#transient
							ServletOutputStream outputStream = response.getOutputStream();
							XMLEncoder encoder = new XMLEncoder(outputStream) {
								public PersistenceDelegate getPersistenceDelegate(Class<?> type) {
									if (type == byte[].class) {
										return del;
									} else {
										return super.getPersistenceDelegate(type);
									}
								}
							};
							encoder.setExceptionListener(new ExceptionListener() {
								public void exceptionThrown(Exception ex) {
									Log.warning("Error in encoder.writeObject(pactProgram): " + ex.getMessage());
									ex.printStackTrace();
								}
							});

							// DO the serialization to servlet output stream
							encoder.writeObject(pactProgram);
							encoder.close();

							// close output stream
							outputStream.flush();
							outputStream.close();
						}
					} else {
						Log.warning("pactProgram is null");
						response.sendError(HttpServletResponse.SC_NOT_FOUND);
					}
				} else {
					Log.warning("pactProgramManager is null");
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				}
			}
		}
	}
}