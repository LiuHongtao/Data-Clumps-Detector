/**
 * @author LHT
 * @date 04-17-2014
 * @task ast creator
 */
package util.ast;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import util.FileUtil;

public abstract class AbstractAstUtil {
	
	protected ASTParser astParser = ASTParser.newParser(AST.JLS4);
	
	public AbstractAstUtil() {
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
	}
	
	protected abstract CompilationUnit getCompUnit(String path);
	
	protected abstract void beforeVisit(CompilationUnit compUnit);
	
	/**
	 * @param javaFilePath
	 * @return
	 */
    protected CompilationUnit getCompUnitMeta(String path){
    	
        byte[] input = null;
		try {
			BufferedInputStream bufferedInputStream = new BufferedInputStream(
			        new FileInputStream(path));
			input = new byte[bufferedInputStream.available()];
	        bufferedInputStream.read(input);
	        bufferedInputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
		astParser.setUnitName(path);
        astParser.setSource(new String(
        		getInput(path)).toCharArray());

        CompilationUnit result = (CompilationUnit) astParser.createAST(null);
        
        return result;
    }
    
    public void getCompUnitAll(String projectPath, ASTVisitor visitor) {
    	FileUtil fileTool = new FileUtil();
		ArrayList<String> filePath = fileTool.getAllJavaFilePath(projectPath);
		
		for (String path: filePath) {
			CompilationUnit compUnit = getCompUnit(path);
			beforeVisit(compUnit);
			compUnit.accept(visitor);
		}
    }
    
    public byte[] getInput(String javaFilePath) {
    	byte[] input = null;
		try {
			BufferedInputStream bufferedInputStream = new BufferedInputStream(
			        new FileInputStream(javaFilePath));
			input = new byte[bufferedInputStream.available()];
	        bufferedInputStream.read(input);
	        bufferedInputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return input;
    }
    
    public CompilationUnit getNewUnit() {
		ASTParser astParser = ASTParser.newParser(AST.JLS4);
		astParser.setSource("".toCharArray());
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);

		CompilationUnit unit = (CompilationUnit) astParser.createAST(null);
		
		return unit;
	}
    
}

