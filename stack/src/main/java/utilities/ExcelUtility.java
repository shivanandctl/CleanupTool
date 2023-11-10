package utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import stack.IPcleanup;

public class ExcelUtility {

	public static XSSFWorkbook workbook;
	public static Sheet sheet;
	public static XSSFRow row;
	public static XSSFFont defaultFont;
	public static int rowCount = 0;
	public static int rowid = 1;

	public ExcelUtility() {
		workbook = new XSSFWorkbook();
		sheet = workbook.createSheet();
		createHeaderRow(sheet);
	}

	public static ArrayList<Object> exRowData = new ArrayList();
	public static HashMap<String, Object> exData;

	public static void readExcelData() {
		try {
			File file = new File(IPcleanup.fileDir + "\\IP_DATA\\IP_DATA.xlsx");
			FileInputStream fis = new FileInputStream(file);
			// creating Workbook instance that refers to .xlsx file
			XSSFWorkbook wb = new XSSFWorkbook(fis);
			XSSFSheet sheet = wb.getSheetAt(0);
			int rowTotal = sheet.getLastRowNum() + 1;
			IPcleanup.username = wb.getSheetAt(1).getRow(1).getCell(0).getStringCellValue();
			System.out.println("Authenticating with USER::" + wb.getSheetAt(1).getRow(1).getCell(0).getStringCellValue());
			IPcleanup.password = wb.getSheetAt(1).getRow(1).getCell(1).getStringCellValue();
			System.out.println(" -------------------------------------------------------------------------");
			System.out.println("                              BEGIN                                       ");
			System.out.println(" -------------------------------------------------------------------------\n");
			
			
			
			for (int i = 1; i < rowTotal; i++) {
				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				System.out.println("CLEANUP Started for SERVICE ID :: " + wb.getSheetAt(0).getRow(i).getCell(0));
				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//				System.out.println("SERVICE ID :: "+wb.getSheetAt(0).getRow(i).getCell(0));
				String service = wb.getSheetAt(0).getRow(i).getCell(0).getStringCellValue();
//				IPcleanup.deactivateService(service);
				ArrayList<String> parentservices = IPcleanup.getParentServices(service);
				if(parentservices.size()>0) {
					System.out.println("\n\n===========Cleaning of parent service started=============");
					for (String parent : parentservices) {
						System.out.println("\nCleaning associated parent service::"+parent);
						if(!parent.contains("_")) {
							IPcleanup.deleteDeactivateServiceFromACT(parent);	
						}						
						IPcleanup.deleteServicefromAsri(parent, "services");
						if(parent.contains("IRXX")) {
							IPcleanup.cleanIp(parent);
						}						
						IPcleanup.printMap();
					}
					System.out.println("===========Cleaning of parent service End================");
				}
				if(!service.contains("_")) {
					IPcleanup.deleteDeactivateServiceFromACT(service);	
				}
				IPcleanup.deleteServicefromAsri(service, "services");
				if(service.contains("IRXX")) {
					IPcleanup.cleanIp(service);
				}
				IPcleanup.printMap();
			}
			System.out.println(" -------------------------------------------------------------------------");
			System.out.println("                              END                                         ");
			System.out.println(" -------------------------------------------------------------------------");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void writeExcel(HashMap<String, Object> exData, String excelFilePath) throws IOException {
//	    XSSFWorkbook workbook = new XSSFWorkbook();
//	    XSSFRow row;
//	    Sheet sheet = workbook.createSheet();
//	    createHeaderRow(sheet);

		ArrayList<Object> x = new ArrayList();
		Set<String> keyid = exData.keySet();
		row = (XSSFRow) sheet.createRow(rowid++);
		for (String key : keyid) {
			x.add(exData.get(key));
		}

		int cellid = 0;

		for (String key : keyid) {
			switch (key) {
			case "colService":
				Cell cell = row.createCell(0);
				try {
					cell.setCellValue((String) exData.get(key));
				} catch (Exception e) {
					cell.setCellValue((Integer) exData.get(key));
				}

				break;

			case "colEnvironment":
				cell = row.createCell(1);
				try {
					cell.setCellValue((String) exData.get(key));
				} catch (Exception e) {
					cell.setCellValue((Integer) exData.get(key));
				}

				break;

			case "colRequestId":
				cell = row.createCell(5);
				try {
					cell.setCellValue((String) exData.get(key));
				} catch (Exception e) {
					cell.setCellValue((Integer) exData.get(key));
				}

				break;

			case "colActStatus":
				cell = row.createCell(2);
				try {
					cell.setCellValue((String) exData.get(key));
				} catch (Exception e) {
					cell.setCellValue((Integer) exData.get(key));
				}

				break;

			case "colActId":
				cell = row.createCell(7);
				try {
					cell.setCellValue((String) exData.get(key));
				} catch (Exception e) {
					cell.setCellValue((Integer) exData.get(key));
				}

				break;

			case "colDeactivateJobId":
				cell = row.createCell(6);
				try {
					cell.setCellValue((String) exData.get(key));
				} catch (Exception e) {
					cell.setCellValue((Integer) exData.get(key));
				}

				break;

			case "colError":
				cell = row.createCell(8);
				try {
					cell.setCellValue((String) exData.get(key));
				} catch (Exception e) {
					cell.setCellValue((Integer) exData.get(key));
				}

				break;

			case "colAsri_Status":
				cell = row.createCell(3);
				try {
					cell.setCellValue((String) exData.get(key));
				} catch (Exception e) {
					cell.setCellValue((Integer) exData.get(key));
				}

				break;

			case "colIP_Status":
				cell = row.createCell(4);
				try {
					cell.setCellValue((String) exData.get(key));
				} catch (Exception e) {
					cell.setCellValue((Integer) exData.get(key));
				}

				break;

			default:
				break;
			}
		}

		int noOfColumns = sheet.getRow(0).getPhysicalNumberOfCells();
		for (int i = 0; i < noOfColumns; i++) {
			sheet.autoSizeColumn(i);
		}
		// writing the workbook into the file...
		FileOutputStream out = new FileOutputStream(new File(excelFilePath));

		workbook.write(out);
		out.close();

		// resetting variable
		IPcleanup.exVar_ServiceAlias = "NULL";
		IPcleanup.exVar_IpStatus = "NULL";
		IPcleanup.exVar_AsriStatus = "NULL";
		IPcleanup.exVar_ActStatus = "NULL";
		IPcleanup.exVar_RequestId = 0;
		IPcleanup.exVar_IdentifierId = "NULL";
		IPcleanup.exVar_ActId = "NULL";
		IPcleanup.exVar_Error = "NULL";
		IPcleanup.exVar_DeactivateJobId = "NULL";
		IPcleanup.exVar_Environment = "NULL";

	}

	private static void createHeaderRow(Sheet sheet) {

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		defaultFont = workbook.createFont();
		defaultFont.setFontName("Arial");
		defaultFont.setColor(IndexedColors.DARK_RED.getIndex());
		defaultFont.setBold(true);
//	    Font font = sheet.getWorkbook().createFont();
//	    font.setBoldweight((short) 10);
//	    font.setFontHeightInPoints((short) 16);
		cellStyle.setFont(defaultFont);

		Row row = sheet.createRow(0);
		Cell cellSERVICE = row.createCell(0);
		cellSERVICE.setCellStyle(cellStyle);
		cellSERVICE.setCellValue("SERVICE");

		Cell cellENVIRONMENT = row.createCell(1);
		cellENVIRONMENT.setCellStyle(cellStyle);
		cellENVIRONMENT.setCellValue("ENVIRONMENT");

		Cell cellACT_STATUS = row.createCell(2);
		cellACT_STATUS.setCellStyle(cellStyle);
		cellACT_STATUS.setCellValue("ACT_STATUS");

		Cell cellASRI_STATUS = row.createCell(3);
		cellASRI_STATUS.setCellStyle(cellStyle);
		cellASRI_STATUS.setCellValue("ASRI_STATUS");

		Cell cellIP_STATUS = row.createCell(4);
		cellIP_STATUS.setCellStyle(cellStyle);
		cellIP_STATUS.setCellValue("IP_STATUS");

		Cell cellREQUEST_ID = row.createCell(5);
		cellREQUEST_ID.setCellStyle(cellStyle);
		cellREQUEST_ID.setCellValue("REQUEST_ID");

		Cell cellDEACTIVATE_JOB_ID = row.createCell(6);
		cellDEACTIVATE_JOB_ID.setCellStyle(cellStyle);
		cellDEACTIVATE_JOB_ID.setCellValue("DEACTIVATE_JOB_ID");

		Cell cellACT_ID = row.createCell(7);
		cellACT_ID.setCellStyle(cellStyle);
		cellACT_ID.setCellValue("ACT_ID");

		Cell cellERROR = row.createCell(8);
		cellERROR.setCellStyle(cellStyle);
		cellERROR.setCellValue("ERROR");

	}

}
