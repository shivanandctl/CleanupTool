package stack;

import static io.restassured.RestAssured.given;
import utilities.ExcelUtility;
import stack.ServiceCleanupUtility;
import stack.AUTOPILOT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.json.XML;

import com.jayway.jsonpath.JsonPath;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class IPcleanup {

	// constructor
	public IPcleanup() {

	}

	public static String Test1_authUrl = "https://act-env1.corp.intranet/ac-ip-rs-web/rs/auth";
	public static String Test1_fetch = "https://act-env1.corp.intranet/ac-ip-rs-web/rs/view/default/data?q0=";
	public static String Test1_fetchDetailsFromReqId = "http://act-env1.corp.intranet/ac-ip-rs-web/rs/requestPayload?requestID=";

	public static String Test2_authUrl = "https://act-env2.corp.intranet/ac-ip-rs-web/rs/auth";
	public static String Test2_fetch = "https://act-env2.corp.intranet/ac-ip-rs-web/rs/view/default/data?q0=";
	public static String Test2_fetchDetailsFromReqId = "http://act-env2.corp.intranet/ac-ip-rs-web/rs/requestPayload?requestID=";

	public static String Test4_authUrl = "https://act-env4.corp.intranet/ac-ip-rs-web/rs/auth";
	public static String Test4_fetch = "https://act-env4.corp.intranet/ac-ip-rs-web/rs/view/default/data?q0=";
	public static String Test4_fetchDetailsFromReqId = "http://act-env4.corp.intranet/ac-ip-rs-web/rs/requestPayload?requestID=";

//	public static String Test1_SASI = "https://sasi-test1.kubeodc-test.corp.intranet/inventory/v1/asri/service_type?name=";
//	public static String Test2_SASI = "https://sasi-test2.kubeodc-test.corp.intranet/inventory/v1/asri/service_type?name=";
//	public static String Test4_SASI = "https://sasi-test4.kubeodc-test.corp.intranet/inventory/v1/asri/service_type?name=";
	
	//new SASI URLs
	public static String Test1_SASI = "https://api-test1.test.intranet/Inventory/v1/Resource/sasi/asri/service_type?name=";
	public static String Test2_SASI = "https://api-test2.test.intranet/Inventory/v1/Resource/sasi/asri/service_type?name=";
	public static String Test4_SASI = "https://api-test4.test.intranet/Inventory/v1/Resource/sasi/asri/service_type?name=";
	
	public static String SASI_HEADER_APP_KEY_NAME = "X-Level3-Application-key";
	public static String SASI_HEADER_APP_KEY_VALUE = "APPKEY764872024091808122324561435";
	

//	public static String Test_GET_IP = "https://sasi-sasiwrap-test1.kubeodc.corp.intranet/wrappers/nisws/ipBlocks?circuitId=";
//	public static String Test_IP_Release = "https://sasi-sasiwrap-test1.kubeodc.corp.intranet/wrappers/nisws/ipRelease";
	
	//new SASI URLs for IP cleanup
	public static String Test_GET_IP = "https://api-test1.test.intranet/Inventory/v1/Resource/sasiwrap/nisws/ipBlocks?circuitId=";
	public static String Test_IP_Release = "https://api-test1.test.intranet/Inventory/v1/Resource/sasiwrap/nisws/ipRelease";

	public static String username;
	public static String password;

	public static String inventoryType;
	public static String explicitEnvironment;
	
	public static String cleanIpUsingOrderId;

	public static String environment;
	public static String parentServiceEnvironment;
	static AUTOPILOT ap = new AUTOPILOT();
	static ExcelUtility eu = new ExcelUtility();

	// Write data variables
	public static String exVar_ServiceAlias = "NULL";
	public static String exVar_IpStatus = "NULL";
	public static String exVar_AsriStatus = "NULL";
	public static String exVar_ActStatus = "NULL";
	public static Integer exVar_RequestId = 0;
	public static String exVar_IdentifierId = "NULL";
	public static String exVar_ActId = "NULL";
	public static String exVar_Error = "NULL";
	public static String exVar_DeactivateJobId = "NULL";
	public static String exVar_Environment = "NULL";

	public static ArrayList<Object> exRowData = new ArrayList();
	public static LinkedHashMap<String, Object> exData = new LinkedHashMap<String, Object>();

	//
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	public static String dateTime = sdf.format(new Date());

	// file system

	public static String fileDir = System.getProperty("user.dir");
	public static PrintStream myconsole;

	public static void main(String[] args) throws InterruptedException {

		// main line down
		eu.readExcelData();
		// read circuitIds from csv
//		eu.getCircuitIDs();

		//
//		IPcleanup.deleteServicefromInventory("CO/KXFN/046963/LUMN","services");
//		deleteServicefromArm("29/KXGS/667107//MS","services");

	}

	public static boolean deactivateService(String serviceID) {

		ArrayList<String> actDeactivate;
		ArrayList<String> act;
		String serviceAlias = serviceID;
		exVar_ServiceAlias = serviceAlias;
		exData.put("colService", exVar_ServiceAlias);
		boolean isCleaned = false;
		// checking for successful delete request
		System.out.println("\n=======================ACT CLEANUP START============================");
		System.out.println("------------checking for successful \"delete\" request------------------");
		act = getJSONresponseFromServiceAlias(serviceAlias, "delete");

		if (act.size() == 0) {
			System.out.println("\n------------checking for successful \"deactivate\" request---------------");
			actDeactivate = getJSONresponseFromServiceAlias(serviceAlias, "deactivate");
			if (actDeactivate.size() > 0 && actDeactivate.get(0) != null) {
				System.out.println("Service already deactivated!!\nActID::" + actDeactivate.get(0));
				exVar_ActStatus = "ALREADY CLEANED";
				isCleaned = true;
				exVar_ActId = actDeactivate.get(0);
				exData.put("colActStatus", exVar_ActStatus);
				exData.put("colActId", "Already Deactivate::" + exVar_ActId);
				exData.put("colError", "NULL");
				exData.put("colDeactivateJobId", "NULL");
			} else {
				System.out.println("No successful \"delete\" or \"deactivate\" request found!!!");
				isCleaned = deleteInternetServiceFromACT(serviceID);

			}
		} else {
			System.out.println("Service already deleted!!\nActID::" + act.get(0));
			exVar_ActStatus = "ALREADY CLEANED";
			isCleaned = true;
			exVar_ActId = act.get(0);
			exData.put("colActStatus", exVar_ActStatus);
			exData.put("colActId", "Already Delete::" + exVar_ActId);
			exData.put("colError", "NULL");
			exData.put("colDeactivateJobId", "NULL");
		}

		System.out.println(
				"==============================================ACT CLEANUP END===================================================\n\n");
		return isCleaned;
	}

	public static void deleteDeactivateServiceFromACT(String serviceID) {

		ArrayList<String> actDeactivate;
		ArrayList<String> act;
		String serviceAlias = serviceID;
		exVar_ServiceAlias = serviceAlias;
		exData.put("colService", exVar_ServiceAlias);
		// checking for successful delete request
		System.out.println(
				"\n==============================================ACT CLEANUP START===================================================");

		{
			exData.put("colActStatus", "NOT FOUND");
			System.out.println("\n-----------Checking for successful \"new\" request---------------");
			act = getJSONresponseFromServiceAlias(serviceAlias, "new");
			if (act.size() > 0) {
				String token = ap.getToken(username, password);
//				System.out.println("Logging into Autopilot\nToken generated::"+token+"\nLogged in Successfully");
//				System.out.println("Logging into Autopilot\nLogged in Successfully");
				String jobid_ = ap.triggerWorkflow(act.get(0), act.get(1), "LNAAS_DELETE_TRANSACTION_ACT_TL_V1", token);
				System.out.println("Triggering workflow::\"LNAAS_DELETE_TRANSACTION_ACT_TL_V1\"\nJob id::" + jobid_);
				exVar_DeactivateJobId = jobid_;
				exData.put("colDeactivateJobId", exVar_DeactivateJobId);
				try {
					System.out.println("Is Workflow completed?::" + ap.getWorkflowStatus(jobid_, token));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					String errorString = ap.getTaskDetail(jobid_, "e5b9", "$..outgoing.return_value", token);
					if (errorString != null) {
						System.out.println("Delete Transaction from ACT completed Successfully");
						String successDelActId = ap.getTaskDetail(jobid_, "14bc", "$..actIdentifierId", token);
						exData.put("colActStatus", "CLEANED");
						exData.put("colRequestId", "NULL");
						exData.put("colActId", "Delete::" + successDelActId);
					} else {
						errorString = ap.getTaskDetail(jobid_, "51ed", "$..outgoing..message", token);
						String delActId = ap.getTaskDetail(jobid_, "51ed", "$..outgoing..actID", token);
						System.out.println("DELETE Transaction failed with error::");
						System.out.println(
								"+-----------+---------+-----------+---------+-----------+---------+-----------+---------+-----------+---------+");
						System.out.println(errorString);
						System.out.println(
								"+-----------+---------+-----------+---------+-----------+---------+-----------+---------+-----------+---------+");
						exVar_Error = errorString;
						exData.put("colError", exVar_Error);

						exVar_ActStatus = "NOT CLEANED";
						exVar_ActId = delActId;
						exData.put("colActStatus", exVar_ActStatus);
						exData.put("colActId", "Delete::" + exVar_ActId);
						exData.put("colRequestId", "NULL");

						// Triggering DEACTIVATE workflow as we got error using DELETE workflow
						if (errorString != null || AUTOPILOT.iterationCount >= 20) {
							System.out.println(
									"\n===Triggering DEACTIVATE workflow as we got error using DELETE workflow===\n");
							jobid_ = ap.triggerWorkflow(act.get(0), act.get(1), "Deactivate_Transaction_ACT", token);
							System.out
									.println("Triggering workflow::\"DEACTIVATE_TRANSACTION_ACT\"\nJob id::" + jobid_);
							exVar_DeactivateJobId = jobid_;
							exData.put("colDeactivateJobId", "Deactivate::" + exVar_DeactivateJobId);
							try {
								System.out.println("Is Workflow completed?::" + ap.getWorkflowStatus(jobid_, token));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							try {
								errorString = ap.getTaskDetail(jobid_, "2375", "$..outgoing.return_value", token);
								if (errorString != null) {
									System.out.println("Deactivation completed Successfully");
//									String successDelActId = ap.getTaskDetail(jobid_, "662a","$..actIdentifierId", token);
									exData.put("colActStatus", "CLEANED");
									exData.put("colRequestId", "NULL");
									exData.put("colActId", "NULL");
									exData.put("colError", "NULL");
								} else {
									errorString = ap.getTaskDetail(jobid_, "51ed", "$..outgoing..response", token);
//									delActId = ap.getTaskDetail(jobid_, "51ed","$..outgoing..actID", token);
									System.out.println("Deactivation failed with error::");
									System.out.println("............................................................");
									System.out.println(errorString);
									System.out.println("............................................................");
									exVar_Error = errorString;
									exData.put("colError", exVar_Error);

									exVar_ActStatus = "NOT CLEANED";
									exVar_ActId = delActId;
									exData.put("colActStatus", exVar_ActStatus);
									exData.put("colActId", "DEACTIVATE::" + "NULL");
									exData.put("colRequestId", "DEACTIVATE::" + "NULL");

								}
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}

					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				exData.put("colActStatus", "NOT FOUND");
			}
		}

		System.out.println(
				"==============================================ACT CLEANUP END===================================================\n\n");

	}

	public static boolean deleteInternetServiceFromACT(String serviceID) {

		ArrayList<String> actDeactivate;
		ArrayList<String> act;
		boolean internetServiceCleaned = false;
		String serviceAlias = serviceID;
		exVar_ServiceAlias = serviceAlias;
		exData.put("colService", exVar_ServiceAlias);
		exData.put("colError", "NULL");
		// checking for successful delete request
		System.out.println(
				"\n==============================================ACT CLEANUP START===================================================");

		{
			exData.put("colActStatus", "NOT FOUND");
			System.out.println("\n-----------Checking for successful \"new\" request---------------");
			act = getJSONresponseFromServiceAlias(serviceAlias, "new");
			if (act.size() > 0) {
				String token = ap.getToken(username, password);
//				System.out.println("Logging into Autopilot\nToken generated::"+token+"\nLogged in Successfully");
//				System.out.println("Logging into Autopilot\nLogged in Successfully");
				String jobid_ = ap.triggerWorkflow(act.get(0), act.get(1), "LNAAS_DELETE_TRANSACTION_ACT_TL_V1", token);
				System.out.println("Triggering workflow::\"LNAAS_DELETE_TRANSACTION_ACT_TL_V1\"\nJob id::" + jobid_);
				exVar_DeactivateJobId = jobid_;
				exData.put("colDeactivateJobId", exVar_DeactivateJobId);
				try {
					System.out.println("Is Workflow completed?::" + ap.getWorkflowStatus(jobid_, token));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					String errorString = ap.getTaskDetail(jobid_, "e5b9", "$..outgoing.return_value", token);
					if (errorString != null) {
						System.out.println("Delete Transaction from ACT completed Successfully");
						String successDelActId = ap.getTaskDetail(jobid_, "14bc", "$..actIdentifierId", token);
						exData.put("colActStatus", "CLEANED");
						exData.put("colRequestId", "NULL");
						exData.put("colActId", "Delete::" + successDelActId);
						internetServiceCleaned = true;
					} else {
						errorString = ap.getTaskDetail(jobid_, "51ed", "$..outgoing..message", token);
						String delActId = ap.getTaskDetail(jobid_, "51ed", "$..outgoing..actID", token);
						System.out.println("DELETE Transaction failed with error::");
						System.out.println(
								"+-----------+---------+-----------+---------+-----------+---------+-----------+---------+-----------+---------+");
						System.out.println(errorString);
						System.out.println(
								"+-----------+---------+-----------+---------+-----------+---------+-----------+---------+-----------+---------+");
						exVar_Error = errorString;
						exData.put("colError", exVar_Error);
						exVar_ActStatus = "NOT CLEANED";
						exVar_ActId = delActId;
						exData.put("colActStatus", exVar_ActStatus);
						exData.put("colActId", exVar_ActId);
						exData.put("colRequestId", "NULL");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				exData.put("colActStatus", "NOT FOUND");
				exData.put("colError", "NULL");
				internetServiceCleaned = true;
			}
		}

		System.out.println(
				"==============================================ACT CLEANUP END===================================================\n\n");
		return internetServiceCleaned;
	}

	public static boolean deleteServicefromAsri(String serviceID, String serviceType) {
		System.out.println(
				"==============================================ASRI CLEANUP START=================================================");
		boolean serviceCleanedInAsri = false;
		boolean isInternetService = false;
		String resolvedUrl = Test1_SASI.replaceAll("service_type", serviceType);
		resolvedUrl = resolvedUrl + serviceID;
//		System.out.println(resolvedUrl);
		String serviceBody = given().relaxedHTTPSValidation().header(SASI_HEADER_APP_KEY_NAME, SASI_HEADER_APP_KEY_VALUE).get(resolvedUrl).body().asString();
		ArrayList<String> resourceId = JsonPath.read(serviceBody, "$..resources[*].id");
		ArrayList<String> resourceType = JsonPath.read(serviceBody, "$..resources[0].type");
		if (resourceId.size() > 0) {
			System.out.println("Service found in TEST1\nresourceId::" + resourceId.get(0));
			String[] deleteUrl = Test1_SASI.split("service_type");
			String resolvedDeleteUrl = deleteUrl[0] + serviceType + "/".concat(resourceId.get(0));
			System.out.println(resolvedDeleteUrl);
			String delResBody = given().relaxedHTTPSValidation().header(SASI_HEADER_APP_KEY_NAME, SASI_HEADER_APP_KEY_VALUE).delete(resolvedDeleteUrl).body().asString();
			System.out.println(delResBody);
			if (delResBody.contains("successfully")) {
				serviceCleanedInAsri = true;
				exVar_AsriStatus = "CLEANED";
				exData.put("colAsri_Status", exVar_AsriStatus);
				if (exVar_Environment.equalsIgnoreCase("NULL")) {
					exVar_Environment = "TEST1";
					exData.put("colEnvironment", exVar_Environment);
				}
				// updation for endpoint
				if (serviceID.contains("_")) {
					exData.put("colService", serviceID);
					exData.put("colRequestId", "NULL");
					exData.put("colActStatus", "NULL");
					exData.put("colActId", "NULL");
					exData.put("colDeactivateJobId", "NULL");
					exData.put("colError", "NULL");
				}

			} else {

				exVar_AsriStatus = "NOT CLEANED";
				exData.put("colAsri_Status", exVar_AsriStatus);
				if (serviceID.contains("_")) {
					exData.put("colService", serviceID);
					exData.put("colRequestId", "NULL");
					exData.put("colActStatus", "NULL");
					exData.put("colActId", "NULL");
					exData.put("colDeactivateJobId", "NULL");
					exData.put("colError", "NULL");
				}
			}
		} else {
			System.out.println("Service not found in Test1..checking in Test4");
			resolvedUrl = Test4_SASI.replaceAll("service_type", serviceType);
			resolvedUrl = resolvedUrl + serviceID;
//			System.out.println(resolvedUrl);
			serviceBody = given().relaxedHTTPSValidation().header(SASI_HEADER_APP_KEY_NAME, SASI_HEADER_APP_KEY_VALUE).get(resolvedUrl).body().asString();
			resourceId = JsonPath.read(serviceBody, "$..resources[*].id");
			resourceType = JsonPath.read(serviceBody, "$..resources[0].type");
			if (resourceId.size() > 0) {
				System.out.println("Service found in TEST4\nresourceId::" + resourceId.get(0));
				String[] deleteUrl = Test4_SASI.split("service_type");
				String resolvedDeleteUrl = deleteUrl[0] + serviceType + "/".concat(resourceId.get(0));
//				System.out.println(resolvedDeleteUrl);
				String delResBody = given().relaxedHTTPSValidation().header(SASI_HEADER_APP_KEY_NAME, SASI_HEADER_APP_KEY_VALUE).delete(resolvedDeleteUrl).body().asString();
				System.out.println(delResBody);
				if (delResBody.contains("successfully")) {
					serviceCleanedInAsri = true;
					exVar_AsriStatus = "CLEANED";
					exData.put("colAsri_Status", exVar_AsriStatus);
					if (exVar_Environment.equalsIgnoreCase("NULL")) {
						exVar_Environment = "TEST4";
						exData.put("colEnvironment", exVar_Environment);
					}
					if (serviceID.contains("_")) {
						exData.put("colService", serviceID);
						exData.put("colRequestId", "NULL");
						exData.put("colActStatus", "NULL");
						exData.put("colActId", "NULL");
						exData.put("colDeactivateJobId", "NULL");
						exData.put("colError", "NULL");
						exData.put("colIP_Status", "NULL");
					}
				}
			} else {
				System.out.println("Service not found in Test4 also");
				exVar_AsriStatus = "NOT FOUND/CLEANED";
				exData.put("colAsri_Status", exVar_AsriStatus);
				if (serviceID.contains("_")) {
					exData.put("colService", serviceID);
					exData.put("colRequestId", "NULL");
					exData.put("colActStatus", "NULL");
					exData.put("colActId", "NULL");
					exData.put("colDeactivateJobId", "NULL");
					exData.put("colError", "NULL");
					exData.put("colIP_Status", "NULL");
				}

			}
		}
		System.out.println(
				"==============================================ASRI CLEANUP END===================================================\n\n");
		if (resourceType.size() > 0) {
			if (resourceType.get(0).contains("Internet Access")) {
				isInternetService = true;
			}
		}
		return isInternetService;

	}

	public static boolean deleteServicefromArm(String serviceID, String serviceType) {
		System.out.println(
				"==============================================ASRI CLEANUP START=================================================");
		boolean serviceCleanedInAsri = false;
		boolean isInternetService = false;
		String resolvedUrl = Test1_SASI.replaceAll("service_type", serviceType);
		resolvedUrl = resolvedUrl + serviceID;
//		System.out.println(resolvedUrl);
		String serviceBody = given().relaxedHTTPSValidation().header(SASI_HEADER_APP_KEY_NAME, SASI_HEADER_APP_KEY_VALUE).get(resolvedUrl).body().asString();
		ArrayList<String> resourceId = JsonPath.read(serviceBody, "$..resources[*].id");
		ArrayList<String> resourceType = JsonPath.read(serviceBody, "$..resources[0].type");
		if (resourceId.size() > 0) {
			System.out.println("Service found in TEST1\nresourceId::" + resourceId.get(0));
			String[] deleteUrl = Test1_SASI.split("service_type");
			String resolvedDeleteUrl = deleteUrl[0] + serviceType + "/".concat(resourceId.get(0));
			System.out.println(resolvedDeleteUrl);
			String delResBody = given().relaxedHTTPSValidation().header(SASI_HEADER_APP_KEY_NAME, SASI_HEADER_APP_KEY_VALUE).delete(resolvedDeleteUrl).body().asString();
			System.out.println(delResBody);
			if (delResBody.contains("successfully")) {
				serviceCleanedInAsri = true;
				exVar_AsriStatus = "CLEANED";
				exData.put("colAsri_Status", exVar_AsriStatus);
				if (exVar_Environment.equalsIgnoreCase("NULL")) {
					exVar_Environment = "TEST1";
					exData.put("colEnvironment", exVar_Environment);
				}
				// updation for endpoint
				if (serviceID.contains("_")) {
					exData.put("colService", serviceID);
					exData.put("colRequestId", "NULL");
					exData.put("colActStatus", "NULL");
					exData.put("colActId", "NULL");
					exData.put("colDeactivateJobId", "NULL");
					exData.put("colError", "NULL");
				}

			} else {

				exVar_AsriStatus = "NOT CLEANED";
				exData.put("colAsri_Status", exVar_AsriStatus);
				if (serviceID.contains("_")) {
					exData.put("colService", serviceID);
					exData.put("colRequestId", "NULL");
					exData.put("colActStatus", "NULL");
					exData.put("colActId", "NULL");
					exData.put("colDeactivateJobId", "NULL");
					exData.put("colError", "NULL");
				}
			}
		} else {
			System.out.println("Service not found in Test1..checking in Test4");
			resolvedUrl = Test4_SASI.replaceAll("service_type", serviceType);
			resolvedUrl = resolvedUrl + serviceID;
//			System.out.println(resolvedUrl);
			serviceBody = given().relaxedHTTPSValidation().header(SASI_HEADER_APP_KEY_NAME, SASI_HEADER_APP_KEY_VALUE).get(resolvedUrl).body().asString();
			resourceId = JsonPath.read(serviceBody, "$..resources[*].id");
			resourceType = JsonPath.read(serviceBody, "$..resources[0].type");
			if (resourceId.size() > 0) {
				System.out.println("Service found in TEST4\nresourceId::" + resourceId.get(0));
				String[] deleteUrl = Test4_SASI.split("service_type");
				String resolvedDeleteUrl = deleteUrl[0] + serviceType + "/".concat(resourceId.get(0));
//				System.out.println(resolvedDeleteUrl);
				String delResBody = given().relaxedHTTPSValidation().header(SASI_HEADER_APP_KEY_NAME, SASI_HEADER_APP_KEY_VALUE).delete(resolvedDeleteUrl).body().asString();
				System.out.println(delResBody);
				if (delResBody.contains("successfully")) {
					serviceCleanedInAsri = true;
					exVar_AsriStatus = "CLEANED";
					exData.put("colAsri_Status", exVar_AsriStatus);
					if (exVar_Environment.equalsIgnoreCase("NULL")) {
						exVar_Environment = "TEST4";
						exData.put("colEnvironment", exVar_Environment);
					}
					if (serviceID.contains("_")) {
						exData.put("colService", serviceID);
						exData.put("colRequestId", "NULL");
						exData.put("colActStatus", "NULL");
						exData.put("colActId", "NULL");
						exData.put("colDeactivateJobId", "NULL");
						exData.put("colError", "NULL");
						exData.put("colIP_Status", "NULL");
					}
				}
			} else {
				System.out.println("Service not found in Test4 also");
				exVar_AsriStatus = "NOT FOUND/CLEANED";
				exData.put("colAsri_Status", exVar_AsriStatus);
				if (serviceID.contains("_")) {
					exData.put("colService", serviceID);
					exData.put("colRequestId", "NULL");
					exData.put("colActStatus", "NULL");
					exData.put("colActId", "NULL");
					exData.put("colDeactivateJobId", "NULL");
					exData.put("colError", "NULL");
					exData.put("colIP_Status", "NULL");
				}

			}
		}
		System.out.println(
				"==============================================ASRI CLEANUP END===================================================\n\n");
		if (resourceType.size() > 0) {
			if (resourceType.get(0).contains("Internet Access")) {
				isInternetService = true;
			}
		}
		return isInternetService;

	}

	public static boolean cleanIp(String serviceID) {
		System.out.println(
				"==============================================IP CLEANUP START=================================================");
		boolean isIpCleaned = false;
		String resolvedIpUrl = Test_GET_IP + serviceID;
		String iPResBody = given().relaxedHTTPSValidation().header(SASI_HEADER_APP_KEY_NAME, SASI_HEADER_APP_KEY_VALUE).get(resolvedIpUrl).body().asString();
		ArrayList<String> ipList = JsonPath.read(iPResBody, "$..ipBlock..cidrRange");
		if (ipList.size() > 0) {
			System.out.println("IPs Found!!\nNumber of IPs occupied by " + serviceID + " is::" + ipList.size());
			for (String ip : ipList) {
				System.out.println("Releasing IP::" + ip);
				String ipReleasePayload = "{\r\n" + "    \"circuitId\" : \"" + serviceID + "\",\r\n"
						+ "    \"cidrRange\" : \"" + ip + "\"\r\n" + "}";
				System.out.println(ipReleasePayload);
				String ipReleaseResponse = given().relaxedHTTPSValidation().header("Content-type", "application/json")
						.header(SASI_HEADER_APP_KEY_NAME, SASI_HEADER_APP_KEY_VALUE)
						.and().body(ipReleasePayload).when().post(Test_IP_Release).then().extract().response()
						.asString();
				System.out.println(ipReleaseResponse);
				ArrayList<String> ipReleaseList = JsonPath.read(ipReleaseResponse, "$..errorMessage");
				if (ipReleaseList.size() > 0) {
					System.out.println(ipReleaseList.get(0));
					ipReleaseList.get(0).equalsIgnoreCase("SUCCESS");
					exVar_IpStatus = "CLEANED";
					exData.put("colIP_Status", exVar_IpStatus);
				}
			}
		} else {
			System.out.println("No IPs occupied by the service::" + serviceID);
			exVar_IpStatus = "NOT OCCUPIED/CLEANED";
			exData.put("colIP_Status", exVar_IpStatus);
		}
		System.out.println(
				"==============================================IP CLEANUP END===================================================\n\n");
		return isIpCleaned;

	}

	public static ArrayList<Map<String, String>> actAuthentication() {

		// Set basic authentication header
		String auth = username + ":" + password;
		String authHeader = "";
		byte[] encodedAuth;
		ArrayList<Map<String, String>> cookiesMap = new ArrayList<Map<String, String>>();
		try {
			encodedAuth = Base64.encodeBase64(auth.getBytes("UTF-8"));
			authHeader = new String(encodedAuth);
			Response Test1_response = given().relaxedHTTPSValidation()
					.header("authorization", authHeader).get(Test1_authUrl);
			Map<String, String> Test1_cook = Test1_response.cookies();

			Response Test4_response = given().relaxedHTTPSValidation()
					.header("authorization", authHeader).get(Test4_authUrl);
			Map<String, String> Test4_cook = Test4_response.cookies();

			Response Test2_response = given().relaxedHTTPSValidation()
					.header("authorization", authHeader).get(Test2_authUrl);
			Map<String, String> Test2_cook = Test2_response.cookies();

			cookiesMap.add(Test1_cook);
			cookiesMap.add(Test4_cook);
			cookiesMap.add(Test2_cook);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cookiesMap;
	}

	public static ArrayList<String> getJSONresponseFromServiceAlias(String serviceAlias, String activity) {
		ArrayList<Map<String, String>> cookiesMap = actAuthentication();
		ArrayList<String> actInfo = new ArrayList<String>();
		Integer requestID = null;
		String identifier_id = null;
		String header_identifier = null;

		String x = given().cookies(cookiesMap.get(0)).when().get(Test1_fetch + serviceAlias).body().asString();
		JSONObject xmlJSONObj = XML.toJSONObject(x);
		String jsonPrettyPrintString1 = xmlJSONObj.toString(4);
//		System.out.println(jsonPrettyPrintString1);
//		requestID = getRequestID(jsonPrettyPrintString1, activity);
		// enhanacement for requestID
		ArrayList<String> requestIDs = getRequestIDs("serviceAlias", "1");
		if (requestIDs.size() > 0) {
			requestID = Integer.parseInt(requestIDs.get(0).split("&&")[0]);
			activity = requestIDs.get(0).split("&&")[2];

			System.out.println("RequestID found in Test1::" + requestID + " for activity::" + activity);
			System.out.println(
					"ACT Url::" + "http://act-env1.idc1.level3.com:8081/ac-ip-rs-web/#/activation/" + requestID);
			AUTOPILOT.environment = "1";
			exVar_Environment = "TEST1";
			exVar_RequestId = requestID;
			exData.put("colEnvironment", exVar_Environment);
			if (activity.equalsIgnoreCase("delete")) {
				exData.put("colRequestId", "Delete::" + exVar_RequestId);
			} else if (activity.equalsIgnoreCase("deactivate")) {
				exData.put("colRequestId", "Deactivate::" + exVar_RequestId);
			} else {
				exData.put("colRequestId", "Create::" + exVar_RequestId);
			}
			// fetching info using requestID

			x = given().when().get(Test1_fetchDetailsFromReqId + requestID).body().asString();
			xmlJSONObj = XML.toJSONObject(x);
			jsonPrettyPrintString1 = xmlJSONObj.toString(4);
			ArrayList identifier_id_list = JsonPath.read(jsonPrettyPrintString1,
					"$..item[?(@.name=='identifier_id')].value");
			ArrayList header_identifier_list = JsonPath.read(jsonPrettyPrintString1,
					"$..item[?(@.name=='header.identifier')].value");
			if (identifier_id_list.size() > 0 && header_identifier_list.size() > 0) {
				identifier_id = (String) identifier_id_list.get(0);
				header_identifier = String.valueOf(header_identifier_list.get(0));
				actInfo.add(identifier_id);
				actInfo.add(header_identifier);
			}

		} else {
			System.out.println("RequestID not found in Test1, checking in Test4");
			x = given().cookies(cookiesMap.get(1)).when().get(Test4_fetch + serviceAlias).body().asString();
			xmlJSONObj = XML.toJSONObject(x);
			String jsonPrettyPrintString4 = xmlJSONObj.toString(4);
//		        System.out.println("====================================================");
//		        System.out.println(jsonPrettyPrintString4);
//			requestID = getRequestID(jsonPrettyPrintString4, activity);
			// enhanacement for requestID
			requestIDs = getRequestIDs("serviceAlias", "4");
			if (requestIDs.size() > 0) {
				requestID = Integer.parseInt(requestIDs.get(0).split("&&")[0]);
				activity = requestIDs.get(0).split("&&")[2];
				System.out.println("RequestID found in Test4::" + requestID + " for activity::" + activity);
				System.out.println(
						"ACT Url::" + "http://act-env4.idc1.level3.com:8081/ac-ip-rs-web/#/activation/" + requestID);
				AUTOPILOT.environment = "4";
				exVar_Environment = "TEST4";
				exVar_RequestId = requestID;

				exData.put("colEnvironment", exVar_Environment);
//				if (activity.equalsIgnoreCase("delete") || activity.equalsIgnoreCase("deactivate")) {
//					exData.put("colRequestId", "Delete::" + exVar_RequestId);
//				} else {
//					exData.put("colRequestId", "Create::" + exVar_RequestId);
//				}
				if (activity.equalsIgnoreCase("delete")) {
					exData.put("colRequestId", "Delete::" + exVar_RequestId);
				} else if (activity.equalsIgnoreCase("deactivate")) {
					exData.put("colRequestId", "Deactivate::" + exVar_RequestId);
				} else {
					exData.put("colRequestId", "Create::" + exVar_RequestId);
				}

				x = given().when().get(Test4_fetchDetailsFromReqId + requestID).body().asString();
				xmlJSONObj = XML.toJSONObject(x);
				jsonPrettyPrintString4 = xmlJSONObj.toString(4);
				ArrayList identifier_id_list = JsonPath.read(jsonPrettyPrintString4,
						"$..item[?(@.name=='identifier_id')].value");
				ArrayList header_identifier_list = JsonPath.read(jsonPrettyPrintString4,
						"$..item[?(@.name=='header.identifier')].value");
				if (identifier_id_list.size() > 0 && header_identifier_list.size() > 0) {
					identifier_id = (String) identifier_id_list.get(0);
					header_identifier = String.valueOf(header_identifier_list.get(0));
					actInfo.add(identifier_id);
					actInfo.add(header_identifier);
				}

			} else {
				System.out.println("RequestID not found in Test4, checking in Test2");
				x = given().cookies(cookiesMap.get(2)).when().get(Test2_fetch + serviceAlias).body().asString();
				xmlJSONObj = XML.toJSONObject(x);
				String jsonPrettyPrintString2 = xmlJSONObj.toString(4);
				// System.out.println("====================================================");
				// System.out.println(jsonPrettyPrintString2);
//				requestID = getRequestID(jsonPrettyPrintString2, activity);
				// enhanacement for requestID
				requestIDs = getRequestIDs("serviceAlias", "2");
				if (requestIDs.size() > 0) {
					requestID = Integer.parseInt(requestIDs.get(0).split("&&")[0]);
					activity = requestIDs.get(0).split("&&")[2];
					System.out.println("RequestID found in Test2::" + requestID + " for activity::" + activity);
					System.out.println("ACT Url::" + "http://act-env2.idc1.level3.com:8081/ac-ip-rs-web/#/activation/"
							+ requestID);
					AUTOPILOT.environment = "2";
					exVar_Environment = "TEST2";
					exVar_RequestId = requestID;
					exData.put("colEnvironment", exVar_Environment);
					if (activity.equalsIgnoreCase("delete")) {
						exData.put("colRequestId", "Delete::" + exVar_RequestId);
					} else if (activity.equalsIgnoreCase("deactivate")) {
						exData.put("colRequestId", "Deactivate::" + exVar_RequestId);
					} else {
						exData.put("colRequestId", "Create::" + exVar_RequestId);
					}
					x = given().when().get(Test2_fetchDetailsFromReqId + requestID).body().asString();
					xmlJSONObj = XML.toJSONObject(x);
					jsonPrettyPrintString2 = xmlJSONObj.toString(4);
					ArrayList identifier_id_list = JsonPath.read(jsonPrettyPrintString2,
							"$..item[?(@.name=='identifier_id')].value");
					ArrayList header_identifier_list = JsonPath.read(jsonPrettyPrintString2,
							"$..item[?(@.name=='header.identifier')].value");
					if (identifier_id_list.size() > 0 && header_identifier_list.size() > 0) {
						identifier_id = (String) identifier_id_list.get(0);
						header_identifier = String.valueOf(header_identifier_list.get(0));
						actInfo.add(identifier_id);
						actInfo.add(header_identifier);
					}

				} else {
					System.out.println("RequestID not found in Test2 also");

					exVar_IpStatus = "NULL";
					exVar_AsriStatus = "NULL";
					exVar_ActStatus = "NULL";
					exVar_RequestId = 0;
					exVar_IdentifierId = "NULL";
					exVar_ActId = "NULL";
					exVar_Error = "NULL";
					exVar_DeactivateJobId = "NULL";
					exVar_Environment = "NULL";

					exData.put("colEnvironment", exVar_Environment);
					exData.put("colRequestId", exVar_RequestId);
					exData.put("colActStatus", exVar_ActStatus);
					exData.put("colActId", exVar_ActStatus);
					exData.put("colDeactivateJobId", exVar_DeactivateJobId);
					exData.put("colError", exVar_Error);

				}
			}
		}

		return actInfo;

	}

	public static Integer getRequestID(String jsonPrettyPrintString, String activity) {
		ArrayList requestIdLength = JsonPath.read(jsonPrettyPrintString, "$..requestID");
//		 System.out.println(jsonPrettyPrintString);
		Integer requestID = null;
		if (requestIdLength.size() == 1) {
			ArrayList<String> requestActivity = JsonPath.read(jsonPrettyPrintString, "$..data[3].data");
			if (requestActivity.size() > 0
					&& (activity.equalsIgnoreCase("delete") || activity.equalsIgnoreCase("deactivate"))) {
				String activityFound = requestActivity.get(0);
				if (activityFound.equalsIgnoreCase("new")) {
					requestID = null;
				} else {
					requestID = (Integer) requestIdLength.get(0);
				}
			} else {
				// requestID = (Integer) requestIdLength.get(0);
				{
					ArrayList isRequestType = JsonPath.read(jsonPrettyPrintString, "$..data[3].data");
					ArrayList isRequestComplete = JsonPath.read(jsonPrettyPrintString, "$..data[0].data");
					ArrayList idNewRequest = null;
					String reqType = null;
					String reqComplete = null;

					if (isRequestType.size() > 0) {
						reqType = (String) isRequestType.get(0);
						reqComplete = (String) isRequestComplete.get(0);
						if (reqType.equalsIgnoreCase(activity) && reqComplete.equalsIgnoreCase("complete")) {
							idNewRequest = JsonPath.read(jsonPrettyPrintString, "$..requestID");
							if (idNewRequest.size() > 0) {
								requestID = (Integer) idNewRequest.get(0);
							}
						}
					}
				}
			}
			//
			// checking for delete request only
		} else {
			ArrayList givenServiceType = JsonPath.read(jsonPrettyPrintString, "$..data[5].data");
			int serviceTypeSize = givenServiceType.size();
			if (serviceTypeSize > 0) {
				String serviceTypeName = (String) givenServiceType.get(serviceTypeSize - 1);
//				System.out.println("Given input is of Type::"+serviceTypeName);

				for (int i = 0; i < requestIdLength.size(); i++) {
					ArrayList isRequestType = JsonPath.read(jsonPrettyPrintString, "$..rows[" + i + "]..data[3].data");
					ArrayList isRequestComplete = JsonPath.read(jsonPrettyPrintString,
							"$..rows[" + i + "]..data[0].data");
					ArrayList<String> serviceType = JsonPath.read(jsonPrettyPrintString,
							"$..rows[" + i + "]..data[5].data");

					ArrayList idNewRequest = null;
					String reqType = null;
					String reqComplete = null;

					if (isRequestType.size() > 0) {
						reqType = (String) isRequestType.get(0);
						reqComplete = (String) isRequestComplete.get(0);
						if (activity.equalsIgnoreCase("deactivate")) {
							if (reqType.equalsIgnoreCase(activity) && reqComplete.equalsIgnoreCase("complete")) {
								idNewRequest = JsonPath.read(jsonPrettyPrintString, "$..rows[" + i + "]..requestID");
								if (idNewRequest.size() > 0) {
									System.out.println("Given input is of Type::" + serviceTypeName);
									requestID = (Integer) idNewRequest.get(0);
									break;
								}
							}
						} else {
							if (reqType.equalsIgnoreCase(activity) && reqComplete.equalsIgnoreCase("complete")
									&& serviceTypeName.equalsIgnoreCase(serviceType.get(0))) {
								idNewRequest = JsonPath.read(jsonPrettyPrintString, "$..rows[" + i + "]..requestID");
								if (idNewRequest.size() > 0) {
									System.out.println("Given input is of Type::" + serviceTypeName);
									requestID = (Integer) idNewRequest.get(0);
									break;
								}
							}
						}

//						else if (reqType.equalsIgnoreCase(activity) && reqComplete.equalsIgnoreCase("complete") && serviceTypeName.equalsIgnoreCase("service")) {
//							idNewRequest = JsonPath.read(jsonPrettyPrintString, "$..rows[" + i + "]..requestID");
//							if (idNewRequest.size() > 0) {
//								requestID = (Integer) idNewRequest.get(0);
//							}
//						}
					}
				}
			}

		}

		return requestID;
	}

	public static ArrayList<String> getRequestIDs(String serviceAlias, String environment) {

		ArrayList<Map<String, String>> cookiesMap = actAuthentication();
		ArrayList<String> ReqID_ServiceType_ReqType = new ArrayList<String>();
		String actJsonResponse = null;
		String finalUrl = null;
		
		// prepare complete URL
				if (environment.contains("1")) {
					finalUrl = "https://act-env1.corp.intranet/ac-ip-rs-web/rs/view/default/data?q0=" + serviceAlias;
//					System.out.println("URL for ACT in the environment::"+environment+"::\n"+finalUrl);
					actJsonResponse = RestAssured.given().relaxedHTTPSValidation().cookies(cookiesMap.get(0)).when()
							.get(finalUrl).body().asString();
				} else if (environment.contains("2")) {
					finalUrl = "https://act-env2.corp.intranet/ac-ip-rs-web/rs/view/default/data?q0=" + serviceAlias;
//					System.out.println("URL for ACT in the environment::"+environment+"::\n"+finalUrl);
					actJsonResponse = RestAssured.given().relaxedHTTPSValidation().cookies(cookiesMap.get(2)).when()
							.get(finalUrl).body().asString();
				} else if (environment.contains("4")) {
					finalUrl = "https://act-env4.corp.intranet/ac-ip-rs-web/rs/view/default/data?q0=" + serviceAlias;
//					System.out.println("URL for ACT in the environment::"+environment+"::\n"+finalUrl);
					actJsonResponse = RestAssured.given().relaxedHTTPSValidation().cookies(cookiesMap.get(1)).when()
							.get(finalUrl).body().asString();
				}

//		if (environment.contains("1")) {
//			actJsonResponse = RestAssured.given().cookies(cookiesMap.get(0)).when()
//					.get("http://act-env1.idc1.level3.com:8081/ac-ip-rs-web/rs/view/default/data?q0=" + serviceAlias)
//					.body().asString();
//		} else if (environment.contains("2")) {
//			actJsonResponse = RestAssured.given().cookies(cookiesMap.get(2)).when()
//					.get("http://act-env2.idc1.level3.com:8081/ac-ip-rs-web/rs/view/default/data?q0=" + serviceAlias)
//					.body().asString();
//		} else if (environment.contains("4")) {
//			actJsonResponse = RestAssured.given().cookies(cookiesMap.get(1)).when()
//					.get("http://act-env4.idc1.level3.com:8081/ac-ip-rs-web/rs/view/default/data?q0=" + serviceAlias)
//					.body().asString();
//		}
		
		if (actJsonResponse.contains("Unauthorized")) {
			System.out.println("\n\n\n+-------------------------------------------------+");
			System.out.println("ACT Login Failed..\nPlease check the credentials and try again..");
			System.out.println("+-------------------------------------------------+");
			System.exit(0);
		}

		JSONObject xmlJSONObj = XML.toJSONObject(actJsonResponse);
		String jsonPrettyPrintString = xmlJSONObj.toString(4);

		ArrayList requestIdLength = JsonPath.read(jsonPrettyPrintString, "$..requestID");
		LinkedHashMap<Integer, String> newRequestIdMap = new LinkedHashMap<Integer, String>();
		Integer requestId = null;
		if (requestIdLength.size() == 1) {
			{
				ArrayList isRequestType = JsonPath.read(jsonPrettyPrintString, "$..data[3].data");
				ArrayList isRequestComplete = JsonPath.read(jsonPrettyPrintString, "$..data[0].data");
				ArrayList<String> serviceType = JsonPath.read(jsonPrettyPrintString, "$..data[5].data");
				ArrayList idNewRequest = null;
				String reqType = null;
				String reqComplete = null;
				String serviceTypeName = null;

				if (isRequestType.size() > 0) {
					reqType = (String) isRequestType.get(0);
					reqComplete = (String) isRequestComplete.get(0);
					serviceTypeName = (String) serviceType.get(0);
					if (reqType.equalsIgnoreCase("new") && reqComplete.equalsIgnoreCase("complete")) {
						idNewRequest = JsonPath.read(jsonPrettyPrintString, "$..requestID");
						if (idNewRequest.size() > 0) {
							requestId = (Integer) idNewRequest.get(0);
							newRequestIdMap.put(requestId, serviceTypeName + "_" + reqType);
							ReqID_ServiceType_ReqType
									.add(requestId.toString() + "&&" + serviceTypeName + "&&" + reqType);
						}
					}
				}
			}
		} else {
			ArrayList givenServiceType = JsonPath.read(jsonPrettyPrintString, "$..data[5].data");
			int serviceTypeSize = givenServiceType.size();
			if (serviceTypeSize > 0) {
				String serviceTypeName = (String) givenServiceType.get(serviceTypeSize - 1);
				// System.out.println("Given input is of Type::"+serviceTypeName);

				for (int i = 0; i < requestIdLength.size(); i++) {
					ArrayList isRequestType = JsonPath.read(jsonPrettyPrintString, "$..rows[" + i + "]..data[3].data");
					ArrayList isRequestComplete = JsonPath.read(jsonPrettyPrintString,
							"$..rows[" + i + "]..data[0].data");
					ArrayList<String> serviceType = JsonPath.read(jsonPrettyPrintString,
							"$..rows[" + i + "]..data[5].data");

					ArrayList idNewRequest = null;
					String reqType = null;
					String reqComplete = null;
					String innerServiceTypeName = null;

					if (isRequestType.size() > 0) {
						reqType = (String) isRequestType.get(0);
						reqComplete = (String) isRequestComplete.get(0);
						innerServiceTypeName = (String) serviceType.get(0);
						// if (reqType.equalsIgnoreCase(activity) &&
						// reqComplete.equalsIgnoreCase("complete")) {
						if (reqComplete.equalsIgnoreCase("complete")
								&& innerServiceTypeName.equalsIgnoreCase(serviceTypeName)
								&& (reqType.equalsIgnoreCase("new") || reqType.equalsIgnoreCase("delete"))) {
							idNewRequest = JsonPath.read(jsonPrettyPrintString, "$..rows[" + i + "]..requestID");
							if (idNewRequest.size() > 0) {
								// System.out.println("Given input is of Type::"+serviceTypeName);
								requestId = (Integer) idNewRequest.get(0);
								newRequestIdMap.put(requestId, innerServiceTypeName + "_" + reqType);
								ReqID_ServiceType_ReqType
										.add(requestId.toString() + "&&" + innerServiceTypeName + "&&" + reqType);
								// System.out.println(serviceTypeName+"::"+requestId+"::"+reqType);
							}
						}
					}
				}
			}

		}

		return ReqID_ServiceType_ReqType;

	}

	public static ArrayList<String> getRequestIDsWithServiceType(String serviceAlias, String environment, String typeOfService, String productType) {

		ArrayList<Map<String, String>> cookiesMap = actAuthentication();
		ArrayList<String> ReqID_ServiceType_ReqType = new ArrayList<String>();
		String actJsonResponse = null;

		if (environment.contains("1")) {
			actJsonResponse = RestAssured.given().cookies(cookiesMap.get(0)).when()
					.get("http://act-env1.idc1.level3.com:8081/ac-ip-rs-web/rs/view/default/data?q0=" + serviceAlias)
					.body().asString();
		} else if (environment.contains("2")) {
			actJsonResponse = RestAssured.given().cookies(cookiesMap.get(2)).when()
					.get("http://act-env2.idc1.level3.com:8081/ac-ip-rs-web/rs/view/default/data?q0=" + serviceAlias)
					.body().asString();
		} else if (environment.contains("4")) {
			actJsonResponse = RestAssured.given().cookies(cookiesMap.get(1)).when()
					.get("http://act-env4.idc1.level3.com:8081/ac-ip-rs-web/rs/view/default/data?q0=" + serviceAlias)
					.body().asString();
		}
		
		if (actJsonResponse.contains("Unauthorized")) {
			System.out.println("\n\n\n+-------------------------------------------------+");
			System.out.println("ACT Login Failed..\nPlease check the credentials and try again..");
			System.out.println("+-------------------------------------------------+");
			System.exit(0);
		}

		JSONObject xmlJSONObj = XML.toJSONObject(actJsonResponse);
		String jsonPrettyPrintString = xmlJSONObj.toString(4);
//		System.out.println(jsonPrettyPrintString);

		ArrayList requestIdLength = JsonPath.read(jsonPrettyPrintString, "$..requestID");
		LinkedHashMap<Integer, String> newRequestIdMap = new LinkedHashMap<Integer, String>();
		Integer requestId = null;
		if (requestIdLength.size() == 1) {
			{
				ArrayList isRequestComplete = JsonPath.read(jsonPrettyPrintString, "$..data[0].data");
				ArrayList isRequestType = JsonPath.read(jsonPrettyPrintString, "$..data[3].data");
				ArrayList isProductType = JsonPath.read(jsonPrettyPrintString, "$..data[4].data");
				ArrayList<String> serviceType = JsonPath.read(jsonPrettyPrintString, "$..data[5].data");
				ArrayList idNewRequest = null;
				String reqType = null;
				String reqComplete = null;
				String serviceTypeName = null;
				String productTypeName = null;

				if (isRequestType.size() > 0) {
					reqType = (String) isRequestType.get(0);
					reqComplete = (String) isRequestComplete.get(0);
					productTypeName = (String) isProductType.get(0);
					serviceTypeName = (String) serviceType.get(0);
					
					if (reqType.equalsIgnoreCase("new") && reqComplete.equalsIgnoreCase("complete") && serviceTypeName.equalsIgnoreCase(typeOfService) && productTypeName.equalsIgnoreCase(productType) ) {
						idNewRequest = JsonPath.read(jsonPrettyPrintString, "$..requestID");
						if (idNewRequest.size() > 0) {
							requestId = (Integer) idNewRequest.get(0);
							newRequestIdMap.put(requestId, serviceTypeName + "_" + reqType);
							ReqID_ServiceType_ReqType
									.add(requestId.toString() + "&&" + serviceTypeName + "&&" + reqType);
						}
					}
				}
			}
		} else {
			ArrayList givenServiceType = JsonPath.read(jsonPrettyPrintString, "$..data[5].data");
			int serviceTypeSize = givenServiceType.size();
			if (serviceTypeSize > 0) {
//				String serviceTypeName = (String) givenServiceType.get(serviceTypeSize - 1);
				// System.out.println("Given input is of Type::"+serviceTypeName);

				for (int i = 0; i < requestIdLength.size(); i++) {
					ArrayList isRequestType = JsonPath.read(jsonPrettyPrintString, "$..rows[" + i + "]..data[3].data");
					ArrayList isRequestComplete = JsonPath.read(jsonPrettyPrintString,
							"$..rows[" + i + "]..data[0].data");
					ArrayList<String> serviceType = JsonPath.read(jsonPrettyPrintString,
							"$..rows[" + i + "]..data[5].data");
					ArrayList isProductType = JsonPath.read(jsonPrettyPrintString, "$..rows[" + i + "]..data[4].data");

					ArrayList idNewRequest = null;
					String reqType = null;
					String reqComplete = null;
					String innerServiceTypeName = null;
					String innerProductTypeName = null;

					if (isRequestType.size() > 0) {
						reqType = (String) isRequestType.get(0);
						reqComplete = (String) isRequestComplete.get(0);
						innerServiceTypeName = (String) serviceType.get(0);
						innerProductTypeName = (String) isProductType.get(0);
						
						// if (reqType.equalsIgnoreCase(activity) &&
						// reqComplete.equalsIgnoreCase("complete")) {
						if (reqComplete.equalsIgnoreCase("complete")
								&& innerServiceTypeName.equalsIgnoreCase(typeOfService)
								&& innerProductTypeName.equalsIgnoreCase(productType)
								&& (reqType.equalsIgnoreCase("new") || reqType.equalsIgnoreCase("delete") || reqType.equalsIgnoreCase("enableDDoS"))) {
							idNewRequest = JsonPath.read(jsonPrettyPrintString, "$..rows[" + i + "]..requestID");
							if (idNewRequest.size() > 0) {
								// System.out.println("Given input is of Type::"+serviceTypeName);
								requestId = (Integer) idNewRequest.get(0);
								newRequestIdMap.put(requestId, innerServiceTypeName + "_" + reqType);
								ReqID_ServiceType_ReqType
										.add(requestId.toString() + "&&" + innerServiceTypeName + "&&" + reqType);
								// System.out.println(serviceTypeName+"::"+requestId+"::"+reqType);
							}
						}
					}
				}
			}

		}

		return ReqID_ServiceType_ReqType;

	}
	
	public static boolean networkCleanup(String service, String env_, String workflow) {

		// Logic to clean up the network
		// If network is cleaned up successfully, return true
		// Else return false
		boolean actCleanupStatus = false;
		// Autopilot autopilot = new Autopilot();
		// set the environment
//		ap.environment = env_;

		exVar_ServiceAlias = service;
		exData.put("colService", exVar_ServiceAlias);
		

		ArrayList<String> envs = new ArrayList<String>();
		if (env_.equalsIgnoreCase("null") || env_.equalsIgnoreCase("") || env_==null) {
			envs.add("1");
			envs.add("2");
			envs.add("4");
		} else {
			envs.add(env_);
		}
		

		// get the request ids
		ArrayList<String> ReqID_ServiceType_ReqType = new ArrayList<String>();

		for (String env : envs) {
			System.out.println("Checking RequestID for the Service::" + service + " in the " + env + " environment");
			ReqID_ServiceType_ReqType = getRequestIDs(service, env);
			//getRequestIDsWithServiceType
			if (ReqID_ServiceType_ReqType.size() > 0) {
				ap.environment = env;
				environment = env;
				System.out.println("RequestID found for the Service::" + service + " in the " + env + " environment");
				break;
			} else {
					System.out.println("RequestID not found  in the " + env + " environment..Checking in the next environment");
					System.out.println(" -------------------------------------------------------------------------");
			}
		}

		if (ReqID_ServiceType_ReqType.size() == 0) {
			
			exData.put("colActStatus", "NOT FOUND");
			exData.put("colActId", "NULL");
			exData.put("colDeactivateJobId", "NULL");
			exData.put("colError", "NULL");
			exData.put("colRequestId", "NULL");
			exData.put("colEnvironment", "NULL");
//			actCleanupStatus = true;
			actCleanupStatus = false;
			return actCleanupStatus;
		} else if (ReqID_ServiceType_ReqType.size() > 0) {
			if (ReqID_ServiceType_ReqType.get(0).contains("delete")) {
				ArrayList<String> actInfo = new ArrayList<String>();
				actInfo = getActDetailsUsingRequestID(ReqID_ServiceType_ReqType.get(0).split("&&")[0], environment);
				if (actInfo.size() > 0) {
					String identifier_id = actInfo.get(0);
					String header_identifier = actInfo.get(1);
					String portName = null;
					String deviceName = null;
					
					try {
						portName = actInfo.get(2);
						deviceName = actInfo.get(3);						
					} catch (Exception e) {
						portName = null;
						deviceName = null;
					}
					
					
					// cleanup the network
					System.out.println("\n=======================ACT CLEANUP START============================");
					System.out.println(
							"Act request found for ServiceID::" + service + " in the environment::" + environment);
					System.out.println("Network cleanup already done for ServiceID::" + service + "\nIDENTIFIER_ID::"
							+ identifier_id + "\nHEADER_IDENTIFIER::" + header_identifier 
							);
					actCleanupStatus = true;
					
//					if(!environment.contains("4")) {
//						System.out.println("\nAs the Network is already cleaned, skipping the network cleanup for the service::"+service + " in the environment::"+environment);
//						if (environment.contains("1")) {
//							boolean test2CleanupStatus = networkCleanup(service, "TEST2", workflow);
//							if (test2CleanupStatus) {
//								boolean test4CleanupStatus = networkCleanup(service, "TEST4", workflow);
//								if (test4CleanupStatus) {
//									exVar_ActStatus = "CLEANED";
//									exVar_ActId = identifier_id;
//									exVar_Environment = environment.toLowerCase().contains("test") ? environment
//											: "TEST" + environment;
//									exData.put("colActStatus", exVar_ActStatus);
//									exData.put("colActId", exVar_ActId);
//									exData.put("colError", "NULL");
//									exData.put("colRequestId", "NULL");
//									exData.put("colEnvironment", exVar_Environment);
//									System.out.println(
//											"\n=======================ACT CLEANUP END============================");
//								} else {
//									exVar_ActStatus = "NOT CLEANED";
//									exVar_ActId = identifier_id;
//									exVar_Environment = environment.toLowerCase().contains("test") ? environment
//											: "TEST" + environment;
//									exData.put("colActStatus", exVar_ActStatus);
//									exData.put("colActId", exVar_ActId);
//									exData.put("colError", "NULL");
//									exData.put("colRequestId", "NULL");
//									exData.put("colEnvironment", exVar_Environment);
//									actCleanupStatus = false;
//									System.out.println(
//											"\n=======================ACT CLEANUP END============================");
//								}
//							}
//						} else if (environment.contains("2")) {
//							boolean test4CleanupStatus = networkCleanup(service, "4", workflow);
//							if (test4CleanupStatus) {
//								exVar_ActStatus = "CLEANED";
//								exVar_ActId = identifier_id;
//								exVar_Environment = environment.toLowerCase().contains("test") ? environment
//										: "TEST" + environment;
//								exData.put("colActStatus", exVar_ActStatus);
//								exData.put("colActId", exVar_ActId);
//								exData.put("colError", "NULL");
//								exData.put("colRequestId", "NULL");
//								exData.put("colEnvironment", exVar_Environment);
//								System.out.println(
//										"\n=======================ACT CLEANUP END============================");
//							} else {
//								exVar_ActStatus = "NOT CLEANED";
//								exVar_ActId = identifier_id;
//								exVar_Environment = environment.toLowerCase().contains("test") ? environment
//										: "TEST" + environment;
//								exData.put("colActStatus", exVar_ActStatus);
//								exData.put("colActId", exVar_ActId);
//								exData.put("colError", "NULL");
//								exData.put("colRequestId", "NULL");
//								exData.put("colEnvironment", exVar_Environment);
//								actCleanupStatus = false;
//								System.out.println(
//										"\n=======================ACT CLEANUP END============================");
//							}
//						} else if (environment.contains("4")) {
//							exVar_ActStatus = "CLEANED";
//							exVar_ActId = identifier_id;
//							exVar_Environment = environment.toLowerCase().contains("test") ? environment
//									: "TEST" + environment;
//							exData.put("colActStatus", exVar_ActStatus);
//							exData.put("colActId", exVar_ActId);
//							exData.put("colError", "NULL");
//							exData.put("colRequestId", "NULL");
//							exData.put("colEnvironment", exVar_Environment);
//							actCleanupStatus = true;
//							System.out.println("\n=======================ACT CLEANUP END============================");
//						}
//					}else {
//						exVar_ActStatus = "ALREADY CLEANED";
//						exVar_Environment = environment.toLowerCase().contains("test") ? environment : "TEST"+environment;
//						exVar_ActId = identifier_id;
//						exData.put("colActStatus", exVar_ActStatus);
//						exData.put("colActId", "Already Delete::" + exVar_ActId);
//						exData.put("colError", "NULL");
//						exData.put("colDeactivateJobId", "NULL");
//						exData.put("colRequestId", "NULL");
//						exData.put("colEnvironment", exVar_Environment);
//						System.out.println("\n=======================ACT CLEANUP END============================");
//						
//					}
					
					
					
					
//					 updating the status in excel
					exVar_ActStatus = "ALREADY CLEANED";
					exVar_Environment = environment.toLowerCase().contains("test") ? environment : "TEST"+environment;
					exVar_ActId = identifier_id;
					exData.put("colActStatus", exVar_ActStatus);
					exData.put("colActId", "Already Delete::" + exVar_ActId);
					exData.put("colError", "NULL");
					exData.put("colDeactivateJobId", "NULL");
					exData.put("colRequestId", "NULL");
					exData.put("colEnvironment", exVar_Environment);
					System.out.println("\n=======================ACT CLEANUP END============================");
//					
					
				}
			} else {
				// get the act details using request id
				ArrayList<String> actInfo = new ArrayList<String>();
				actInfo = getActDetailsUsingRequestID(ReqID_ServiceType_ReqType.get(0).split("&&")[0], environment);
				if (actInfo.size() > 0) {
					String identifier_id = actInfo.get(0);
					String header_identifier = actInfo.get(1);
					// cleanup the network
					System.out.println("\n=======================ACT CLEANUP START============================");
					System.out.println(
							"Act request found for ServiceID::" + service + " in the environment::" + environment);
					System.out.println("Network cleanup is in progress for ServiceID::" + service + "\nidentifier_id::"
							+ identifier_id + "\nheader_identifier::" + header_identifier);

					String token = ap.getToken(username, password);
					// perform network cleanup
					
					
					//-----------------------------------------------------------------//
					
					if (workflow == null || workflow.equalsIgnoreCase("null")||workflow.equalsIgnoreCase("")) {
						System.out.println("Specific Workflow is not provided for ACT Cleanup, so defaulting to \"LNAAS_DELETE_TRANSACTION_ACT_TL_V1\"");
						workflow = "delete";
					}
					
					if (workflow.equalsIgnoreCase("delete")) {
						// perform delete transaction
						
						// System.out.println("Logging into Autopilot\nToken
						// generated::"+token+"\nLogged in Successfully");
						// System.out.println("Logging into Autopilot\nLogged in Successfully");
						String jobid_ = ap.triggerWorkflow(identifier_id, header_identifier,
								"LNAAS_DELETE_TRANSACTION_ACT_TL_V1", token);
						exData.put("colDeactivateJobId", jobid_);
						System.out
								.println("Triggering workflow::\"LNAAS_DELETE_TRANSACTION_ACT_TL_V1\"\nJob id::" + jobid_);
						try {
							System.out.println("Is Workflow completed?::" + ap.getWorkflowStatus(jobid_, token));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						try {
							String errorString = ap.getTaskDetail(jobid_, "e5b9", "$..outgoing.return_value", token);
							if (errorString != null) {
								System.out.println("Delete Transaction from ACT completed Successfully");
								String successDelActId = ap.getTaskDetail(jobid_, "14bc", "$..actIdentifierId", token);
								exVar_ActStatus = "CLEANED";
								exVar_ActId = successDelActId;
								exVar_Environment = environment.toLowerCase().contains("test") ? environment : "TEST"+environment;
								exData.put("colActStatus", exVar_ActStatus);
								exData.put("colActId", exVar_ActId);
								exData.put("colError", "NULL");
								exData.put("colRequestId", "NULL");
								exData.put("colEnvironment",exVar_Environment);
								

								actCleanupStatus = true;
								System.out
										.println("Delete Transaction from ACT completed Successfully::" + successDelActId);
								System.out.println("\n=======================ACT CLEANUP END============================");
							} else {
								errorString = ap.getTaskDetail(jobid_, "51ed", "$..outgoing..message", token);
								String delActId = ap.getTaskDetail(jobid_, "51ed", "$..outgoing..actID", token);
								System.out.println("DELETE Transaction failed with error::");
								System.out.println(
										"+-----------+---------+-----------+---------+-----------+---------+-----------+---------+-----------+---------+");
								System.out.println(errorString);
								System.out.println(
										"+-----------+---------+-----------+---------+-----------+---------+-----------+---------+-----------+---------+");
								System.out.println("Failed to delete Transaction from ACT::" + delActId);

								exVar_Error = errorString;
								exData.put("colError", exVar_Error);
								exVar_ActStatus = "NOT CLEANED";
								exVar_ActId = delActId;
								exVar_Environment = environment.toLowerCase().contains("test") ? environment : "TEST"+environment;
								exData.put("colActStatus", exVar_ActStatus);
								exData.put("colActId", exVar_ActId);
								exData.put("colRequestId", "NULL");
								exData.put("colEnvironment",exVar_Environment);
								actCleanupStatus = false;
								
								System.out.println("\n=======================ACT CLEANUP END============================");
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
					} else if (workflow.equalsIgnoreCase("deactivate")) {
						// perform deactivate transaction
//						System.out.println(
//								"\n===Triggering DEACTIVATE workflow as we got error using DELETE workflow===\n");
						String jobid_ = ap.triggerWorkflow(identifier_id,header_identifier , "Deactivate_Transaction_ACT", token);
						System.out
								.println("Triggering workflow::\"DEACTIVATE_TRANSACTION_ACT\"\nJob id::" + jobid_);
						exVar_DeactivateJobId = jobid_;
						exData.put("colDeactivateJobId", "Deactivate::" + exVar_DeactivateJobId);
						try {
							System.out.println("Is Workflow completed?::" + ap.getWorkflowStatus(jobid_, token));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						try {
							String errorString = ap.getTaskDetail(jobid_, "2375", "$..outgoing.return_value", token);
							if (errorString != null) {
								System.out.println("Deactivation completed Successfully");
								actCleanupStatus = true;
//								String successDelActId = ap.getTaskDetail(jobid_, "662a","$..actIdentifierId", token);
								exVar_Environment = environment.toLowerCase().contains("test") ? environment : "TEST"+environment;
								exData.put("colActStatus", "CLEANED");
								exData.put("colRequestId", "NULL");
								exData.put("colActId", "NULL");
								exData.put("colError", "NULL");
								exData.put("colEnvironment", exVar_Environment);
							} else {
								errorString = ap.getTaskDetail(jobid_, "51ed", "$..outgoing..response", token);
//								delActId = ap.getTaskDetail(jobid_, "51ed","$..outgoing..actID", token);
								System.out.println("Deactivation failed with error::");
								System.out.println("............................................................");
								System.out.println(errorString);
								System.out.println("............................................................");
								exVar_Error = errorString;
								exVar_Environment = environment.toLowerCase().contains("test") ? environment : "TEST"+environment;
								exData.put("colError", exVar_Error);

								exVar_ActStatus = "NOT CLEANED";
								exData.put("colActStatus", exVar_ActStatus);
								exData.put("colActId", "DEACTIVATE::" + "NULL");
								exData.put("colRequestId", "DEACTIVATE::" + "NULL");
								exData.put("colEnvironment", exVar_Environment);

							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						
					}
					else {
						System.out.println("Invalid Workflow provided for ACT Cleanup");
						exVar_Error = "Invalid Workflow provided for ACT Cleanup";
						exData.put("colError", exVar_Error);
						exVar_ActStatus = "NOT CLEANED";
						exData.put("colActStatus", exVar_ActStatus);
						exData.put("colActId", "NULL");
						exData.put("colRequestId", "NULL");
					}

					

				}
			}
		}
		return actCleanupStatus;
	}

	public static ArrayList<String> getActDetailsUsingRequestID(String requestId, String environment) {
		ArrayList<String> actInfo = new ArrayList<String>();
		String actJsonResponse = null;
		String identifier_id = null;
		String header_identifier = null;
		String portName = null;
		String deviceName = null;

		if (environment.contains("1")) {
			actJsonResponse = RestAssured.given().when()
					.get(Test1_fetchDetailsFromReqId+ requestId)
					.body().asString();
		} else if (environment.contains("2")) {
			actJsonResponse = RestAssured.given().when()
					.get(Test2_fetchDetailsFromReqId + requestId)
					.body().asString();
		} else if (environment.contains("4")) {
			actJsonResponse = RestAssured.given().when()
					.get(Test4_fetchDetailsFromReqId + requestId)
					.body().asString();
		}
		JSONObject xmlJSONObj = XML.toJSONObject(actJsonResponse);
		String jsonPrettyPrintString = xmlJSONObj.toString(4);

		ArrayList identifier_id_list = JsonPath.read(jsonPrettyPrintString,
				"$..item[?(@.name=='identifier_id')].value");
		ArrayList header_identifier_list = JsonPath.read(jsonPrettyPrintString,
				"$..item[?(@.name=='header.identifier')].value");
		ArrayList portNameList = JsonPath.read(jsonPrettyPrintString, "$..item[?(@.name=='body.devices.ports[0].port')].value");
		ArrayList deviceNameList = JsonPath.read(jsonPrettyPrintString, "$..item[?(@.name=='body.devices.device')].value");
		if (identifier_id_list.size() > 0 && header_identifier_list.size() > 0) {
			identifier_id = (String) identifier_id_list.get(0);
			header_identifier = String.valueOf(header_identifier_list.get(0));
			actInfo.add(identifier_id);
			actInfo.add(header_identifier);
			if (portNameList.size() > 0) {
//				portName = (String) portNameList.get(0);
//				actInfo.add(portName);
			}
			if (deviceNameList.size() > 0) {
//				deviceName = (String) deviceNameList.get(0);
//				actInfo.add(deviceName);
			}
		}

		return actInfo;
	}

	public static void fetchActDetailsUsingRequestId(LinkedHashMap<Integer, String> getNewRequestIDs) {
		Iterator it = getNewRequestIDs.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
//			System.out.println(pair.getKey() + " = " + pair.getValue());

			Integer requestID = (Integer) pair.getKey();

			String x = given().when().get(Test1_fetchDetailsFromReqId + requestID).body().asString();
			JSONObject xmlJSONObj = XML.toJSONObject(x);
			String jsonPrettyPrintString = xmlJSONObj.toString(4);

			ArrayList productType = JsonPath.read(jsonPrettyPrintString,
					"$..item[?(@.name=='body.productType')].value");

			ArrayList ovcAlias = JsonPath.read(jsonPrettyPrintString, "$..item[?(@.name=='body.productType')].value");
			if (productType.size() > 0) {
				String productypeName = (String) productType.get(0);
				switch (productypeName) {
				case "port":
					System.out.println("Cleaning port");
					ArrayList<String> portAlias = JsonPath.read(jsonPrettyPrintString,
							"$..item[?(@.name=='body.devices.ports[0].serviceId')].value");
					if (portAlias.size() > 0) {
						ServiceCleanupUtility.cleanPort(requestID, portAlias.get(0));
					}

					break;

				case "elanEvpl":
					System.out.println("Cleaning service ELAN EVPL");
					ArrayList<String> evcAlias = JsonPath.read(jsonPrettyPrintString,
							"$..item[?(@.name=='body.vrf[0].serviceId')].value");
					if (evcAlias.size() > 0) {
						ServiceCleanupUtility.cleanelanEvpl(requestID, evcAlias.get(0));
					}
					break;

				case "build_ethernet_path":
					System.out.println("Cleaning service build_ethernet_path");
					ArrayList<String> olineAlias = JsonPath.read(jsonPrettyPrintString,
							"$..item[?(@.name=='body.endpoints[0].networkPath.serviceId')].value");
					if (olineAlias.size() > 0) {
						ServiceCleanupUtility.clean_build_ethernet_path_usingRequestId(requestID, olineAlias.get(0));
					}
					break;

				default:
					break;
				}
			}
//			System.out.println(productType.get(0));

		}
	}

	public static boolean cleanFromACT(Integer requestID) {
		boolean result = false;

		String x = given().when().get(IPcleanup.Test1_fetchDetailsFromReqId + requestID).body().asString();
		JSONObject xmlJSONObj = XML.toJSONObject(x);
		String jsonPrettyPrintString = xmlJSONObj.toString(4);
		ArrayList identifier_id_list = JsonPath.read(jsonPrettyPrintString,
				"$..item[?(@.name=='identifier_id')].value");
		ArrayList header_identifier_list = JsonPath.read(jsonPrettyPrintString,
				"$..item[?(@.name=='header.identifier')].value");
		if (identifier_id_list.size() > 0 && header_identifier_list.size() > 0) {
			String identifier_id = (String) identifier_id_list.get(0);
			String header_identifier = (String) header_identifier_list.get(0);

			System.out.println("identifier_id::" + identifier_id);
			System.out.println("header_identifier::" + header_identifier);

			{
				String token = ap.getToken(username, password);
//				System.out.println("Logging into Autopilot\nToken generated::"+token+"\nLogged in Successfully");
//				System.out.println("Logging into Autopilot\nLogged in Successfully");
				String jobid_ = ap.triggerWorkflow(identifier_id, header_identifier,
						"LNAAS_DELETE_TRANSACTION_ACT_TL_V1", token);
				System.out.println("Triggering workflow::\"LNAAS_DELETE_TRANSACTION_ACT_TL_V1\"\n Job id::" + jobid_);

				try {
					System.out.println("Is Workflow completed?::" + ap.getWorkflowStatus(jobid_, token));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					String errorString = ap.getTaskDetail(jobid_, "e5b9", "$..outgoing.return_value", token);
					if (errorString != null) {
						System.out.println("Delete Transaction from ACT completed Successfully");
						String successDelActId = ap.getTaskDetail(jobid_, "14bc", "$..actIdentifierId", token);
					} else {
						errorString = ap.getTaskDetail(jobid_, "51ed", "$..outgoing..message", token);
						String delActId = ap.getTaskDetail(jobid_, "51ed", "$..outgoing..actID", token);
						System.out.println("DELETE Transaction failed with error::");
						System.out.println("............................................................");
						System.out.println(errorString);
						System.out.println("............................................................");

						// Triggering DEACTIVATE workflow as we got error using DELETE workflow
						if (errorString != null || AUTOPILOT.iterationCount >= 20) {
							System.out.println(
									"\n===Triggering DEACTIVATE workflow as we got error using DELETE workflow===\n");
							jobid_ = ap.triggerWorkflow(identifier_id, header_identifier, "Deactivate_Transaction_ACT",
									token);
							System.out
									.println("Triggering workflow::\"DEACTIVATE_TRANSACTION_ACT\"\n job id::" + jobid_);

							try {
								System.out.println("Is Workflow completed?::" + ap.getWorkflowStatus(jobid_, token));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							try {
								errorString = ap.getTaskDetail(jobid_, "2375", "$..outgoing.return_value", token);
								if (errorString != null) {
									System.out.println("Deactivation completed Successfully");
//									String successDelActId = ap.getTaskDetail(jobid_, "662a","$..actIdentifierId", token);
								} else {
									errorString = ap.getTaskDetail(jobid_, "51ed", "$..outgoing..response", token);
//									delActId = ap.getTaskDetail(jobid_, "51ed","$..outgoing..actID", token);
									System.out.println("Deactivation failed with error::");
									System.out.println("............................................................");
									System.out.println(errorString);
									System.out.println("............................................................");

								}
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}

					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}

	public static ArrayList<String> getParentServices(String serviceID) {

		String resolvedUrl = IPcleanup.Test1_SASI.replaceAll("service_type", "services");
		resolvedUrl = resolvedUrl + serviceID;
		String serviceBody = given().relaxedHTTPSValidation().header(SASI_HEADER_APP_KEY_NAME, SASI_HEADER_APP_KEY_VALUE).get(resolvedUrl).body().asString();
		ArrayList<String> parentServiceName = JsonPath.read(serviceBody, "$..parentServices[*].name");
		if (parentServiceName.size() == 0) {
//			System.out.println("No Parent services found in Test1, checking in Test4");
			resolvedUrl = IPcleanup.Test4_SASI.replaceAll("service_type", "services");
			resolvedUrl = resolvedUrl + serviceID;
			serviceBody = given().relaxedHTTPSValidation().header(SASI_HEADER_APP_KEY_NAME, SASI_HEADER_APP_KEY_VALUE).get(resolvedUrl).body().asString();
			parentServiceName = JsonPath.read(serviceBody, "$..parentServices[*].name");
			if (parentServiceName.size() < 0) {
//				System.out.println("No Parent services found in Test4 also");
				IPcleanup.parentServiceEnvironment = null;
			}
		} else {
			String serviceFromEndpoint[];
			for (String parent : parentServiceName) {
				if (parent.contains("_")) {
					serviceFromEndpoint = parent.split("_");
					if (!parentServiceName.contains(serviceFromEndpoint)) {
						parentServiceName.add(serviceFromEndpoint[0]);
						break;
					}

				}
			}
		}
		if (parentServiceName.size() == 0) {
//			System.out.println("\nNo Parent Associated to the given ServiceID::"+serviceID);
		} else {
			String serviceFromEndpoint[];
			for (String parent : parentServiceName) {
				if (parent.contains("_")) {
					serviceFromEndpoint = parent.split("_");
					if (!parentServiceName.contains(serviceFromEndpoint[0])) {
						parentServiceName.add(serviceFromEndpoint[0]);
						break;
					}
				}
			}
			System.out.println(
					"\n+-----------+---------+-----------+---------+-----------+---------+-----------+---------+-----------+---------+");
			System.out.println("Parent services associated to the given ServiceID::" + serviceID);
//			System.out.println(
//					"========================================================================");
//			for (String parent : parentServiceName) {
//				System.out.println(parent);
//			}
//			Collections.reverse(parentServiceName);
			System.out.println("========================================================================");
			for (String parent : parentServiceName) {
				System.out.println(parent);
			}
			System.out.println(
					"+-----------+---------+-----------+---------+-----------+---------+-----------+---------+-----------+---------+");
		}
		return parentServiceName;

	}

	public static boolean deleteServicefromInventory(String serviceID, String serviceType) {
		System.out.println(
				"==============================================ASRI CLEANUP START=================================================");
		boolean serviceCleanedInAsri = false;
		boolean isInternetService = false;
		String resType = null;
		String resName = null;

		String resolvedUrl = Test1_SASI.replaceAll("service_type", serviceType);
		resolvedUrl = resolvedUrl + serviceID;
//	System.out.println(resolvedUrl);
		String serviceBody = given().relaxedHTTPSValidation().get(resolvedUrl).body().asString();
		ArrayList<String> resourceId = JsonPath.read(serviceBody, "$..resources[*].id");
		ArrayList<String> resourceType = JsonPath.read(serviceBody, "$..resources[0].type");
		//checking environment also
		if (resourceId.size() > 0 && exVar_Environment.contains("1")) {
			// setting environment
			AUTOPILOT.environment = "1";
			System.out.println("Service found in TEST1\nresourceId::" + resourceId.get(0));

			String resTypeFormatted = resourceType.get(0).replaceAll("\\s+", "").toLowerCase();

			if (resTypeFormatted.equalsIgnoreCase("uni")) {
				resName = "unis";
				resType = "Uni";
			} else if (resTypeFormatted.equalsIgnoreCase("oline")) {
				resName = "olines";
				resType = "OLine";
			} else if (resTypeFormatted.equalsIgnoreCase("internetaccess")) {
				resName = "internetaccesses";
				resType = "InternetAccess";
			} else if (resTypeFormatted.equalsIgnoreCase("ovc")) {
				resName = "ovcs";
				resType = "Ovc";
			} else if (resTypeFormatted.equalsIgnoreCase("evc")) {
				resName = "evcs";
				resType = "Evc";
			} else if (resTypeFormatted.equalsIgnoreCase("mp-evc")) {
				resName = "mpevcs";
				resType = "MpEvc";
			} else if (resTypeFormatted.equalsIgnoreCase("ovcendPoint")) {
				resName = "ovcendpoints";
				resType = "OvcEndpoint";
			} else if (resTypeFormatted.equalsIgnoreCase("evcendPoint")) {
				resName = "evcendpoints";
				resType = "EvcEndpoint";
			} else if (resTypeFormatted.equalsIgnoreCase("mp-evcendpoint")) {
				resName = "mpevcendpoints";
				resType = "MpEvcEndpoint";
			} else if (resTypeFormatted.equalsIgnoreCase("ipvpnendpoint")) {
				resName = "ipvpnendpoints";
				resType = "IpVpnEndpoint";
			}

			String resName_resType = resName + "_" + resType;

			String token = ap.getToken(username, password);
//		System.out.println("Logging into Autopilot\nToken generated::"+token+"\nLogged in Successfully");
//		System.out.println("Logging into Autopilot\nLogged in Successfully");
			String jobid_ = ap.triggerWorkflow(resName_resType, serviceID, "Delete_ResourceByFilter_ASRI_AL", token);

			System.out.println("Triggering workflow::\"Delete_ResourceByFilter_ASRI_AL\"\nJob id::" + jobid_);

			String delResBody = "";
			try {
				System.out.println("Is Workflow completed?::" + ap.getWorkflowStatus(jobid_, token));
				delResBody = ap.getTaskDetail(jobid_, "7858", "$..outgoing..return_data", token);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			System.out.println(delResBody);
			if (delResBody != null && delResBody.contains("successfully")) {
				serviceCleanedInAsri = true;
				exVar_AsriStatus = "CLEANED";
				exData.put("colAsri_Status", exVar_AsriStatus);
				if (exVar_Environment.equalsIgnoreCase("NULL")) {
					exVar_Environment = "TEST1";
					exData.put("colEnvironment", exVar_Environment);
				}
				// updation for endpoint
				if (serviceID.contains("_")) {
					exData.put("colService", serviceID);
					exData.put("colRequestId", "NULL");
					exData.put("colActStatus", "NULL");
					exData.put("colActId", "NULL");
					exData.put("colDeactivateJobId", "NULL");
					exData.put("colError", "NULL");
				}

			} else {

				exVar_AsriStatus = "NOT CLEANED";
				exData.put("colAsri_Status", exVar_AsriStatus);
				if (serviceID.contains("_")) {
					exData.put("colService", serviceID);
					exData.put("colRequestId", "NULL");
					exData.put("colActStatus", "NULL");
					exData.put("colActId", "NULL");
					exData.put("colDeactivateJobId", "NULL");
					exData.put("colError", "NULL");
				}
			}
		} else {
//			System.out.println("Service not found in Test1..checking in Test4");//-----------> Commented
			resolvedUrl = Test4_SASI.replaceAll("service_type", serviceType);
			resolvedUrl = resolvedUrl + serviceID;
//		System.out.println(resolvedUrl);
			serviceBody = given().relaxedHTTPSValidation().get(resolvedUrl).body().asString();
			resourceId = JsonPath.read(serviceBody, "$..resources[*].id");
			resourceType = JsonPath.read(serviceBody, "$..resources[0].type");
			if (resourceId.size() > 0 && exVar_Environment.contains("4")) {  //----------------> Added
				// setting environment
				AUTOPILOT.environment = "4";
				System.out.println("Service found in TEST4\nresourceId::" + resourceId.get(0));

				String resTypeFormatted = resourceType.get(0).replaceAll("\\s+", "").toLowerCase();

				if (resTypeFormatted.equalsIgnoreCase("uni")) {
					resName = "unis";
					resType = "Uni";
				} else if (resTypeFormatted.equalsIgnoreCase("oline")) {
					resName = "olines";
					resType = "OLine";
				} else if (resTypeFormatted.equalsIgnoreCase("internetaccess")) {
					resName = "internetaccesses";
					resType = "InternetAccess";
				} else if (resTypeFormatted.equalsIgnoreCase("ovc")) {
					resName = "ovcs";
					resType = "Ovc";
				} else if (resTypeFormatted.equalsIgnoreCase("evc")) {
					resName = "evcs";
					resType = "Evc";
				} else if (resTypeFormatted.equalsIgnoreCase("mp-evc")) {
					resName = "mpevcs";
					resType = "MpEvc";
				} else if (resTypeFormatted.equalsIgnoreCase("ovcendPoint")) {
					resName = "ovcendpoints";
					resType = "OvcEndpoint";
				} else if (resTypeFormatted.equalsIgnoreCase("evcendPoint")) {
					resName = "evcendpoints";
					resType = "EvcEndpoint";
				} else if (resTypeFormatted.equalsIgnoreCase("mp-evcendpoint")) {
					resName = "mpevcendpoints";
					resType = "MpEvcEndpoint";
				} else if (resTypeFormatted.equalsIgnoreCase("ipvpnendpoint")) {
					resName = "ipvpnendpoints";
					resType = "IpVpnEndpoint";
				}

				String resName_resType = resName + "_" + resType;

				String token = ap.getToken(username, password);
//			System.out.println("Logging into Autopilot\nToken generated::"+token+"\nLogged in Successfully");
//			System.out.println("Logging into Autopilot\nLogged in Successfully");
				String jobid_ = ap.triggerWorkflow(resName_resType, serviceID, "Delete_ResourceByFilter_ASRI_AL",
						token);

				System.out.println("Triggering workflow::\"Delete_ResourceByFilter_ASRI_AL\"\nJob id::" + jobid_);

				String delResBody = "";
				try {
					System.out.println("Is Workflow completed?::" + ap.getWorkflowStatus(jobid_, token));
					delResBody = ap.getTaskDetail(jobid_, "7858", "$..outgoing..return_data", token);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				System.out.println(delResBody);
				if (delResBody != null && delResBody.contains("successfully")) {
					serviceCleanedInAsri = true;
					exVar_AsriStatus = "CLEANED";
					exData.put("colAsri_Status", exVar_AsriStatus);
					if (exVar_Environment.equalsIgnoreCase("NULL")) {
						exVar_Environment = "TEST4";
						exData.put("colEnvironment", exVar_Environment);
					}
					// updation for endpoint
					if (serviceID.contains("_")) {
						exData.put("colService", serviceID);
						exData.put("colRequestId", "NULL");
						exData.put("colActStatus", "NULL");
						exData.put("colActId", "NULL");
						exData.put("colDeactivateJobId", "NULL");
						exData.put("colError", "NULL");
					}

				} else {

					exVar_AsriStatus = "NOT CLEANED";
					exData.put("colAsri_Status", exVar_AsriStatus);
					if (serviceID.contains("_")) {
						exData.put("colService", serviceID);
						exData.put("colRequestId", "NULL");
						exData.put("colActStatus", "NULL");
						exData.put("colActId", "NULL");
						exData.put("colDeactivateJobId", "NULL");
						exData.put("colError", "NULL");
					}
				}
			} else {
				
				
				{
//					System.out.println("Service not found in Test4..checking in Test2");//-----------> Commented
					resolvedUrl = Test2_SASI.replaceAll("service_type", serviceType);
					resolvedUrl = resolvedUrl + serviceID;
//				System.out.println(resolvedUrl);
					serviceBody = given().relaxedHTTPSValidation().header(SASI_HEADER_APP_KEY_NAME, SASI_HEADER_APP_KEY_VALUE).get(resolvedUrl).body().asString();
					resourceId = JsonPath.read(serviceBody, "$..resources[*].id");
					resourceType = JsonPath.read(serviceBody, "$..resources[0].type");
					if (resourceId.size() > 0 && exVar_Environment.contains("2")) { //----------------> Added
						// setting environment
						AUTOPILOT.environment = "2";
						System.out.println("Service found in TEST2\nresourceId::" + resourceId.get(0));

						String resTypeFormatted = resourceType.get(0).replaceAll("\\s+", "").toLowerCase();

						if (resTypeFormatted.equalsIgnoreCase("uni")) {
							resName = "unis";
							resType = "Uni";
						} else if (resTypeFormatted.equalsIgnoreCase("oline")) {
							resName = "olines";
							resType = "OLine";
						} else if (resTypeFormatted.equalsIgnoreCase("internetaccess")) {
							resName = "internetaccesses";
							resType = "InternetAccess";
						} else if (resTypeFormatted.equalsIgnoreCase("ovc")) {
							resName = "ovcs";
							resType = "Ovc";
						} else if (resTypeFormatted.equalsIgnoreCase("evc")) {
							resName = "evcs";
							resType = "Evc";
						} else if (resTypeFormatted.equalsIgnoreCase("mp-evc")) {
							resName = "mpevcs";
							resType = "MpEvc";
						} else if (resTypeFormatted.equalsIgnoreCase("ovcendPoint")) {
							resName = "ovcendpoints";
							resType = "OvcEndpoint";
						} else if (resTypeFormatted.equalsIgnoreCase("evcendPoint")) {
							resName = "evcendpoints";
							resType = "EvcEndpoint";
						} else if (resTypeFormatted.equalsIgnoreCase("mp-evcendpoint")) {
							resName = "mpevcendpoints";
							resType = "MpEvcEndpoint";
						} else if (resTypeFormatted.equalsIgnoreCase("ipvpnendpoint")) {
							resName = "ipvpnendpoints";
							resType = "IpVpnEndpoint";
						}

						String resName_resType = resName + "_" + resType;

						String token = ap.getToken(username, password);
//					System.out.println("Logging into Autopilot\nToken generated::"+token+"\nLogged in Successfully");
//					System.out.println("Logging into Autopilot\nLogged in Successfully");
						String jobid_ = ap.triggerWorkflow(resName_resType, serviceID, "Delete_ResourceByFilter_ASRI_AL",
								token);

						System.out.println("Triggering workflow::\"Delete_ResourceByFilter_ASRI_AL\"\nJob id::" + jobid_);

						String delResBody = "";
						try {
							System.out.println("Is Workflow completed?::" + ap.getWorkflowStatus(jobid_, token));
							delResBody = ap.getTaskDetail(jobid_, "7858", "$..outgoing..return_data", token);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						System.out.println(delResBody);
						if (delResBody != null && delResBody.contains("successfully")) {
							serviceCleanedInAsri = true;
							exVar_AsriStatus = "CLEANED";
							exData.put("colAsri_Status", exVar_AsriStatus);
							if (exVar_Environment.equalsIgnoreCase("NULL")) {
								exVar_Environment = "TEST2";
								exData.put("colEnvironment", exVar_Environment);
							}
							// updation for endpoint
							if (serviceID.contains("_")) {
								exData.put("colService", serviceID);
								exData.put("colRequestId", "NULL");
								exData.put("colActStatus", "NULL");
								exData.put("colActId", "NULL");
								exData.put("colDeactivateJobId", "NULL");
								exData.put("colError", "NULL");
							}

						} else {

							exVar_AsriStatus = "NOT CLEANED";
							exData.put("colAsri_Status", exVar_AsriStatus);
							if (serviceID.contains("_")) {
								exData.put("colService", serviceID);
								exData.put("colRequestId", "NULL");
								exData.put("colActStatus", "NULL");
								exData.put("colActId", "NULL");
								exData.put("colDeactivateJobId", "NULL");
								exData.put("colError", "NULL");
							}
						}
					} else {
//						System.out.println("Service not found in Test2 also"); //----------------> Commented
						System.out.println("No Service found in "+exVar_Environment);
						exVar_AsriStatus = "NOT FOUND/CLEANED";
						exData.put("colAsri_Status", exVar_AsriStatus);
						if (serviceID.contains("_")) {
							exData.put("colService", serviceID);
							exData.put("colRequestId", "NULL");
							exData.put("colActStatus", "NULL");
							exData.put("colActId", "NULL");
							exData.put("colDeactivateJobId", "NULL");
							exData.put("colError", "NULL");
							exData.put("colIP_Status", "NULL");
						}

					}
				}
				
				
//				exVar_AsriStatus = "NOT FOUND/CLEANED";
//				exData.put("colAsri_Status", exVar_AsriStatus);
//				if (serviceID.contains("_")) {
//					exData.put("colService", serviceID);
//					exData.put("colRequestId", "NULL");
//					exData.put("colActStatus", "NULL");
//					exData.put("colActId", "NULL");
//					exData.put("colDeactivateJobId", "NULL");
//					exData.put("colError", "NULL");
//					exData.put("colIP_Status", "NULL");
//				}

			}
			
			//-----------------------------------------------------------------------------------------------------------------------//
			
		}
		System.out.println(
				"==============================================ASRI CLEANUP END===================================================\n\n");
		return isInternetService;
	}

	public static boolean deleteServicefromInventoryOnly(String serviceID, String serviceType) {
		System.out.println(
				"==============================================ASRI CLEANUP START=================================================");
		boolean serviceCleanedInAsri = false;
		boolean isInternetService = false;
		String resType = null;
		String resName = null;

		String resolvedUrl = Test1_SASI.replaceAll("service_type", serviceType);
		resolvedUrl = resolvedUrl + serviceID;
//	System.out.println(resolvedUrl);
		String serviceBody = given().relaxedHTTPSValidation().header(SASI_HEADER_APP_KEY_NAME, SASI_HEADER_APP_KEY_VALUE).get(resolvedUrl).body().asString();
		ArrayList<String> resourceId = JsonPath.read(serviceBody, "$..resources[*].id");
		ArrayList<String> resourceType = JsonPath.read(serviceBody, "$..resources[0].type");
		//checking environment also
		if (resourceId.size() > 0 ) {
			// setting environment
			AUTOPILOT.environment = "1";
			System.out.println("Service found in TEST1\nresourceId::" + resourceId.get(0));

			String resTypeFormatted = resourceType.get(0).replaceAll("\\s+", "").toLowerCase();

			if (resTypeFormatted.equalsIgnoreCase("uni")) {
				resName = "unis";
				resType = "Uni";
			} else if (resTypeFormatted.equalsIgnoreCase("oline")) {
				resName = "olines";
				resType = "OLine";
			} else if (resTypeFormatted.equalsIgnoreCase("internetaccess")) {
				resName = "internetaccesses";
				resType = "InternetAccess";
			} else if (resTypeFormatted.equalsIgnoreCase("ovc")) {
				resName = "ovcs";
				resType = "Ovc";
			} else if (resTypeFormatted.equalsIgnoreCase("evc")) {
				resName = "evcs";
				resType = "Evc";
			} else if (resTypeFormatted.equalsIgnoreCase("mp-evc")) {
				resName = "mpevcs";
				resType = "MpEvc";
			} else if (resTypeFormatted.equalsIgnoreCase("ovcendPoint")) {
				resName = "ovcendpoints";
				resType = "OvcEndpoint";
			} else if (resTypeFormatted.equalsIgnoreCase("evcendPoint")) {
				resName = "evcendpoints";
				resType = "EvcEndpoint";
			} else if (resTypeFormatted.equalsIgnoreCase("mp-evcendpoint")) {
				resName = "mpevcendpoints";
				resType = "MpEvcEndpoint";
			} else if (resTypeFormatted.equalsIgnoreCase("ipvpnendpoint")) {
				resName = "ipvpnendpoints";
				resType = "IpVpnEndpoint";
			}

			String resName_resType = resName + "_" + resType;

			String token = ap.getToken(username, password);
//		System.out.println("Logging into Autopilot\nToken generated::"+token+"\nLogged in Successfully");
//		System.out.println("Logging into Autopilot\nLogged in Successfully");
			String jobid_ = ap.triggerWorkflow(resName_resType, serviceID, "Delete_ResourceByFilter_ASRI_AL", token);

			System.out.println("Triggering workflow::\"Delete_ResourceByFilter_ASRI_AL\"\nJob id::" + jobid_);

			String delResBody = "";
			try {
				System.out.println("Is Workflow completed?::" + ap.getWorkflowStatus(jobid_, token));
				delResBody = ap.getTaskDetail(jobid_, "7858", "$..outgoing..return_data", token);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			System.out.println(delResBody);
			if (delResBody != null && delResBody.contains("successfully")) {
				serviceCleanedInAsri = true;
				exVar_AsriStatus = "CLEANED";
				exData.put("colAsri_Status", exVar_AsriStatus);
				if (exVar_Environment.equalsIgnoreCase("NULL")) {
					exVar_Environment = "TEST1";
					exData.put("colEnvironment", exVar_Environment);
				}
				// updation for endpoint
				if (serviceID.contains("_")) {
					exData.put("colService", serviceID);
					exData.put("colRequestId", "NULL");
					exData.put("colActStatus", "NULL");
					exData.put("colActId", "NULL");
					exData.put("colDeactivateJobId", "NULL");
					exData.put("colError", "NULL");
				}

			} else {

				exVar_AsriStatus = "NOT CLEANED";
				exData.put("colAsri_Status", exVar_AsriStatus);
				if (serviceID.contains("_")) {
					exData.put("colService", serviceID);
					exData.put("colRequestId", "NULL");
					exData.put("colActStatus", "NULL");
					exData.put("colActId", "NULL");
					exData.put("colDeactivateJobId", "NULL");
					exData.put("colError", "NULL");
				}
			}
		} else {
//			System.out.println("Service not found in Test1..checking in Test4");//-----------> Commented
			resolvedUrl = Test4_SASI.replaceAll("service_type", serviceType);
			resolvedUrl = resolvedUrl + serviceID;
//		System.out.println(resolvedUrl);
			serviceBody = given().relaxedHTTPSValidation().header(SASI_HEADER_APP_KEY_NAME, SASI_HEADER_APP_KEY_VALUE).get(resolvedUrl).body().asString();
			resourceId = JsonPath.read(serviceBody, "$..resources[*].id");
			resourceType = JsonPath.read(serviceBody, "$..resources[0].type");
			if (resourceId.size() > 0 ) {  //----------------> Added
				// setting environment
				AUTOPILOT.environment = "4";
				System.out.println("Service found in TEST4\nresourceId::" + resourceId.get(0));

				String resTypeFormatted = resourceType.get(0).replaceAll("\\s+", "").toLowerCase();

				if (resTypeFormatted.equalsIgnoreCase("uni")) {
					resName = "unis";
					resType = "Uni";
				} else if (resTypeFormatted.equalsIgnoreCase("oline")) {
					resName = "olines";
					resType = "OLine";
				} else if (resTypeFormatted.equalsIgnoreCase("internetaccess")) {
					resName = "internetaccesses";
					resType = "InternetAccess";
				} else if (resTypeFormatted.equalsIgnoreCase("ovc")) {
					resName = "ovcs";
					resType = "Ovc";
				} else if (resTypeFormatted.equalsIgnoreCase("evc")) {
					resName = "evcs";
					resType = "Evc";
				} else if (resTypeFormatted.equalsIgnoreCase("mp-evc")) {
					resName = "mpevcs";
					resType = "MpEvc";
				} else if (resTypeFormatted.equalsIgnoreCase("ovcendPoint")) {
					resName = "ovcendpoints";
					resType = "OvcEndpoint";
				} else if (resTypeFormatted.equalsIgnoreCase("evcendPoint")) {
					resName = "evcendpoints";
					resType = "EvcEndpoint";
				} else if (resTypeFormatted.equalsIgnoreCase("mp-evcendpoint")) {
					resName = "mpevcendpoints";
					resType = "MpEvcEndpoint";
				} else if (resTypeFormatted.equalsIgnoreCase("ipvpnendpoint")) {
					resName = "ipvpnendpoints";
					resType = "IpVpnEndpoint";
				}

				String resName_resType = resName + "_" + resType;

				String token = ap.getToken(username, password);
//			System.out.println("Logging into Autopilot\nToken generated::"+token+"\nLogged in Successfully");
//			System.out.println("Logging into Autopilot\nLogged in Successfully");
				String jobid_ = ap.triggerWorkflow(resName_resType, serviceID, "Delete_ResourceByFilter_ASRI_AL",
						token);

				System.out.println("Triggering workflow::\"Delete_ResourceByFilter_ASRI_AL\"\nJob id::" + jobid_);

				String delResBody = "";
				try {
					System.out.println("Is Workflow completed?::" + ap.getWorkflowStatus(jobid_, token));
					delResBody = ap.getTaskDetail(jobid_, "7858", "$..outgoing..return_data", token);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				System.out.println(delResBody);
				if (delResBody != null && delResBody.contains("successfully")) {
					serviceCleanedInAsri = true;
					exVar_AsriStatus = "CLEANED";
					exData.put("colAsri_Status", exVar_AsriStatus);
					if (exVar_Environment.equalsIgnoreCase("NULL")) {
						exVar_Environment = "TEST4";
						exData.put("colEnvironment", exVar_Environment);
					}
					// updation for endpoint
					if (serviceID.contains("_")) {
						exData.put("colService", serviceID);
						exData.put("colRequestId", "NULL");
						exData.put("colActStatus", "NULL");
						exData.put("colActId", "NULL");
						exData.put("colDeactivateJobId", "NULL");
						exData.put("colError", "NULL");
					}

				} else {

					exVar_AsriStatus = "NOT CLEANED";
					exData.put("colAsri_Status", exVar_AsriStatus);
					if (serviceID.contains("_")) {
						exData.put("colService", serviceID);
						exData.put("colRequestId", "NULL");
						exData.put("colActStatus", "NULL");
						exData.put("colActId", "NULL");
						exData.put("colDeactivateJobId", "NULL");
						exData.put("colError", "NULL");
					}
				}
			} else {
				
				
				{
//					System.out.println("Service not found in Test4..checking in Test2");//-----------> Commented
					resolvedUrl = Test2_SASI.replaceAll("service_type", serviceType);
					resolvedUrl = resolvedUrl + serviceID;
//				System.out.println(resolvedUrl);
					serviceBody = given().relaxedHTTPSValidation().get(resolvedUrl).body().asString();
					resourceId = JsonPath.read(serviceBody, "$..resources[*].id");
					resourceType = JsonPath.read(serviceBody, "$..resources[0].type");
					if (resourceId.size() > 0 ) { //----------------> Added
						// setting environment
						AUTOPILOT.environment = "2";
						System.out.println("Service found in TEST2\nresourceId::" + resourceId.get(0));

						String resTypeFormatted = resourceType.get(0).replaceAll("\\s+", "").toLowerCase();

						if (resTypeFormatted.equalsIgnoreCase("uni")) {
							resName = "unis";
							resType = "Uni";
						} else if (resTypeFormatted.equalsIgnoreCase("oline")) {
							resName = "olines";
							resType = "OLine";
						} else if (resTypeFormatted.equalsIgnoreCase("internetaccess")) {
							resName = "internetaccesses";
							resType = "InternetAccess";
						} else if (resTypeFormatted.equalsIgnoreCase("ovc")) {
							resName = "ovcs";
							resType = "Ovc";
						} else if (resTypeFormatted.equalsIgnoreCase("evc")) {
							resName = "evcs";
							resType = "Evc";
						} else if (resTypeFormatted.equalsIgnoreCase("mp-evc")) {
							resName = "mpevcs";
							resType = "MpEvc";
						} else if (resTypeFormatted.equalsIgnoreCase("ovcendPoint")) {
							resName = "ovcendpoints";
							resType = "OvcEndpoint";
						} else if (resTypeFormatted.equalsIgnoreCase("evcendPoint")) {
							resName = "evcendpoints";
							resType = "EvcEndpoint";
						} else if (resTypeFormatted.equalsIgnoreCase("mp-evcendpoint")) {
							resName = "mpevcendpoints";
							resType = "MpEvcEndpoint";
						} else if (resTypeFormatted.equalsIgnoreCase("ipvpnendpoint")) {
							resName = "ipvpnendpoints";
							resType = "IpVpnEndpoint";
						}

						String resName_resType = resName + "_" + resType;

						String token = ap.getToken(username, password);
//					System.out.println("Logging into Autopilot\nToken generated::"+token+"\nLogged in Successfully");
//					System.out.println("Logging into Autopilot\nLogged in Successfully");
						String jobid_ = ap.triggerWorkflow(resName_resType, serviceID, "Delete_ResourceByFilter_ASRI_AL",
								token);

						System.out.println("Triggering workflow::\"Delete_ResourceByFilter_ASRI_AL\"\nJob id::" + jobid_);

						String delResBody = "";
						try {
							System.out.println("Is Workflow completed?::" + ap.getWorkflowStatus(jobid_, token));
							delResBody = ap.getTaskDetail(jobid_, "7858", "$..outgoing..return_data", token);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						System.out.println(delResBody);
						if (delResBody != null && delResBody.contains("successfully")) {
							serviceCleanedInAsri = true;
							exVar_AsriStatus = "CLEANED";
							exData.put("colAsri_Status", exVar_AsriStatus);
							if (exVar_Environment.equalsIgnoreCase("NULL")) {
								exVar_Environment = "TEST2";
								exData.put("colEnvironment", exVar_Environment);
							}
							// updation for endpoint
							if (serviceID.contains("_")) {
								exData.put("colService", serviceID);
								exData.put("colRequestId", "NULL");
								exData.put("colActStatus", "NULL");
								exData.put("colActId", "NULL");
								exData.put("colDeactivateJobId", "NULL");
								exData.put("colError", "NULL");
							}

						} else {

							exVar_AsriStatus = "NOT CLEANED";
							exData.put("colAsri_Status", exVar_AsriStatus);
							if (serviceID.contains("_")) {
								exData.put("colService", serviceID);
								exData.put("colRequestId", "NULL");
								exData.put("colActStatus", "NULL");
								exData.put("colActId", "NULL");
								exData.put("colDeactivateJobId", "NULL");
								exData.put("colError", "NULL");
							}
						}
					} else {
//						System.out.println("Service not found in Test2 also"); //----------------> Commented
						System.out.println("No Service found in Inventory ");
						exVar_AsriStatus = "NOT FOUND/CLEANED";
						exData.put("colAsri_Status", exVar_AsriStatus);
						if (serviceID.contains("_")) {
							exData.put("colService", serviceID);
							exData.put("colRequestId", "NULL");
							exData.put("colActStatus", "NULL");
							exData.put("colActId", "NULL");
							exData.put("colDeactivateJobId", "NULL");
							exData.put("colError", "NULL");
							exData.put("colIP_Status", "NULL");
						}

					}
				}
				
				
//				exVar_AsriStatus = "NOT FOUND/CLEANED";
//				exData.put("colAsri_Status", exVar_AsriStatus);
//				if (serviceID.contains("_")) {
//					exData.put("colService", serviceID);
//					exData.put("colRequestId", "NULL");
//					exData.put("colActStatus", "NULL");
//					exData.put("colActId", "NULL");
//					exData.put("colDeactivateJobId", "NULL");
//					exData.put("colError", "NULL");
//					exData.put("colIP_Status", "NULL");
//				}

			}
			
			//-----------------------------------------------------------------------------------------------------------------------//
			
		}
		System.out.println(
				"==============================================ASRI CLEANUP END===================================================\n\n");
		return isInternetService;
	}
	
	public static boolean deleteServicefromArmInventory(String serviceID, String serviceType) {
		System.out.println(
				"==============================================ASRI CLEANUP START=================================================");
		boolean serviceCleanedInAsri = false;
		boolean isInternetService = false;
		String resType = null;
		String resName = null;

		String resolvedUrl = Test1_SASI.replaceAll("service_type", serviceType);
		resolvedUrl = resolvedUrl + serviceID;
//	System.out.println(resolvedUrl);
		String serviceBody = given().relaxedHTTPSValidation().header(SASI_HEADER_APP_KEY_NAME, SASI_HEADER_APP_KEY_VALUE).get(resolvedUrl).body().asString();
		ArrayList<String> resourceId = JsonPath.read(serviceBody, "$..resources[*].id");
		ArrayList<String> resourceType = JsonPath.read(serviceBody, "$..resources[0].type");
		if (resourceId.size() > 0) {
			// setting environment
			AUTOPILOT.environment = "1";
			System.out.println("Service found in TEST1\nresourceId::" + resourceId.get(0));

			String resTypeFormatted = resourceType.get(0).replaceAll("\\s+", "").toLowerCase();

			if (resTypeFormatted.equalsIgnoreCase("uni")) {
				resName = "unis";
				resType = "Uni";
			} else if (resTypeFormatted.equalsIgnoreCase("oline")) {
				resName = "olines";
				resType = "OLine";
			} else if (resTypeFormatted.equalsIgnoreCase("internetaccess")) {
				resName = "internetaccesses";
				resType = "InternetAccess";
			} else if (resTypeFormatted.equalsIgnoreCase("ovc")) {
				resName = "ovcs";
				resType = "Ovc";
			} else if (resTypeFormatted.equalsIgnoreCase("evc")) {
				resName = "evcs";
				resType = "Evc";
			} else if (resTypeFormatted.equalsIgnoreCase("mp-evc")) {
				resName = "mpevcs";
				resType = "MpEvc";
			} else if (resTypeFormatted.equalsIgnoreCase("ovcendPoint")) {
				resName = "ovcendpoints";
				resType = "OvcEndpoint";
			} else if (resTypeFormatted.equalsIgnoreCase("evcendPoint")) {
				resName = "evcendpoints";
				resType = "EvcEndpoint";
			} else if (resTypeFormatted.equalsIgnoreCase("mp-evcendpoint")) {
				resName = "mpevcendpoints";
				resType = "MpEvcEndpoint";
			} else if (resTypeFormatted.equalsIgnoreCase("ipvpnendpoint")) {
				resName = "ipvpnendpoints";
				resType = "IpVpnEndpoint";
			}

			String resName_resType = resName + "_" + resType;

			String token = ap.getToken(username, password);
//		System.out.println("Logging into Autopilot\nToken generated::"+token+"\nLogged in Successfully");
//		System.out.println("Logging into Autopilot\nLogged in Successfully");
			String jobid_ = ap.triggerWorkflow(resName_resType, serviceID, "Delete_ResourceByFilter_ASRI_AL", token);

			System.out.println("Triggering workflow::\"Delete_ResourceByFilter_ASRI_AL\"\nJob id::" + jobid_);

			String delResBody = "";
			try {
				System.out.println("Is Workflow completed?::" + ap.getWorkflowStatus(jobid_, token));
				delResBody = ap.getTaskDetail(jobid_, "7858", "$..outgoing..return_data", token);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			System.out.println(delResBody);
			if (delResBody != null && delResBody.contains("successfully")) {
				serviceCleanedInAsri = true;
				exVar_AsriStatus = "CLEANED";
				exData.put("colAsri_Status", exVar_AsriStatus);
				if (exVar_Environment.equalsIgnoreCase("NULL")) {
					exVar_Environment = "TEST1";
					exData.put("colEnvironment", exVar_Environment);
				}
				// updation for endpoint
				if (serviceID.contains("_")) {
					exData.put("colService", serviceID);
					exData.put("colRequestId", "NULL");
					exData.put("colActStatus", "NULL");
					exData.put("colActId", "NULL");
					exData.put("colDeactivateJobId", "NULL");
					exData.put("colError", "NULL");
				}

			} else {

				exVar_AsriStatus = "NOT CLEANED";
				exData.put("colAsri_Status", exVar_AsriStatus);
				if (serviceID.contains("_")) {
					exData.put("colService", serviceID);
					exData.put("colRequestId", "NULL");
					exData.put("colActStatus", "NULL");
					exData.put("colActId", "NULL");
					exData.put("colDeactivateJobId", "NULL");
					exData.put("colError", "NULL");
				}
			}
		} else {
			System.out.println("Service not found in Test1..checking in Test4");
			resolvedUrl = Test4_SASI.replaceAll("service_type", serviceType);
			resolvedUrl = resolvedUrl + serviceID;
//		System.out.println(resolvedUrl);
			serviceBody = given().relaxedHTTPSValidation().header(SASI_HEADER_APP_KEY_NAME, SASI_HEADER_APP_KEY_VALUE).get(resolvedUrl).body().asString();
			resourceId = JsonPath.read(serviceBody, "$..resources[*].id");
			resourceType = JsonPath.read(serviceBody, "$..resources[0].type");
			if (resourceId.size() > 0) {
				// setting environment
				AUTOPILOT.environment = "4";
				System.out.println("Service found in TEST4\nresourceId::" + resourceId.get(0));

				String resTypeFormatted = resourceType.get(0).replaceAll("\\s+", "").toLowerCase();

				if (resTypeFormatted.equalsIgnoreCase("uni")) {
					resName = "unis";
					resType = "Uni";
				} else if (resTypeFormatted.equalsIgnoreCase("oline")) {
					resName = "olines";
					resType = "OLine";
				} else if (resTypeFormatted.equalsIgnoreCase("internetaccess")) {
					resName = "internetaccesses";
					resType = "InternetAccess";
				} else if (resTypeFormatted.equalsIgnoreCase("ovc")) {
					resName = "ovcs";
					resType = "Ovc";
				} else if (resTypeFormatted.equalsIgnoreCase("evc")) {
					resName = "evcs";
					resType = "Evc";
				} else if (resTypeFormatted.equalsIgnoreCase("mp-evc")) {
					resName = "mpevcs";
					resType = "MpEvc";
				} else if (resTypeFormatted.equalsIgnoreCase("ovcendPoint")) {
					resName = "ovcendpoints";
					resType = "OvcEndpoint";
				} else if (resTypeFormatted.equalsIgnoreCase("evcendPoint")) {
					resName = "evcendpoints";
					resType = "EvcEndpoint";
				} else if (resTypeFormatted.equalsIgnoreCase("mp-evcendpoint")) {
					resName = "mpevcendpoints";
					resType = "MpEvcEndpoint";
				} else if (resTypeFormatted.equalsIgnoreCase("ipvpnendpoint")) {
					resName = "ipvpnendpoints";
					resType = "IpVpnEndpoint";
				}

				String resName_resType = resName + "_" + resType;

				String token = ap.getToken(username, password);
//			System.out.println("Logging into Autopilot\nToken generated::"+token+"\nLogged in Successfully");
//			System.out.println("Logging into Autopilot\nLogged in Successfully");
				String jobid_ = ap.triggerWorkflow(resName_resType, serviceID, "Delete_ResourceByFilter_ASRI_AL",
						token);

				System.out.println("Triggering workflow::\"Delete_ResourceByFilter_ASRI_AL\"\nJob id::" + jobid_);

				String delResBody = "";
				try {
					System.out.println("Is Workflow completed?::" + ap.getWorkflowStatus(jobid_, token));
					delResBody = ap.getTaskDetail(jobid_, "7858", "$..outgoing..return_data", token);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				System.out.println(delResBody);
				if (delResBody != null && delResBody.contains("successfully")) {
					serviceCleanedInAsri = true;
					exVar_AsriStatus = "CLEANED";
					exData.put("colAsri_Status", exVar_AsriStatus);
					if (exVar_Environment.equalsIgnoreCase("NULL")) {
						exVar_Environment = "TEST4";
						exData.put("colEnvironment", exVar_Environment);
					}
					// updation for endpoint
					if (serviceID.contains("_")) {
						exData.put("colService", serviceID);
						exData.put("colRequestId", "NULL");
						exData.put("colActStatus", "NULL");
						exData.put("colActId", "NULL");
						exData.put("colDeactivateJobId", "NULL");
						exData.put("colError", "NULL");
					}

				} else {

					exVar_AsriStatus = "NOT CLEANED";
					exData.put("colAsri_Status", exVar_AsriStatus);
					if (serviceID.contains("_")) {
						exData.put("colService", serviceID);
						exData.put("colRequestId", "NULL");
						exData.put("colActStatus", "NULL");
						exData.put("colActId", "NULL");
						exData.put("colDeactivateJobId", "NULL");
						exData.put("colError", "NULL");
					}
				}
			} else {
				System.out.println("Service not found in Test4 also");
				exVar_AsriStatus = "NOT FOUND/CLEANED";
				exData.put("colAsri_Status", exVar_AsriStatus);
				if (serviceID.contains("_")) {
					exData.put("colService", serviceID);
					exData.put("colRequestId", "NULL");
					exData.put("colActStatus", "NULL");
					exData.put("colActId", "NULL");
					exData.put("colDeactivateJobId", "NULL");
					exData.put("colError", "NULL");
					exData.put("colIP_Status", "NULL");
				}

			}
		}
		System.out.println(
				"==============================================ASRI CLEANUP END===================================================\n\n");
		return isInternetService;
	}

	public static void printMap() throws IOException {
		Iterator it = exData.entrySet().iterator();
		System.out.println(
				"+-------------+-------------+-------------+-------------+-------------+-------------+-------------+-------------+");
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			System.out.println(pair.getKey() + " = " + pair.getValue());
		}
		System.out.println(
				"+-------------+-------------+-------------+-------------+-------------+--------------------------+-------------+\n\n");
//		    eu.writeExcel(exData,"C:\\STAF_Files\\CleanedIpSheet_"+dateTime+".xlsx");
		eu.writeExcel(exData, fileDir + "\\IP_DATA\\CLEANED_IP_FILES\\CleanedIpSheet_" + dateTime + ".xlsx");

		// resetting variable
		exVar_ServiceAlias = "NULL";
		exVar_IpStatus = "NULL";
		exVar_AsriStatus = "NULL";
		exVar_ActStatus = "NULL";
		exVar_RequestId = 0;
		exVar_IdentifierId = "NULL";
		exVar_ActId = "NULL";
		exVar_Error = "NULL";
		exVar_DeactivateJobId = "NULL";
		exVar_Environment = "NULL";
		exVar_AsriStatus = "CLEANED";
		exData.put("colIP_Status", "NULL");
	}
	
	public static boolean networkCleanup1(String service, String environment) {
		
		// Logic to clean up the network
		// If network is cleaned up successfully, return true
		// Else return false
		boolean actCleanupStatus = false;
		AUTOPILOT autopilot = new AUTOPILOT();
		//set the environment
		autopilot.environment = environment;
		
		
		//get the request ids
		ArrayList<String> ReqID_ServiceType_ReqType = new ArrayList<String>();
		ReqID_ServiceType_ReqType = getRequestIDs(service, environment);
		if (ReqID_ServiceType_ReqType.size() == 0) {
			System.out.println("No request found for the given service::" + service +" in the "+environment+" environment");
			actCleanupStatus = true;
			return actCleanupStatus;
		}else if (ReqID_ServiceType_ReqType.size() > 0) {
			if(ReqID_ServiceType_ReqType.get(0).contains("delete")) {
				ArrayList<String> actInfo = new ArrayList<String>();
				actInfo = getActDetailsUsingRequestID(ReqID_ServiceType_ReqType.get(0).split("&&")[0], environment);
				if (actInfo.size() > 0) {
					String identifier_id = actInfo.get(0);
					String header_identifier = actInfo.get(1);
					// cleanup the network
					System.out.println("+-----------------ACT CLEANUP START---------------------------------------------------------------+");
					System.out.println("Act request found for ServiceID::"+service+ "in the environment::"+ environment);
					System.out.println("Network cleanup already done for ServiceID::"+service+ "\nidentifier_id::" + identifier_id + "\nheader_identifier::" + header_identifier);
					actCleanupStatus = true;
					System.out.println("+-----------------ACT CLEANUP END-----------------------------------------------------------------+");
				}
			} else {
				// get the act details using request id
				ArrayList<String> actInfo = new ArrayList<String>();
				actInfo = getActDetailsUsingRequestID(ReqID_ServiceType_ReqType.get(0).split("&&")[0], environment);
				if (actInfo.size() > 0) {
					String identifier_id = actInfo.get(0);
					String header_identifier = actInfo.get(1);
					// cleanup the network
					System.out.println("+-----------------ACT CLEANUP START---------------------------------------------------------------+");
					System.out.println("Act request found for ServiceID::"+service+ "in the environment::"+ environment);
					System.out.println("Network cleanup is in progress for ServiceID::"+service+ "\nidentifier_id::" + identifier_id + "\nheader_identifier::" + header_identifier);
					
					//perform network cleanup
					
					String token = autopilot.getToken(username, password);
//					System.out.println("Logging into Autopilot\nToken generated::"+token+"\nLogged in Successfully");
//					System.out.println("Logging into Autopilot\nLogged in Successfully");
					String jobid_ = autopilot.triggerWorkflow(identifier_id, header_identifier, "LNAAS_DELETE_TRANSACTION_ACT_TL_V1",
							token);
					System.out.println("Triggering workflow::\"LNAAS_DELETE_TRANSACTION_ACT_TL_V1\"\nJob id::" + jobid_);
					try {
						System.out.println("Is Workflow completed?::" + autopilot.getWorkflowStatus(jobid_, token));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					try {
						String errorString = autopilot.getTaskDetail(jobid_, "e5b9", "$..outgoing.return_value", token);
						if (errorString != null) {
							System.out.println("Delete Transaction from ACT completed Successfully");
							String successDelActId = autopilot.getTaskDetail(jobid_, "14bc", "$..actIdentifierId", token);
							actCleanupStatus = true;
							System.out.println("Delete Transaction from ACT completed Successfully::" + successDelActId);
							System.out.println("+-----------------ACT CLEANUP END-----------------------------------------------------------------+");
						} else {
							errorString = autopilot.getTaskDetail(jobid_, "51ed", "$..outgoing..message", token);
							String delActId = autopilot.getTaskDetail(jobid_, "51ed", "$..outgoing..actID", token);
							System.out.println("DELETE Transaction failed with error::");
							System.out.println("PORT_MONITOR_BUILD_LOG_EXCERPT_ERROR_START");
//							System.out.println("+------------------------------------------------------------------------------------------------------------------------------------------------+");
							System.out.println("<h4 style=\"background-color: #bebebe;color: #000000;margin-top: 7px;padding: 10px 1px;text-align: left;\">"+
									service+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ENV::"+environment+"\n</h4>\n<b>ACT_ID</b><br>\n" + delActId + "\n\n<br><br><b>ERROR</b><br>\n" + errorString+"<hr>");
							System.out.println("<br>===============================================================================================================");
							System.out.println("ERROR_END");
							System.out.println(
									"+-----------+---------+-----------+---------+-----------+---------+-----------+---------+-----------+---------+");
							System.out.println(errorString);
							System.out.println(
									"+-----------+---------+-----------+---------+-----------+---------+-----------+---------+-----------+---------+");
							System.out.println("Failed to delete Transaction from ACT::" + delActId);
							System.out.println("+-----------------ACT CLEANUP END-----------------------------------------------------------------+");
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					
					
				}
			}
		}
		

		return actCleanupStatus;

	}
	
	
	
	//if explicit environment is provided
	
	
	
	public static ArrayList<String> cleanPortsViaPortMonitorData(String service, String env , String workflow) {
		Asri asri = new Asri();
		ArrayList<String> services = new ArrayList<String>();
		LinkedHashMap<String, String> servicesMap = asri.consolidateServices(service, env);
		ArrayList<String> storeCleanedUni = new ArrayList<String>();
		exVar_IpStatus = "NULL";		
		exVar_AsriStatus = "NULL";
		

		if (servicesMap.size() > 0) {

			services = asri.getRearragedServices(servicesMap, env);
//			for (Iterator iterator = services.iterator(); iterator.hasNext();) {
			while (services.size() > 0) {

//				String s = (String) iterator.next();
				String s = (String) services.get(0);
				System.out.println("Cleanup started for::" + s);

				boolean actCleanUpStatus = IPcleanup.networkCleanup(s, env, workflow);
				if (actCleanUpStatus) {
					System.out.println("Act Cleanup is successful");

					// cleaning Ips
					if (s.contains("IRXX")||s.contains("MVXX")) {
						IPcleanup.cleanIp(s);
					}
					boolean asriCleanUpStatus = IPcleanup.inventoryCleanUp(s, env);
					if (asriCleanUpStatus) {
						System.out.println("ASRI Cleanup is successful");
						

					} else {
						System.out.println("ASRI Cleanup is not successful");
						break;
					}
				} else {
					System.out.println("Act Cleanup is not successful");
					break;
				}
				servicesMap = asri.consolidateServices(service, env);
				services = asri.getRearragedServices(servicesMap, env);

			}
		} else {
			System.out.println("Service Not found in Inventory");

			boolean actCleanUpStatus = IPcleanup.networkCleanup(service, env, workflow);
			if (actCleanUpStatus) {
				System.out.println("Act Cleanup is successful");
				boolean asriCleanUpStatus = IPcleanup.inventoryCleanUp(service, env);
				if (service.contains("IRXX")||service.contains("MVXX")) {
					IPcleanup.cleanIp(service);
				}
				if (asriCleanUpStatus) {
					System.out.println("ASRI Cleanup is successful");
				} else {
					System.out.println("ASRI Cleanup is not successful");
				}
			} else {
				System.out.println("Act Cleanup is not successful");
			}
		}
		exData.put("colAsri_Status", exVar_AsriStatus);
		exData.put("colIP_Status", exVar_IpStatus);
//		
		return storeCleanedUni;

	}
	
	
	public static boolean inventoryCleanUp(String service, String environment) {
		System.out.println("+------------Inventory Cleanup Start---------------+");
		System.out.println("Cleanup Started for Service::" + service + " in Environment::" + environment);
		boolean inventoryCleanUpStatus = false;	
		AUTOPILOT autopilot = new AUTOPILOT();
		Asri asri = new Asri();
		
		ArrayList<String> serviceTypeList = asri.getServiceType(service, environment);
		if (!serviceTypeList.isEmpty()) {
			String serviceType = asri.getServiceType(service, environment).get(0);
			String resName_resType = asri.getReqNameAndReqType(serviceType, environment);
			
			System.out.println("Cleanup Started for Service::" + service);
			System.out.println("Service type::" + serviceType);
			System.out.println("Resource Name and Resource Type::" + resName_resType);
			System.out.println("Environment::" + environment);

			String token = autopilot.getToken(username, password);
			String jobid_ = autopilot.triggerWorkflow(resName_resType, service, "Delete_ResourceByFilter_ASRI_AL", token);
			System.out.println("Triggering workflow::\"Delete_ResourceByFilter_ASRI_AL\"\nJob id::" + jobid_);

			String delResBody = "";
			try {
				System.out.println("Is Workflow completed?::" + autopilot.getWorkflowStatus(jobid_, token));
				delResBody = autopilot.getTaskDetail(jobid_, "7858", "$..outgoing..return_data", token);
				if (delResBody != null && delResBody.contains("successfully")) {
					inventoryCleanUpStatus = true;
					exVar_AsriStatus = "CLEANED";
					exData.put("colAsri_Status", exVar_AsriStatus);
					exData.put("colIP_Status", "NULL");
				} else {
					inventoryCleanUpStatus = false;
					exVar_AsriStatus = "NOT CLEANED";
					exData.put("colAsri_Status", exVar_AsriStatus);
					exData.put("colIP_Status", "NULL");
					delResBody = autopilot.getTaskDetail(jobid_, "69f8", "$..error.response", token);
					System.out.println("Error::" + delResBody);
					
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("+------------Inventory Cleanup End-----------------+");
		} else {
			System.out.println("Service type not found for " + service);
			inventoryCleanUpStatus = true;
			exVar_AsriStatus = "NOT FOUND/CLEANED";
			exData.put("colAsri_Status", exVar_AsriStatus);
			exData.put("colIP_Status", "NULL");
		}		
		return inventoryCleanUpStatus;
	}
	
	
	public void fetchRequestIdFromActId(String actId, String environment) {
		
        //fetch request id from act id
		username = "AC70068";
		password = "RobVanDam@wwf@1992#";
		ArrayList<Map<String, String>> cookiesMap = actAuthentication();
		
		String actFetchUrl = null;
		if (environment.equalsIgnoreCase("1")) {
			actFetchUrl = "http://act-env1.idc1.level3.com:8081/ac-ip-rs-web/rs/view/default/data?q0="+actId;
		}else if(environment.equalsIgnoreCase("2")) {
			actFetchUrl = "http://act-env2.idc1.level3.com:8081/ac-ip-rs-web/rs/view/default/data?q0="+actId;
		}else if(environment.equalsIgnoreCase("4")) {
			actFetchUrl = "http://act-env4.idc1.level3.com:8081/ac-ip-rs-web/rs/view/default/data?q0="+actId;
		}
		
		String x = given().cookies(cookiesMap.get(0)).when().get(actFetchUrl).body().asString();
		JSONObject xmlJSONObj = XML.toJSONObject(x);
		String jsonPrettyPrintString = xmlJSONObj.toString(4);
		
		ArrayList requestIdLength = JsonPath.read(jsonPrettyPrintString, "$..requestID");
		if(requestIdLength.size()>0) {
			System.out.println("RequestID found in ACT::"+requestIdLength.get(0));
        }
		
    }
	
	
	
	
	
	
	

}
