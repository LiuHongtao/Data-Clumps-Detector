package vo;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class DataInfoVO {
	
	private String pkgName = "";
	private String className = "";
	private String methodName = "";
	
	private HashSet<String> datas = new HashSet<String>();
	
	/**
	 * for parameters of method
	 * @param pkgName
	 * @param className
	 * @param method
	 */
	public DataInfoVO(String pkgName, String className, 
			MethodDeclaration method) {
		init(pkgName, className);
		this.methodName = method.getName().toString();
		
		for (Object o: method.parameters()){
			SingleVariableDeclaration svd = (SingleVariableDeclaration)o;
			datas.add(svd.getName().toString() + "%" + svd.getType().toString());
		}
		
	}
	
	/**
	 * for fields of type
	 * @param pkgName
	 * @param className
	 * @param fields
	 */
	public DataInfoVO(String pkgName, String className, 
			FieldDeclaration[] fields) {
		init(pkgName, className);
		
		for (Object o: fields){
			FieldDeclaration field = (FieldDeclaration)o;
			addField(field);
		}
		
	}
	
	/**
	 * for fields of type
	 * @param pkgName
	 * @param className
	 * @param fields
	 */
	public DataInfoVO(String pkgName, String className, 
			ArrayList<FieldDeclaration> fields) {
		init(pkgName, className);
		
		for (Object o: fields){
			FieldDeclaration field = (FieldDeclaration)o;
			addField(field);
		}
		
	}
	
	private void addField(FieldDeclaration field) {
		int modifier = field.getModifiers();
		String type = field.getType().toString();
		for (Object o: field.fragments()) {
			VariableDeclarationFragment vdf = (VariableDeclarationFragment)o;
			String datainfo = vdf.getName().toString() + "%" + type;
			
			if (vdf.getInitializer() != null) {
				datainfo = datainfo + "%" + vdf.getInitializer().toString();
			}
			
			if (Modifier.isPrivate(modifier)) {
				datainfo = "-" + datainfo;
			}
			else if (Modifier.isProtected(modifier)) {
				datainfo = "#" + datainfo;
			}
			else if (Modifier.isPublic(modifier)) {
				datainfo = "+" + datainfo;
			}
			
			datas.add(datainfo);
		}
	}
	
	private void init(String pkgName, String className
			) {
		this.pkgName = pkgName;
		this.className = className;
	}

	public String getPkgName() {
		return pkgName;
	}

	public void setPkgName(String pkgName) {
		this.pkgName = pkgName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public HashSet<String> getDatas() {
		return datas;
	}

	public void setDatas(HashSet<String> datas) {
		this.datas = datas;
	}

}
