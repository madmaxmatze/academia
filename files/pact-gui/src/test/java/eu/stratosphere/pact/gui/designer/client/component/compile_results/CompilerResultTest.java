package eu.stratosphere.pact.gui.designer.client.component.compile_results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import eu.stratosphere.pact.gui.designer.client.component.compile_results.CompilerResult.CompilationResultTreeNode;
import eu.stratosphere.pact.gui.designer.client.gin.AppInjector;
import eu.stratosphere.pact.gui.designer.client.gin.TestAppInjectorFactory;
import eu.stratosphere.pact.gui.designer.shared.model.helper.PactProgramCompilerResult;

public class CompilerResultTest {
	AppInjector appInjectorMock = TestAppInjectorFactory.getNewAppInjectorForTest();

	@Before
	public void setUp() {
	}

	@Test
	public void test() {
		// test all different kinds of CompilerResults object creation
		PactProgramCompilerResult result = null;
		
		// with no result
		CompilerResult compilerResult = new CompilerResult(appInjectorMock, result);
		
		// with empty result
		result = new PactProgramCompilerResult();
		assertFalse(result.wasSuccessFul());
		compilerResult = new CompilerResult(appInjectorMock, result);
		
		// with rootPath, but no files
		result.setServerTempDirPath("C:/test/pact");
		compilerResult = new CompilerResult(appInjectorMock, result);
		
		// with invalid files
		result.addFileToResultSet("sdfsdfsdf ? =", "empty");
		compilerResult = new CompilerResult(appInjectorMock, result);
		assertEquals(compilerResult.getStore().getRootCount(), 0);
		assertEquals(compilerResult.getStore().getAll().size(), 0);
		
		// with some ok and some invalid files
		result.getFiles().clear();
		result.addFileToResultSet("C:/test/pact/test.java", "empty");
		result.addFileToResultSet("C:/test/pact/test1.java", "empty");
		result.addFileToResultSet("C:/test/pact/test2.java", "empty");
		result.addFileToResultSet("C:/test----/pact/path1/path2/path3/notworking.java", "empty");
		compilerResult = new CompilerResult(appInjectorMock, result);
		// only 3 files + a root are recognized - invalid file is ignored
		assertEquals(compilerResult.getStore().getRootCount(), 1);
		assertEquals(compilerResult.getStore().getAll().size(), 4);

		// all files are fine, but invalid - check tree building
		result.getFiles().clear();
		result.addFileToResultSet("C:/test/pact/path1/path2/path3/test.java", "empty");
		result.addFileToResultSet("C:/test/pact/path1/path2/path3/test1.java", "empty");
		result.addFileToResultSet("C:/test/pact/path1/path2/path3/test2.java", "empty");
		result.addFileToResultSet("C:/test/pact/bad.jar", "empty");
		result.addFileToResultSet("C:/test/pact/compiler.log", "empty");
		compilerResult = new CompilerResult(appInjectorMock, result);
		// root + path1 + path2 + path3 + test.java + test1.java + test2.java + bad.jar + compiler.log = 9
		assertEquals(compilerResult.getStore().getAll().size(), 9);
		assertEquals(compilerResult.getStore().getRootCount(), 1);
		assertEquals(compilerResult.getStartNode().getId(), "/compiler.log");
		
		// check valid version 
		result.wasSuccessFul(true);
		assertTrue(result.wasSuccessFul());
		compilerResult = new CompilerResult(appInjectorMock, result);
		assertEquals(compilerResult.getStartNode().getId(), "/bad.jar");
		int deepest = 0;
		int leafCount = 0;
		for (CompilationResultTreeNode node : compilerResult.getStore().getAll()) {
			if (node.getChildren().size() == 0) {
				deepest = Math.max(compilerResult.getStore().getDepth(node), deepest);
				leafCount++;
			}
		}
		assertEquals(deepest, 5);
		assertEquals(leafCount, 5);
	}
}
