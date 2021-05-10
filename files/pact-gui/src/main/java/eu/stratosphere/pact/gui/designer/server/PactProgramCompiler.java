package eu.stratosphere.pact.gui.designer.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import eu.stratosphere.pact.gui.designer.server.util.CustomLogger;
import eu.stratosphere.pact.gui.designer.server.util.StreamCatcher;
import eu.stratosphere.pact.gui.designer.server.util.Utils;
import eu.stratosphere.pact.gui.designer.shared.model.Connection;
import eu.stratosphere.pact.gui.designer.shared.model.JarFile;
import eu.stratosphere.pact.gui.designer.shared.model.Pact;
import eu.stratosphere.pact.gui.designer.shared.model.PactProgram;
import eu.stratosphere.pact.gui.designer.shared.model.PactType;
import eu.stratosphere.pact.gui.designer.shared.model.helper.PactProgramCompilerResult;

/**
 * Compile a given pactProgram model into a valid .jar file
 * 
 * @author MathiasNitzsche@gmail.com
 */
public class PactProgramCompiler {
	/*
	 * Custom logger to file, system console and client
	 */
	private CustomLogger Log = new CustomLogger();

	/*
	 * List of files created during compilation - send to client as result
	 */
	// private LinkedHashMap<String, String> files = new LinkedHashMap<String,
	// String>();
	private PactProgramCompilerResult result = null;

	/*
	 * runtime objects to perform javac and jar on command line
	 */
	private Runtime runtime = null;

	private PactProgram pactProgram = null;
	private ServletContext servletContext = null;
	private HttpSession session = null;

	/**
	 * Constructor
	 */
	public PactProgramCompiler(PactProgram pactProgram, ServletContext servletContext, HttpSession session) {
		this.pactProgram = pactProgram;
		this.servletContext = servletContext;
		this.session = session;
	}

	public PactProgramCompilerResult getResults() {
		if (result == null) {
			result = new PactProgramCompilerResult();
			result.setPactProgramId(pactProgram.getId());
			result.wasSuccessFul(false);

			try {
				// should never happen
				if (pactProgram == null) {
					Log.error("Given pactProgram object is null. Compilation aborted");
				} else if (servletContext == null) {
					Log.error("Given servletContext object is null. Compilation aborted");
				} else if (session == null) {
					Log.error("Given session object is null. Compilation aborted");
				} else {
					// first validate pact program (basic static analysis of
					// graph
					// and
					// user functions)
					ArrayList<Error> validationErrors = pactProgram.getValidationErrors();

					// if no error - perform compilation
					if (validationErrors.size() == 0) {
						result.wasSuccessFul(compile(session, servletContext, pactProgram));
					} else {
						// otherwise output errors to log
						for (Error error : pactProgram.getValidationErrors()) {
							Log.error(error.getMessage());
						}
					}
				}
			} catch (Exception ex) {
				// no exception should make it ever up to this try catch
				// but if - something bad, went wrong
				Log.error("Major UNEXPECTED and UNHANDLED error during compilation: " + ex.getMessage());

				Writer result = new StringWriter();
				PrintWriter printWriter = new PrintWriter(result);
				ex.printStackTrace(printWriter);
				Log.error("Stacktrace" + result.toString());
			}

			// output a nice final message to the log
			if (result.wasSuccessFul()) {
				Log.info("Pact porgram compilation finished successfully.");
			} else {
				Log.error("Compilation of the pact porgram was aborted, due to an error.");
			}
		}

		if (result.getFiles().size() == 0) {
			result.addFileToResultSet("/validate.log", Log.getOutput());
		} else {
			result.addFileToResultSet(Log.getLogFilePath(), Log.getOutput());
		}

		return result;
	}

