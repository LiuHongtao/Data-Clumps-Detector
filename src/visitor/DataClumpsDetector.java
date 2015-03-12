package visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import util.FileUtil;
import util.InstanceOfUtil;
import util.ast.AstUtil;
import vo.DataClumpsVO;
import vo.DataInfoVO;

public class DataClumpsDetector extends ASTVisitor{
	
	private int repeat = 0;
	private int size = 0;
	private ArrayList<DataInfoVO> dataInfoList = new ArrayList<DataInfoVO>();
	private ArrayList<DataClumpsVO> dataClumpsList = new ArrayList<DataClumpsVO>();

	public DataClumpsDetector(String projectPath, int repeat, int size) {
		this.repeat = repeat;
		this.size = size;
		
		dataCollection(projectPath);
		
		findIntersections();
		
		combineIntersections();
		
		refreshClumps();
		
		findSubClumps();
		
		findClumpsByThreshold();
		
		printClumps();
	}
	
	private String pkgName = "";
	
	//traverse all types to collect information
	private void dataCollection(String projectPath) {
		FileUtil fileTool = new FileUtil();
		ArrayList<String> filePath = fileTool.getAllJavaFilePath(projectPath);

		AstUtil astUtil = new AstUtil(); 
		for (String path: filePath) {
			try {
				CompilationUnit compUnit = astUtil.getCompUnit(path);
				if (compUnit.getPackage() != null) {
					pkgName = compUnit.getPackage().getName().toString();
				}
				else {
					pkgName = "!Null";
				}
				
				compUnit.accept(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		String className = "!Anonymous";
		ArrayList<FieldDeclaration> fields = new ArrayList<FieldDeclaration>();
		
		for (Object o: node.bodyDeclarations()) {
			ASTNode astnode = (ASTNode)o; 
			if (InstanceOfUtil.isMethodDeclaration(astnode)) {
				//for parameters of method
				MethodDeclaration method = (MethodDeclaration)o;
				addMethod(method, className);
			}
			else if (InstanceOfUtil.isFieldDeclaration(astnode)) {
				FieldDeclaration field = (FieldDeclaration)o;
				fields.add(field);
			}
		}
		
		//for fields of type
		DataInfoVO vo = new DataInfoVO(pkgName, 
				className, 
				fields);
		//the number of fields should be larger than data clumps size threshold
		if (vo.getDatas().size() >= size) {			
			dataInfoList.add(vo);
		}
		
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		//for fields of type
		DataInfoVO vo = new DataInfoVO(pkgName, 
				node.getName().toString(), 
				node.getFields());
		//the number of fields should be larger than data clumps size threshold
		if (vo.getDatas().size() >= size) {			
			dataInfoList.add(vo);
		}
		
		//for parameters of method
		for (Object o: node.getMethods()) {
			MethodDeclaration method = (MethodDeclaration)o;
			addMethod(method, node.getName().toString());
		}
		
		return true;
	}
	
	private void addMethod(MethodDeclaration method, String className) {
		//the number of parameters should be larger than data clumps size threshold
		if (method.parameters().size() >= size) {
			DataInfoVO vo2 = new DataInfoVO(pkgName, 
					className, 
					method);
			dataInfoList.add(vo2);
		}
	}
	
	//compute the intersection between two data
	private void findIntersections() {
		int listSize = dataInfoList.size();
		for (int i = 0; i < listSize - 1; i++) {
			DataInfoVO data_i = dataInfoList.get(i);
			for (int j = i + 1; j < listSize; j++) {
				DataInfoVO data_j = dataInfoList.get(j);
				
				//if data_i and data_j are different
				if ((data_i.getMethodName().equals("") || data_j.getMethodName().equals(""))
						&& !data_i.getMethodName().equals(data_j.getMethodName())) {
					continue;
				}
				
				//compute the intersection
				HashSet<String> dataClumps = new HashSet<String>();
				dataClumps.clear();
				dataClumps.addAll(
						data_i.getDatas());
				dataClumps.retainAll(
						data_j.getDatas());
				
				//the size of intersection should be larger than data clumps size threshold
				if (dataClumps.size() >= size) {
					DataClumpsVO vo = new DataClumpsVO(dataClumps);
					vo.getSource().add(data_i);
					vo.getSource().add(data_j);
					
					dataClumpsList.add(vo);
				}
			}
		}
		
		//sort intersections according to the size from largest to smallest
		Collections.sort(dataClumpsList);
	}
	
	//combine data if they have same intersection
	private void combineIntersections() {
		int listSize = dataClumpsList.size();
		for (int i = 0; i < listSize - 1; i++) {
			DataClumpsVO dataClumps_i = dataClumpsList.get(i);
			for (int j = i + 1; j < listSize; j++) {
				DataClumpsVO dataClumps_j = dataClumpsList.get(j);
				
				if (dataClumps_i.getDataClumps().equals(
						dataClumps_j.getDataClumps())) {
					//check if they are the same
					dataClumps_i.getSource().addAll(
							dataClumps_j.getSource());
					dataClumpsList.remove(dataClumps_j);
					j = j - 1;
					listSize = listSize - 1;
				}
			}
		}
	}
	
	//so methods of data clumps should be refreshed
	private void refreshClumps() {
		int listSize = dataClumpsList.size();
		for (int i = 0; i < listSize - 1; i++) {
			DataClumpsVO dataClumps_i = dataClumpsList.get(i);			
			for (int j = i + 1; j < listSize; j++) {
				DataClumpsVO dataClumps_j = dataClumpsList.get(j);
				
				HashSet<String> clumps_i = dataClumps_i.getDataClumps();
				HashSet<String> clumps_j = dataClumps_j.getDataClumps();
				HashSet<DataInfoVO> source_i = dataClumps_i.getSource();
				HashSet<DataInfoVO> source_j = dataClumps_j.getSource();
				
				HashSet<String> intersection = new HashSet<String>();
				intersection.clear();
				intersection.addAll(clumps_i);
				intersection.retainAll(clumps_j);
				
				if (intersection.size() > 0) {
					if (clumps_j.size() < clumps_i.size()) {
						source_j.removeAll(source_i);
					}
					else if (clumps_j.size() == clumps_i.size()) {
						if (source_j.size() <= source_i.size()) {
							source_j.removeAll(source_i);
						}
						else if (source_j.size() > source_i.size()) {
							source_i.removeAll(source_j);
						}
					}
				}
			}
		}
	}
	
	//if intersection A contains intersection B
	//the methods that have A must have B
	//so copy the methods information of A to B
	//and replace B in A with a symbol/new object 
	//as "sub data clumps"
	//dataClumps_i is always larger than dataClumps_j
	private ArrayList<DataClumpsVO> subDataClumpsList = new ArrayList<DataClumpsVO>();
	private void findSubClumps() {
		int listSize = dataClumpsList.size();
		for (int i = 0; i < listSize - 1; i++) {
			DataClumpsVO dataClumps_i = dataClumpsList.get(i);			
			for (int j = i + 1; j < listSize; j++) {
				DataClumpsVO dataClumps_j = dataClumpsList.get(j);
				
				if (dataClumps_i.getDataClumps().containsAll(
						dataClumps_j.getDataClumps())){		
					dataClumps_j.getSource().addAll(
							dataClumps_i.getSource());
					
					if (!subDataClumpsList.contains(dataClumps_j)) {
						subDataClumpsList.add(dataClumps_j);
					}				
					dataClumps_i.getDataClumps().removeAll(
							dataClumps_j.getDataClumps());
					dataClumps_i.getDataClumps().add("@" + subDataClumpsList.indexOf(dataClumps_j));
				}
			}
		}
	}
	
	private void findClumpsByThreshold() {
		int listSize = dataClumpsList.size();
		for (int i = 0; i < listSize; i++) {
			DataClumpsVO dataClumps_i = dataClumpsList.get(i);
			if (dataClumps_i.getSource().size() < repeat ||
					dataClumps_i.getDataClumps().size() < size) {
				dataClumpsList.remove(dataClumps_i);
				i = i - 1;
				listSize = listSize - 1;
			}
		}
	}
	
	private void printClumps() {
		int listSize = dataClumpsList.size();
		for (int i = 0; i < listSize; i++) {
			DataClumpsVO dataClumps_i = dataClumpsList.get(i);
			HashSet<String> dataClumps = dataClumps_i.getDataClumps();
			System.out.println(dataClumps.size());
			for (String data: dataClumps) {
				printClumps2(data);
			}
			System.out.println();
			System.out.println("---");
			
			HashSet<DataInfoVO> methods = dataClumps_i.getSource();
			for (DataInfoVO vo: methods) {
				System.out.print(vo.getPkgName() + "\t");
				System.out.print(vo.getClassName() + "\t");
				if (vo.getMethodName() == "") {
					System.out.println("Class");
				}
				System.out.println(vo.getMethodName());
			}
			System.out.println("================================");
			System.out.println();
		}
		System.out.println("Data Clumps Sum:" + listSize);
	}
	
	private void printClumps2(String data) {
		if (data.startsWith("@")) {
			int index = Integer.parseInt(
					data.substring(1, 
							data.length()));
			
			System.out.print("@" + index + " {");
			DataClumpsVO vo = subDataClumpsList.get(index);
			HashSet<String> subDataClumps = vo.getDataClumps();
			
			for (String subData: subDataClumps) {
				printClumps2(subData);
			}
			System.out.print("}\t");
		}
		else {
			System.out.print(data + "\t");
		}
	}
}
