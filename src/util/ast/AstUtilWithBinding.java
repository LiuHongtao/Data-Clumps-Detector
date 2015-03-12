package util.ast;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class AstUtilWithBinding extends AbstractAstUtil {
	
	private String[] classPath;
	private String[] srcPath;
	
	/**
	 * for bindings
	 */
	public AstUtilWithBinding() {
		super();
	}
	
	/**
	 * for bindings
	 * @param classPath
	 * @param srcPath
	 */
	public AstUtilWithBinding(String[] classPath, String[] srcPath) {
		super();
		
		this.classPath = classPath;
		this.srcPath = srcPath;
	}
	
	@Override
	public CompilationUnit getCompUnit(String path) {
	    astParser.setEnvironment(classPath, srcPath, null, false);
	    astParser.setResolveBindings(true);
	    astParser.setBindingsRecovery(true);
	    
	    return getCompUnitMeta(path);
	}
	
	public CompilationUnit getCompUnit(ICompilationUnit icu) {
		astParser.setSource(icu);
		astParser.setResolveBindings(true);
		astParser.setBindingsRecovery(true);
		
		CompilationUnit result = (CompilationUnit) astParser.createAST(null);

//		IProblem[] problems = result.getProblems();
//		for (IProblem problem : problems) {
//			±‡“Î±®¥Ì
//			System.out.println(problem.toString());
//		}

		if (result.getAST().hasBindingsRecovery()) {
			System.out.println("Binding activated.");
		}
		
		return result;
	}

	@Override
	protected void beforeVisit(CompilationUnit compUnit) {
	}
	
}