	/**
	 * Actually perform the compilation of a pactProgram model object into a
	 * real jar file. Compile method is already performed in constructor. Should
	 * probably be changed at some point.
	 * 
	 * @param session
	 *            - in pactProgram embedded jar files are stored in session
	 * @param servletContext
	 *            - Servlet contect is needed to find jars embedded into this
	 *            application
	 * @param pactProgram
	 *            - given pactProgram model
	 */
	private boolean compile(HttpSession session, ServletContext servletContext, PactProgram pactProgram) {
		// temp folder for compilation
		String tempFolderPath = null;

		// at this point
		if (pactProgram == null) {
			Log.error("Given pactProgram object is null. Compilation aborted");
		} else {
			// good to know how slow we are (almost nothing here is optimized
			// for speed, because it's just not important if the compilation
			// takes 1 or 4 seconds)
			long startTime = System.nanoTime();

			Log.info("PactProgram-Name: '" + pactProgram.getName() + "'");
			Log.info("PactProgram-PackageName: '" + pactProgram.getPackageName() + "'");

			// create tempDir like these:
			// C:/Users/Admin/AppData/Local/Temp/temp58770624873659021811012977649731482
			// D:/apache-tomcat/temp/pact_gui_2717372495593296495257137495073165
			File tempDir = Utils.createTempDirectory("pact_gui_");
			if (tempDir.exists()) {
				if (tempDir.isDirectory()) {
					Log.info("Temp directory created: " + tempDir.getAbsolutePath());
					tempFolderPath = Utils.formatPath(tempDir.getAbsolutePath());

					tempDir.setWritable(true);
					// has no effect when vm is not stopped gracefully and
					// furthermore stops .delete() from working on files which
					// are marked deleteOnExit
					// tempDir.deleteOnExit();
				} else {
					Log.error("Created temp file could not be changed to a temp directory '"
							+ tempDir.getAbsolutePath() + "'");
					return false;
				}

				// register temp dir for deletion on shut down of java virtual
				// maschine
			} else {
				Log.error("Could not create temp directory '" + tempDir.getAbsolutePath() + "'");
				return false;
			}
			result.setServerTempDirPath(tempFolderPath);
			Log.setLogFilePath(tempFolderPath + "/compile.log");

			// create MetaDir in tmp Dir
			String metaRelativeDirPath = "META-INF";
			File metaDir = new File(tempFolderPath + "/" + metaRelativeDirPath);
			if (metaDir.mkdirs()) {
				Log.info("Meta directory created '" + metaDir.getAbsolutePath() + "'");
			} else {
				Log.error("Could not create meta directory '" + metaDir.getAbsolutePath() + "'");
				return false;
			}

			// Create Minifest in metaDir
			String manifestFileName = "MANIFEST.MF";
			String manifestRelativeFilePath = metaRelativeDirPath + "/" + manifestFileName;
			// IMPORTANT: EACH line has to finish with a "\n" otherwise this
			// line will be ignored during merge
			String manifestFileContent = "Manifest-Version: 1.0\n";
			manifestFileContent += "Archiver-Version: Plexus Archiver\n";
			manifestFileContent += "Created-By: Pact-Gui Compiler\n";
			manifestFileContent += "Built-By: Pact-Gui User\n";
			manifestFileContent += "Build-Jdk: 1.6.0_19\n";
			manifestFileContent += "Pact-Assembler-Class: " + pactProgram.getPackageName() + "."
					+ pactProgram.getName() + "\n";
			Log.info("Create Manifestfile '" + tempFolderPath + "/" + manifestRelativeFilePath + "' and insert "
					+ manifestFileContent.length() + "bytes of content");
			File manifestFile = Utils.createFile(tempFolderPath + "/" + manifestRelativeFilePath, manifestFileContent);
			if (!manifestFile.exists()) {
				Log.error("Could not create manifest file '" + tempFolderPath + "/" + manifestRelativeFilePath + "'");
				return false;
			}
			result.addFileToResultSet(manifestFile.getAbsolutePath(), manifestFileContent);

			// create javaDir
			String javaRelativeDirPath = pactProgram.getPackageName().replace(".", "/");

			File javaDir = new File(tempFolderPath + "/" + javaRelativeDirPath);
			if (javaDir.mkdirs()) {
				Log.info("Java directory created '" + javaDir.getAbsolutePath() + "'");
			} else {
				Log.error("Could not create java directory '" + metaDir.getAbsolutePath() + "'");
				return false;
			}

			// create jar String for classPath
			File jarDir = new File(servletContext.getRealPath("/designer/compilation_resources/jars"));
			// new File(System.getProperty("user.dir"));
			// new File("designer/compilation_resources/jars");
			if (javaDir.exists()) {
				Log.info("jarDirPath found '" + jarDir.getAbsolutePath() + "'");
			} else {
				Log.error("Can't find needed pact jars under folder: '" + jarDir.getAbsolutePath() + "'");
				return false;
			}

			String jarResourceFilePathsString = jarDir.getAbsolutePath();
			String[] jarFilesInJarDir = jarDir.list();
			for (String jarFileName : jarFilesInJarDir) {
				jarResourceFilePathsString += ";" + jarDir.getAbsolutePath() + "/" + jarFileName;
				Log.info("Jar found and added to classpath: '" + jarFileName + "'");
			}

			// create embedded jars in lib folder
			if (pactProgram.getJarFiles().size() > 0) {
				if (session == null) {
					Log.error("Session not existing. Can't embed jars");
					return false;
				} else {
					// create lib folder for embedded jars
					String libRelativeDirPath = "lib";
					File libDir = new File(tempFolderPath + "/" + libRelativeDirPath);
					if (libDir.mkdirs()) {
						Log.info("Lib directory created '" + libDir.getAbsolutePath() + "'");
					} else {
						Log.error("Could not create lib directory '" + libDir.getAbsolutePath() + "'");
						return false;
					}

					// iterate over embedded jars in pactProgram - and take jar
					// content from session
					Log.info("Create " + pactProgram.getJarFiles().size() + " embedded jar files");
					for (JarFile jarFile : pactProgram.getJarFiles().values()) {
						byte[] jarFileContent = (byte[]) session.getAttribute(jarFile.getHash());

						// if jar content not found in session: abort
						if (jarFileContent == null) {
							Log.error("Jar file '" + jarFile.getHash() + "' not found in session");
						} else {
							// if found - save binary file content from session
							// to real file in temp folder
							try {
								Utils.writeBinaryFile(libDir.getAbsolutePath() + "/" + jarFile.getName(),
										jarFileContent);
							} catch (IOException e) {
								// for now just output a short message - further
								// bellow is the abort and a longer message
								Log.error("Error creatinf jar file:" + e.getMessage());
							}

							// jar file created or not?
							File realJarFile = new File(libDir.getAbsolutePath() + "/" + jarFile.getName());
							if (realJarFile.exists()) {
								Log.info("Created jar '/" + libRelativeDirPath + "/" + jarFile.getName() + "' with "
										+ jarFileContent.length + "bytes");
								jarResourceFilePathsString += ";" + realJarFile.getAbsolutePath();
								Log.info("Jar found and added to classpath: '" + jarFile.getName() + "'");
								result.addFileToResultSet(realJarFile.getAbsolutePath(), null);
							} else {
								Log.error("Could not create jar file '" + realJarFile.getAbsolutePath() + "'");
								return false;
							}
						}
					}
				}
			}

			// create java files
			String planJavaCode = "";

			// list of pacts already processed (as hashMap for easier/faster
			// access)
			HashMap<Integer, Pact> pactsProcessed = new HashMap<Integer, Pact>();

			// list of files to process (as hashMap for easier/faster access)
			HashMap<Integer, Pact> pactsToProcess = pactProgram.getPactsByType(PactType.SINK);

			// initially add all java files (not part of graph to
			// pactsToProcess-list)
			pactsToProcess.putAll(pactProgram.getPactsByType(PactType.JAVA));

			// get java header for all jave files
			String javaHeader = getDefaultPackageAndImportDeclarations(pactProgram);

			// process list of pactsToProcess
			int i = 0;
			while (pactsToProcess.size() > 0) {
				// abort endless loop
				if (i++ > 100) {
					Log.error("Alread 100 pacts processed. Either your pactProgram is too large for the compiler or an error occured, which led into an endless loop. ABORTED");
					return false;
				}

				Log.info("pactsToProcess: " + pactsToProcess.size());

				// get IDs of pacts to process. WORKAROUND to be able to change
				// to currently iterated map (TODO: improve with different map
				// implementation or other solution)
				ArrayList<Integer> pactsToProcessIds = new ArrayList<Integer>();
				for (Pact pact : pactsToProcess.values()) {
					pactsToProcessIds.add(pact.getId());
				}

				// iterate of pactsToProcess (pactsToProcessIds as workaround
				// for concurrent map access)
				for (Integer pactId : pactsToProcessIds) {
					// get pact
					Pact pact = pactsToProcess.get(pactId);
					if (pact != null) {
						boolean processeNow = true;

						if (!pactsProcessed.containsValue(pact)) {
							Log.info("Process pact: " + pact);

							// assure that all successors of a pact have been
							// processed (pact program graph is analyzed
							// backwards from sink to source)
							for (Pact tmpPact : pact.getSuccessors().values()) {
								if (!pactsProcessed.containsValue(tmpPact)) {
									Log.info("skip pact, because not all Successors have been processed: " + pact);
									processeNow = false;
								}
							}

							// so seems like this pact is ready to be processed
							// now
							if (processeNow) {
								// output pact to new .java file
								File newJavaFile = Utils.createFile(javaDir.getAbsolutePath() + "/" + pact.getName()
										+ ".java", javaHeader + pact.getJavaCode());
								Log.info("Create JavaSource File '" + newJavaFile.getAbsolutePath() + "' and insert "
										+ (javaHeader + pact.getJavaCode()).length() + "bytes of content");
								// Abort if file could not be created
								if (!newJavaFile.exists()) {
									Log.error("Failed to save java file to '" + newJavaFile.getAbsolutePath() + "'");
									return false;
								}

								// add newly created file to list of result
								// files
								result.addFileToResultSet(newJavaFile.getAbsolutePath(),
										javaHeader + pact.getJavaCode());

								if (pact.getType() == PactType.JAVA) {
									// java type (no data in/output) is skipped
								} else {
									// all Predecessors of this pact can be
									// process
									// in next loop
									pactsToProcess.putAll(pact.getPredecessors(1));

									// which connections are leading into this
									// pact
									ArrayList<Connection> inputConntections = pact.getInputConntections();

									// prepare a "setDegreeOfParallelism" line
									// to
									// append after the creation of the Input
									// Contract object
									String degreeOfParallelism = "";
									if (pact.getType().isRealInputContract()) {
										degreeOfParallelism = "\n\t\t" + pact.getCompilationName()
												+ ".setDegreeOfParallelism(" + pact.getDegreeOfParallelism() + ");";
									}

									// output plan code depening on type of pact
									switch (pact.getType()) {
									case SOURCE:
										planJavaCode = "\n\t\tFileDataSource " + pact.getCompilationName()
												+ " = new FileDataSource(" + pact.getName() + ".class, data"
												+ pact.getType() + "Path" + pact.getSourceOrSinkNumber() + ", \""
												+ pact.getName() + "\");" + degreeOfParallelism + "\n\n" + planJavaCode;
										break;

									case PACT_MAP:
										planJavaCode = "\t\tMapContract " + pact.getCompilationName()
												+ " = new MapContract(" + pact.getName() + ".class, "
												+ inputConntections.get(0).getFromPact().getCompilationName() + ", \""
												+ pact.getName() + "\");" + degreeOfParallelism + "\n\n" + planJavaCode;
										break;

									case PACT_REDUCE:
										// TODO: key type has to be dynamic:
										// " PactString.class, 0, "
										planJavaCode = "\t\tReduceContract " + pact.getCompilationName()
												+ " = new ReduceContract(" + pact.getName()
												+ ".class, PactString.class, 0, "
												+ inputConntections.get(0).getFromPact().getCompilationName() + ", \""
												+ pact.getName() + "\");" + degreeOfParallelism + "\n\n" + planJavaCode;
										break;

									case PACT_MATCH:
										// TODO: key type has to be dynamic:
										// " PactLong.class, 0, 0, "
										planJavaCode = "\t\tMatchContract " + pact.getCompilationName()
												+ " = new MatchContract(" + pact.getName()
												+ ".class, PactLong.class, 0, 0, "
												+ inputConntections.get(0).getFromPact().getCompilationName() + ", "
												+ inputConntections.get(1).getFromPact().getCompilationName() + ", \""
												+ pact.getName() + "\");" + degreeOfParallelism + "\n\n" + planJavaCode;
										break;

									case PACT_COGROUP:
										// TODO: key type has to be dynamic:
										// " PactString.class, 0, 0, "
										planJavaCode = "\t\tCoGroupContract " + pact.getCompilationName()
												+ " = new CoGroupContract(" + pact.getName()
												+ ".class, PactString.class, 0, 0, "
												+ inputConntections.get(0).getFromPact().getCompilationName() + ", "
												+ inputConntections.get(1).getFromPact().getCompilationName() + ", \""
												+ pact.getName() + "\");" + degreeOfParallelism + "\n\n" + planJavaCode;
										break;

									case PACT_CROSS:
										planJavaCode = "\t\tCrossContract " + pact.getCompilationName()
												+ " = new CrossContract(" + pact.getName() + ".class, "
												+ inputConntections.get(0).getFromPact().getCompilationName() + ", "
												+ inputConntections.get(1).getFromPact().getCompilationName() + ", \""
												+ pact.getName() + "\");" + degreeOfParallelism + "\n\n" + planJavaCode;
										break;

									case SINK:
										planJavaCode = "\t\tFileDataSink " + pact.getCompilationName()
												+ " = new FileDataSink(" + pact.getName() + ".class, data"
												+ pact.getType() + "Path" + pact.getSourceOrSinkNumber() + ", "
												+ inputConntections.get(0).getFromPact().getCompilationName() + ", \""
												+ pact.getName() + "\");" + degreeOfParallelism + "\n\n" + planJavaCode;
										break;

									default:
										// every type need to be defined
										Log.error("pact type '" + pact.getType().getLabel() + "' unknown for " + pact);
										return false;
									}

									// pact was processed now - don't process
									// again
									pactsProcessed.put(pact.getId(), pact);
								}
							}
						}

						// if this pact was processed - remove from todo list
						if (processeNow) {
							pactsToProcess.remove(pact.getId());
						}
					} else {
						// this should really never happen!!
						Log.error(pact + " not found");
						return false;
					}
				}
			}
			// now all pact, sink, source and java files are created

			// create main plan java file
			String planJavaFilePath = javaDir.getAbsolutePath() + "/" + pactProgram.getName() + ".java";
			String planJavaFileContent = getMainClassJava(pactProgram, javaHeader, planJavaCode);
			Log.info("Create Main JavaSource File '" + planJavaFilePath + "' and insert "
					+ planJavaFileContent.length() + "bytes of content");
			File planJavaFile = Utils.createFile(planJavaFilePath, planJavaFileContent);
			if (!planJavaFile.exists()) {
				Log.error("Failed to save plan-java file to '" + planJavaFile.getAbsolutePath() + "'");
				return false;
			}
			result.addFileToResultSet(planJavaFile.getAbsolutePath(), planJavaFileContent);

			// check again if java files have been created: one is manifest and
			// one is log
			// with no java files created out of pact - you could usually never
			// reach this point
			if (result.getFiles().size() < 3) {
				Log.error("No java files created. Pact Program Compilation aborted");
				return false;
			}

			// add all java files to javaFilePathsString for javac call
			HashMap<String, String> newClassFiles = new HashMap<String, String>();

			String javaFilePathsString = "";
			for (String javaFilePath : result.getFiles().keySet()) {
				if (javaFilePath.endsWith(".java")) {
					javaFilePathsString += " \"" + javaFilePath + "\"";
					newClassFiles.put(javaFilePath.replace(".java", ".class"), null);
				}
			}

			// compile java files with command line program javac
			Log.info(">>> COMPILE JAVA files with javac");
			if (!runSystemCommand("javac -classpath \"" + jarResourceFilePathsString + "\"" + javaFilePathsString,
					tempFolderPath)) {
				return false;
			}

			// add generated class files to result file list
			result.getFiles().putAll(newClassFiles);

			// compress all created files into a java archive (.jar)
			String jarFileName = pactProgram.getName().toLowerCase() + ".jar";
			Log.info(">>> CREATE JAR file '" + jarFileName + "'");
			if (!runSystemCommand("jar -cvfm " + jarFileName + " \"" + manifestRelativeFilePath + "\" "
					+ javaRelativeDirPath + "/*.*" + (pactProgram.getJarFiles().size() > 0 ? " lib/*.jar" : ""),
					tempFolderPath)) {
				return false;
			}

			// check if jar file was created
			File jarFile = new File(tempFolderPath + "/" + jarFileName);
			if (jarFile.exists()) {
				Log.info("You can now download the jar file '" + jarFile.getAbsolutePath() + "'");
				result.addFileToResultSet(jarFile.getAbsolutePath(), null);
			} else {
				Log.error("No jar file created");
				return false;
			}

			// compilation done - output duration
			Log.info("Elapsed Time: " + (((double) ((System.nanoTime() - startTime) / 1000)) / 1000) + "ms");
		}

		return true;
	}

