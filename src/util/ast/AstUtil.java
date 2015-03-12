package util.ast;

import org.eclipse.jdt.core.dom.CompilationUnit;

public class AstUtil extends AbstractAstUtil{
	
	/**
	 * for bindings
	 * @param classPath
	 * @param srcPath
	 */
	public AstUtil() {
		super();
	}
	
	@Override
	public CompilationUnit getCompUnit(String path) {
	    return getCompUnitMeta(path);
	}

	@Override
	protected void beforeVisit(CompilationUnit compUnit) {
	}
}
