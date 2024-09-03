package utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
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

	public void filterValidCircuitIDfromXlsxFile() {
		// specify the path of the xlsx file
		String filePath = "C:\\STAF_Files\\Dump\\circkss.xlsx";
		// specify the column you want to read
		int columnIndex = 7; // for example, read data from column E
		// create a file object
		File file = new File(filePath);
		try {
			FileInputStream inputStream = new FileInputStream(file);
			XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
			XSSFSheet sheet = workbook.getSheetAt(0);
			ArrayList<String> uniqueCircuitIDs = new ArrayList<String>();
			for (Row row : sheet) {
				// get the cell at the specified column index
				Cell cell = row.getCell(columnIndex);
				// check if the cell is not null and contains a string value
				if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING) {
					// print the value of the cell
//                    System.out.println(cell.getStringCellValue());

					String ckt = cell.getStringCellValue();
					if (ckt.indexOf("IRXX") != -1 && !uniqueCircuitIDs.contains(ckt)) {
						// add the string to the output ArrayList if it contains IRXX and is not already
						// present in the output
						uniqueCircuitIDs.add(ckt);
						System.out.println(ckt);
					}
				}
			}

			// close the workbook and input stream
			workbook.close();
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<String> filterValidCircuitIDfromCsvFile() {
		// specify the path of the xlsx file
		String filePath = "C:\\STAF_Files\\Dump\\circkss.csv";
		// specify the column you want to read
		int columnIndex = 7; // for example, read data from column E
		// create a file object
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		ArrayList<String> uniqueCircuitIDs = new ArrayList<String>();
		try {
			fileReader = new FileReader(filePath);
			bufferedReader = new BufferedReader(fileReader);
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				String[] columns = line.split(",");
				if (columnIndex >= 0 && columnIndex < columns.length) {
					// print the value of the column
//                    System.out.println(columns[columnIndex]);
					String ckt = columns[7];
					if (ckt.indexOf("IRXX") != -1 && !uniqueCircuitIDs.contains(ckt)) {
						// add the string to the output ArrayList if it contains IRXX and is not already
						// present in the output
						uniqueCircuitIDs.add(ckt);
//                  	  System.out.println(ckt);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
				if (fileReader != null) {
					fileReader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return uniqueCircuitIDs;
	}

	public void getCircuitIDs() {
		ArrayList<String> cktIds = filterValidCircuitIDfromCsvFile();
		for (String ckt : cktIds) {
			System.out.println(ckt);
		}
	}

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
			IPcleanup.inventoryType = wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue();
//			IPcleanup.cleanIpUsingOrderId = wb.getSheetAt(0).getRow(1).getCell(5).getStringCellValue();
			String cleanIpUsingOrderId = null;
			
			String explicitEnv = null;
			String workflow = null;
			String inventoryOnlyCleanup = null;
			//handle null or empty explicit environment
			try {
                explicitEnv = wb.getSheetAt(0).getRow(1).getCell(2).getStringCellValue();                
			} catch (Exception e) {
				explicitEnv = "";
			}
			//handle null or empty workflow
			try {
				workflow = wb.getSheetAt(0).getRow(1).getCell(3).getStringCellValue();
			} catch (Exception e) {
				workflow = "";
			}
			
			//handle null or empty inventoryOnlyCleanup
			try {
				inventoryOnlyCleanup = wb.getSheetAt(0).getRow(1).getCell(4).getStringCellValue();
			} catch (Exception e) {
				inventoryOnlyCleanup = null;
			}
			
			//handle null or empty cleanIpUsingOrderId
			try {
                cleanIpUsingOrderId = wb.getSheetAt(0).getRow(1).getCell(5).getStringCellValue();
            } catch (Exception e) {
                cleanIpUsingOrderId = null;
            }
			
			
			if (cleanIpUsingOrderId != null && cleanIpUsingOrderId.equalsIgnoreCase("YES")) {
				
				
			} else {
				System.out.println(" -------------------------------------------------------------------------");
				System.out.println("		Cleanup started with following parameters::                                       ");
				System.out.println(" -------------------------------------------------------------------------\n");
				System.out.println("INVENTORY_TYPE::" + IPcleanup.inventoryType);
				if (!explicitEnv.isEmpty() && !explicitEnv.equalsIgnoreCase("") && explicitEnv != null) {
					System.out.println("ENVIRONMENT::" + explicitEnv);
				} else {
					System.out.println("ENVIRONMENT::" + "No specific environment provided so cleaning all environments");
				}
				if (!workflow.isEmpty() && !workflow.equalsIgnoreCase("") && workflow != null) {
					System.out.println("WORKFLOW::" + workflow);
				} else {
					System.out.println("WORKFLOW::" + "No specific workflow provided so defaulting to DELETE workflow");
				}
			}
			

			
			System.out.println(" -------------------------------------------------------------------------");
			
//			if(inventoryOnlyCleanup!=null && !inventoryOnlyCleanup.isEmpty() && !inventoryOnlyCleanup.equalsIgnoreCase("")) {
			if(inventoryOnlyCleanup!=null && inventoryOnlyCleanup.equalsIgnoreCase("yes") && !cleanIpUsingOrderId.equalsIgnoreCase("YES")) {
                System.out.println("CLEANING STARTED FOR INVENTORY ONLY");
                System.out.println(" -------------------------------------------------------------------------");
                System.out.println("                              BEGIN                                       ");
                System.out.println(" -------------------------------------------------------------------------\n");
                for (int i = 1; i < rowTotal; i++) {
					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
					System.out.println("CLEANUP Started for SERVICE ID :: " + wb.getSheetAt(0).getRow(i).getCell(0));
					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
					String service = wb.getSheetAt(0).getRow(i).getCell(0).getStringCellValue();
					if(!explicitEnv.isEmpty() && !explicitEnv.equalsIgnoreCase("") && explicitEnv!=null) {
                        IPcleanup.inventoryCleanUp(service, explicitEnv);
                        
						if (service.contains("IRXX")) {
							IPcleanup.cleanIp(service);
						}
					}else {
					IPcleanup.deleteServicefromInventoryOnly(service, "services");
					if (service.contains("IRXX")) {
						IPcleanup.cleanIp(service);
					}
					}
				}
			}

			else if (IPcleanup.inventoryType.equalsIgnoreCase("asri")&& explicitEnv!=null && !explicitEnv.isEmpty() && !explicitEnv.equalsIgnoreCase("")
					&& workflow!=null  && !workflow.equalsIgnoreCase("") && !cleanIpUsingOrderId.equalsIgnoreCase("YES")) {
//                System.out.println("Explicit Environment provided::"+explicitEnv);
                System.out.println("\n\nCLEANING STARTED FOR ASRI INVENTORY AND ACT FOR ENVIRONMENT::"+explicitEnv);
				System.out.println(" -------------------------------------------------------------------------");
				System.out.println("                              BEGIN                                       ");
				System.out.println(" -------------------------------------------------------------------------\n");
				for (int i = 1; i < rowTotal; i++) {
					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
					System.out.println("CLEANUP Started for SERVICE ID :: " + wb.getSheetAt(0).getRow(i).getCell(0));
					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
					String service = wb.getSheetAt(0).getRow(i).getCell(0).getStringCellValue();

					IPcleanup.cleanPortsViaPortMonitorData(service, explicitEnv, workflow);
					IPcleanup.printMap();
				}
				System.out.println(" -------------------------------------------------------------------------");
				System.out.println("                              END                                         ");
				System.out.println(" -------------------------------------------------------------------------");
                

			}
			
			else if (IPcleanup.inventoryType.equalsIgnoreCase("arm") && !cleanIpUsingOrderId.equalsIgnoreCase("YES")) {
				IPcleanup.Test1_SASI = "https://sasi-test1.kubeodc-test.corp.intranet/inventory/v1/arm/service_type?name=";
				IPcleanup.Test4_SASI = "https://sasi-test4.kubeodc-test.corp.intranet/inventory/v1/arm/service_type?name=";
				System.out.println("\n\nCLEANING STARTED FOR ARM INVENTORY AND ACT");
				System.out.println(" -------------------------------------------------------------------------");
				System.out.println("                              BEGIN                                       ");
				System.out.println(" -------------------------------------------------------------------------\n");

				for (int i = 1; i < rowTotal; i++) {
					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
					System.out.println("CLEANUP Started for SERVICE ID :: " + wb.getSheetAt(0).getRow(i).getCell(0));
					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
					String service = wb.getSheetAt(0).getRow(i).getCell(0).getStringCellValue();

					IPcleanup.deleteDeactivateServiceFromACT(service);
					IPcleanup.deleteServicefromArm(service, "services");
					IPcleanup.printMap();
				}
				System.out.println(" -------------------------------------------------------------------------");
				System.out.println("                              END                                         ");
				System.out.println(" -------------------------------------------------------------------------");
			} 
			//IF INVENTORY IS ASRI
			else if (IPcleanup.inventoryType.equalsIgnoreCase("asri") && cleanIpUsingOrderId!=null && !cleanIpUsingOrderId.equalsIgnoreCase("YES")) {
				System.out.println("\n\nCLEANING STARTED FOR ASRI INVENTORY AND ACT");
				System.out.println(" -------------------------------------------------------------------------");
				System.out.println("                              BEGIN                                       ");
				System.out.println(" -------------------------------------------------------------------------\n");

				for (int i = 1; i < rowTotal; i++) {
					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
					System.out.println("CLEANUP Started for SERVICE ID :: " + wb.getSheetAt(0).getRow(i).getCell(0));
					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//					System.out.println("SERVICE ID :: "+wb.getSheetAt(0).getRow(i).getCell(0));
					String service = wb.getSheetAt(0).getRow(i).getCell(0).getStringCellValue();
//					IPcleanup.deactivateService(service);
					ArrayList<String> parentservices = IPcleanup.getParentServices(service);
					if (parentservices.size() > 0) {
						System.out.println("\n\n===========Cleaning of parent service started=============");
						for (String parent : parentservices) {
							System.out.println("\nCleaning associated parent service::" + parent);							
							if (parent.contains("IRXX")) {
//								boolean isInternetServiceCleaned = IPcleanup.deleteInternetServiceFromACT(parent);
//								boolean isInternetServiceCleaned = IPcleanup.deactivateService(parent);
								boolean isInternetServiceCleaned = IPcleanup.networkCleanup(parent,explicitEnv,workflow);
								if(isInternetServiceCleaned) {
									//cleanup the service from inventory from specific environment
									if(!explicitEnv.isEmpty() && !explicitEnv.equalsIgnoreCase("") && explicitEnv!=null) {
                                        IPcleanup.inventoryCleanUp(parent, explicitEnv);
									}else {
									IPcleanup.deleteServicefromInventory(parent, "services");
									}
									IPcleanup.cleanIp(parent);
								}
							}else {
								//boolean isInternetServiceCleaned = IPcleanup.deactivateService(parent);
								boolean isInternetServiceCleaned = IPcleanup.networkCleanup(parent,explicitEnv,workflow);
								if(isInternetServiceCleaned) {
									if(!explicitEnv.isEmpty() && !explicitEnv.equalsIgnoreCase("") && explicitEnv!=null) {
                                        IPcleanup.inventoryCleanUp(parent, explicitEnv);
									}else {
									IPcleanup.deleteServicefromInventory(parent, "services");
									}
									IPcleanup.cleanIp(parent);
								
								
//								IPcleanup.deactivateService(parent);
////								IPcleanup.deleteDeactivateServiceFromACT(parent);							
//								IPcleanup.deleteServicefromInventory(parent, "services");
								}
							}
							IPcleanup.printMap();
						}
						System.out.println("===========Cleaning of parent service End================");
					}
					{

						if (service.contains("IRXX")) {
							//commented for testing
							//boolean isInternetServiceCleaned = IPcleanup.deleteInternetServiceFromACT(service);
//							boolean isInternetServiceCleaned = IPcleanup.deactivateService(service);
							boolean isInternetServiceCleaned = IPcleanup.networkCleanup(service,explicitEnv,workflow);
							
							//comment below line after testing
//							boolean isInternetServiceCleaned = true;
							
							if(isInternetServiceCleaned) {
								if(!explicitEnv.isEmpty() && !explicitEnv.equalsIgnoreCase("") && explicitEnv!=null) {
                                    IPcleanup.inventoryCleanUp(service, explicitEnv);
								}else {
								IPcleanup.deleteServicefromInventory(service, "services");
								}
								IPcleanup.cleanIp(service);
							}else {
								IPcleanup.exVar_AsriStatus = "SKIPPED";
								IPcleanup.exData.put("colAsri_Status", IPcleanup.exVar_AsriStatus);
								IPcleanup.exVar_IpStatus = "SKIPPED";
								IPcleanup.exData.put("colIP_Status", IPcleanup.exVar_IpStatus);
								
							}
						}else {
//							boolean isInternetServiceCleaned =  IPcleanup.deactivateService(service);
							boolean isInternetServiceCleaned = IPcleanup.networkCleanup(service, explicitEnv, workflow);
							// comment below line after testing
//							IPcleanup.deleteDeactivateServiceFromACT(service);							
//							IPcleanup.deleteServicefromInventory(service, "services");
							if(isInternetServiceCleaned) {
								if(!explicitEnv.isEmpty() && !explicitEnv.equalsIgnoreCase("") && explicitEnv!=null) {
                                    IPcleanup.inventoryCleanUp(service, explicitEnv);
								}else {
								IPcleanup.deleteServicefromInventory(service, "services");
								}
//								IPcleanup.cleanIp(service);
							}else {
								IPcleanup.exVar_AsriStatus = "SKIPPED";
								IPcleanup.exData.put("colAsri_Status", IPcleanup.exVar_AsriStatus);
								IPcleanup.exVar_IpStatus = "SKIPPED";
								IPcleanup.exData.put("colIP_Status", IPcleanup.exVar_IpStatus);
								
							}
						}
					}

					IPcleanup.printMap();
				}
				System.out.println(" -------------------------------------------------------------------------");
				System.out.println("                              END                                         ");
				System.out.println(" -------------------------------------------------------------------------");
			}else if (IPcleanup.inventoryType.equalsIgnoreCase("delete") && !cleanIpUsingOrderId.equalsIgnoreCase("YES")) {
				System.out.println("\n\nCHECKING FOR DELETE/DEACTIVATE SERVICES IN ACT");
				System.out.println(" -------------------------------------------------------------------------");
				System.out.println("                              BEGIN                                       ");
				System.out.println(" -------------------------------------------------------------------------\n");

				for (int i = 1; i < rowTotal; i++) {
					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
					System.out.println("SEARCHING Started for SERVICE ID :: " + wb.getSheetAt(0).getRow(i).getCell(0));
					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//					System.out.println("SERVICE ID :: "+wb.getSheetAt(0).getRow(i).getCell(0));
					String service = wb.getSheetAt(0).getRow(i).getCell(0).getStringCellValue();
					IPcleanup.deactivateService(service);
                    IPcleanup.printMap();
                  }

			}
			else if (cleanIpUsingOrderId.equalsIgnoreCase("YES")) {
                System.out.println("\n\nCLEANING STARTED FOR IPs USING ORDER ID as Input");
                                System.out.println(" -------------------------------------------------------------------------");
                                System.out.println("                              BEGIN                                       ");
                                System.out.println(" -------------------------------------------------------------------------\n");
                                
                                for (int i = 1; i < rowTotal; i++) {
                					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                					System.out.println("Cleanup Started for ID :: " + wb.getSheetAt(0).getRow(i).getCell(0).getRawValue());
                					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//                					System.out.println("SERVICE ID :: "+wb.getSheetAt(0).getRow(i).getCell(0));
                					String service = wb.getSheetAt(0).getRow(i).getCell(0).getRawValue();
                					IPcleanup.cleanIp(service);
                                  }
                                
                                
                                
			}

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
		cellASRI_STATUS.setCellValue("ASRI/ARM_STATUS");

		Cell cellIP_STATUS = row.createCell(4);
		cellIP_STATUS.setCellStyle(cellStyle);
		cellIP_STATUS.setCellValue("IP_STATUS");

		Cell cellREQUEST_ID = row.createCell(5);
		cellREQUEST_ID.setCellStyle(cellStyle);
		cellREQUEST_ID.setCellValue("REQUEST_ID");

		Cell cellDEACTIVATE_JOB_ID = row.createCell(6);
		cellDEACTIVATE_JOB_ID.setCellStyle(cellStyle);
		cellDEACTIVATE_JOB_ID.setCellValue("AUTOPILOT_JOB_ID");

		Cell cellACT_ID = row.createCell(7);
		cellACT_ID.setCellStyle(cellStyle);
		cellACT_ID.setCellValue("ACT_ID");

		Cell cellERROR = row.createCell(8);
		cellERROR.setCellStyle(cellStyle);
		cellERROR.setCellValue("ERROR");

	}

}