	/**
	 * Method to perform a system command, setting the current working dir to
	 * dirPath Param
	 * 
	 * @param cmd
	 *            - System command
	 * @param dirPath
	 *            - the current working path
	 * @return
	 */
	public boolean runSystemCommand(String cmd, String dirPath) {
		boolean runSuccess = true;

		// if called with no command - abort
		if (cmd == null || "".equals(cmd)) {
			Log.error("runSystemCommand Method was called with empty cmd='" + cmd + "'. Aborted!");
			runSuccess = false;
		} else {
			// create runtime environment, if not done before
			if (runtime == null) {
				Log.info("Create Runtime to run system command");
				runtime = Runtime.getRuntime();
			} else {
				Log.info("Reuse Runtime object from last system command");
			}

			// create new process in the runtime environment
			Process process = null;
			try {
				File file = null;
				// "cmd /C" need to be in the beginning to perform this command
				if (!cmd.startsWith("cmd /C ")) {
					Log.info("Added cmd /C in front of command.");
					cmd = "cmd /C " + cmd;
				}

				// switch current working directory to dirPath if defined
				if (dirPath == null) {
					Log.info("No execution dir defined. Executed in path of current application");
				} else {
					file = new File(dirPath);
					if (file.exists()) {
						Log.info("Switched to execution dir: '" + dirPath + "'");
					} else {
						Log.error("Passed execution dir '" + dirPath + "' does not exist. RunSystemCommand aborted");
						return false;
					}
				}

				// execute command
				Log.info("Executing command: " + cmd);
				process = runtime.exec(cmd, null, file);

				// catch output?
				StreamCatcher errorCatcher = new StreamCatcher(process.getErrorStream());
				StreamCatcher outputCatcher = new StreamCatcher(process.getInputStream());
				errorCatcher.start();
				outputCatcher.start();

				// log standard output
				String standardOut = outputCatcher.getOutput();
				if (standardOut != null && !"".equals(standardOut)) {
					Log.info("System Command StandardOutput: " + standardOut);
				}

				// exit code meanings:
				// http://stackoverflow.com/questions/6454114/java-exit-codes-and-meanings
				int exitCode = process.waitFor();
				if (exitCode == 0) {
					Log.info("System Command finished successfully.");
				} else {
					Log.error("System Command NOT finished successfully. Program specific exit code: " + exitCode);
				}

				// log error output
				String errorOut = errorCatcher.getOutput();
				if (errorOut != null && !"".equals(errorOut)) {
					Log.error("System Command ErrorOutput: " + errorOut);
					// throw error and not just return to not skip finalize
					throw new Exception("Errors occured during command execution.");
				}
			} catch (IOException e) {
				Log.error("IOException when running system command: " + e.getMessage());
				runSuccess = false;
			} catch (InterruptedException e) {
				Log.error("InterruptedException when running system command: " + e.getMessage());
				runSuccess = false;
			} catch (Exception e) {
				Log.error("Errors occured during command execution.");
				runSuccess = false;
			} finally {
				if (process != null) {
					// Log.info("Finalize created system process");
					process.destroy();
					process = null;
				}
			}
		}

		return runSuccess;
	}

