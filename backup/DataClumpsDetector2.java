package visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import util.FileUtil;
import util.JdtAstUtil;
import vo.DataClumpsVO;
import vo.DataInfoVO;

public class DataClumpsDetector2 extends ASTVisitor{
	
	private int repeat = 0;
	private int size = 0;
	private ArrayList<DataInfoVO> dataInfoList = new ArrayList<DataInfoVO>();
	private ArrayList<DataClumpsVO> dataClumpsList = new ArrayList<DataClumpsVO>();

	public DataClumpsDetector2(String projectPath, int repeat, int size) {
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
		
		for (String path: filePath) {
			try {
				String[] classPath = {projectPath + "bin"};
				String[] srcPath = {projectPath + "src"};
				
				CompilationUnit compUnit = JdtAstUtil.getCompilationUnit(classPath, 
						srcPath, 
						path);
				pkgName = compUnit.getPackage().getName().toString();
				compUnit.accept(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean visit(TypeDeclaration node) {
		//TODO:bindings
//		ITypeBinding bindings = node.resolveBinding();
		
		//for parameters of method
		for (Object o: node.getMethods()) {
			MethodDeclaration method = (MethodDeclaration)o;
			//the number of parameters should be larger than data clumps size threshold
			if (method.parameters().size() >= size) {
				DataInfoVO vo = new DataInfoVO(pkgName, 
						node.getName().toString(), 
						//TODO:bindings
//						bindings, 
						method);
				dataInfoList.add(vo);
			}
		}
		
		//for fields of type
		DataInfoVO vo = new DataInfoVO(pkgName, 
				node.getName().toString(), 
				//TODO:bindings
//				bindings, 
				node.getFields());
		//the number of fields should be larger than data clumps size threshold
		if (vo.getDatas().size() >= size) {			
			dataInfoList.add(vo);
		}
		
		return false;
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
				
				//TODO:bindings
//				if (data_i.getMethodName().equals(data_j.getMethodName()) &&
//						!data_i.getMethodName().equals("")) {
//					if (data_i.getParams().size() == data_j.getParams().size() &&
//							data_i.getParams().size() == dataClumps.size()) {
//						HashSet<String> supers = new HashSet<String>();
//						supers.clear();
//						supers.addAll(
//								data_i.getSupers());
//						supers.retainAll(
//								data_j.getSupers());
//						
//						if (supers.size() > 0) {
//							continue;
//						}
//					}
//				}
				
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
	
	//according to intersections combination
	//some tiny data clumps which other clumps have
	//are treated as "sub data clumps"
	//and replaced with a new object
	//so methods of data clumps should be refreshed
	private void refreshClumps() {
		int listSize = dataClumpsList.size();
		for (int i = 0; i < listSize - 1; i++) {
			DataClumpsVO dataClumps_i = dataClumpsList.get(i);			
			for (int j = i + 1; j < listSize; j++) {
				DataClumpsVO dataClumps_j = dataClumpsList.get(j);
				
				if (!dataClumps_i.getDataClumps().equals(
						dataClumps_j.getDataClumps())) {
					HashSet<String> intersection = new HashSet<String>();
					intersection.clear();
					intersection.addAll(
							dataClumps_i.getDataClumps());
					intersection.retainAll(
							dataClumps_j.getDataClumps());
					
					if (intersection.size() > 0) {
						dataClumps_j.getSource().removeAll(
								dataClumps_i.getSource());
					}
				}
			}
		}
	}
	
	private ArrayList<DataClumpsVO> subDataClumpsList = new ArrayList<DataClumpsVO>();
	
	//if intersection A contains intersection B
	//the methods that have A must have B
	//so copy the methods information of A to B
	//and replace B in A with a symbol/new object 
	//as "sub data clumps"
	//dataClumps_i is always larger than dataClumps_j
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
					dataClumps_i.getDataClumps().removeAll(
							dataClumps_j.getDataClumps());
					
					subDataClumpsList.add(dataClumps_j);
					dataClumps_i.getDataClumps().add("@" + (subDataClumpsList.size() - 1));
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
			for (String data: dataClumps) {
				if (data.startsWith("@")) {
					int index = Integer.parseInt(
							data.substring(1, 
									data.length()));
					
					System.out.print("{");
					DataClumpsVO vo = subDataClumpsList.get(index);
					HashSet<String> subDataClumps = vo.getDataClumps();
					
					for (String subData: subDataClumps) {
						System.out.print(subData + "\t");
					}
					System.out.print("}\t");
				}
				else {
					System.out.print(data + "\t");
				}
			}
			System.out.println();
			System.out.println("---");
			
			HashSet<DataInfoVO> methods = dataClumps_i.getSource();
			for (DataInfoVO vo: methods) {
				System.out.print(vo.getPkgName() + "\t");
				System.out.print(vo.getClassName() + "\t");
				System.out.println(vo.getMethodName());
			}
			System.out.println("================================");
			System.out.println();
		}
		System.out.println("Data Clumps Sum:" + listSize);
	}
}