	// default header string for all .java files
	private String getDefaultPackageAndImportDeclarations(PactProgram pactProgram) {
		String javaHeader = "package " + pactProgram.getPackageName() + ";\n";
		javaHeader += "\n";
		javaHeader += "import java.io.*;\n";
		javaHeader += "import java.util.*;\n";
		javaHeader += "\n";
		javaHeader += "import eu.stratosphere.pact.common.stubs.*;\n";
		javaHeader += "import eu.stratosphere.pact.common.type.*;\n";
		javaHeader += "import eu.stratosphere.pact.common.type.base.*;\n";
		javaHeader += "import eu.stratosphere.pact.common.contract.*;\n";
		javaHeader += "import eu.stratosphere.pact.common.contract.ReduceContract.Combinable;\n";
		javaHeader += "import eu.stratosphere.pact.common.io.*;\n";
		javaHeader += "import eu.stratosphere.pact.common.plan.*;\n";
		javaHeader += "\n";
		return javaHeader;
	}

	// for better understandability of the compile method the surrounding plan
	// code is outsourced to this method
	private String getMainClassJava(PactProgram pactProgram, String javaHeader, String planJavaCode) {
		String m = ""; // main Class
		m += javaHeader;
		m += "/**\n";
		m += " * COUNT LETTERS\n";
		m += " * \n";
		m += " * @author MathiasNitzsche@gmail.com\n";
		m += " */\n";
		m += "public class " + pactProgram.getName() + " implements PlanAssembler, PlanAssemblerDescription {\n";
		m += "	/**\n";
		m += "	 * {@inheritDoc}\n";
		m += "	 */\n";
		m += "	@Override\n";
		m += "	public Plan getPlan(String... args) {\n";
		m += "		// parse job parameters\n";
		m += "		int noSubTasks = (args.length > 0 ? Integer.parseInt(args[0]) : 1);\n";

		// String
		String[] args = new String[pactProgram.getNextObjectId()];
		args[0] = "noSubStasks";

		// output all data inputs
		int sourcesCount = 0;
		for (Pact pact : pactProgram.getPactsByType(PactType.SOURCE).values()) {
			sourcesCount++;
			int i = pact.getSourceOrSinkNumber();
			int argNumber = i + 1;
			m += "		String data" + pact.getType().getLabel() + "Path" + i + " = (args.length > " + argNumber
					+ " ? args[" + argNumber + "] : \"\");\n";
			args[argNumber] = pact.getName();
		}

		// output all output
		// TODO: What's about multiple sinks
		String sinkPactClassName = "";
		for (Pact pact : pactProgram.getPactsByType(PactType.SINK).values()) {
			int i = pact.getSourceOrSinkNumber();
			int argNumber = i + 1 + sourcesCount;
			m += "		String data" + pact.getType().getLabel() + "Path" + i + " = (args.length > " + argNumber
					+ " ? args[" + argNumber + "] : \"\");\n";
			sinkPactClassName = pact.getCompilationName();
			args[argNumber] = pact.getName();
		}

		m += "\n";
		m += planJavaCode;
		m += "	\n";
		m += "		Plan plan = new Plan(" + sinkPactClassName + ", \"LetterCount Example\");\n";
		m += "		plan.setDefaultParallelism(noSubTasks);\n";
		m += "		return plan;\n";
		m += "	}\n";
		m += "\n";
		m += "	/**\n";
		m += "	 * {@inheritDoc}\n";
		m += "	 */\n";
		m += "	@Override\n";
		m += "	public String getDescription() {\n";

		String argString = "";
		for (String arg : args) {
			if (arg != null && !"".equals(arg)) {
				argString += " [" + arg + "]";
			}
		}

		m += "		return \"Parameters:" + argString + "\";\n";
		m += "	}\n";
		m += "}";
		return m;
	}
}
