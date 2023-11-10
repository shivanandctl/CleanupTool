package stack;

import static io.restassured.RestAssured.given;
import stack.AUTOPILOT;
import utilities.ExcelUtility;
import stack.ServiceCleanupUtility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import io.restassured.response.Response;

public class IPcleanup {

	public static String Test4_authUrl = "http://act-env4.idc1.level3.com:8081/ac-ip-rs-web/rs/auth";
	public static String Test4_fetch = "http://act-env4.idc1.level3.com:8081/ac-ip-rs-web/rs/view/default/data?q0=";
	public static String Test4_fetchDetailsFromReqId = "http://act-env4.idc1.level3.com:8081/ac-ip-rs-web/rs/requestPayload?requestID=";

	public static String Test1_authUrl = "http://act-env1.idc1.level3.com:8081/ac-ip-rs-web/rs/auth";
	public static String Test1_fetch = "http://act-env1.idc1.level3.com:8081/ac-ip-rs-web/rs/view/default/data?q0=";
	public static String Test1_fetchDetailsFromReqId = "http://act-env1.idc1.level3.com:8081/ac-ip-rs-web/rs/requestPayload?requestID=";

	public static String Test1_SASI = "https://sasi-test1.kubeodc-test.corp.intranet/inventory/v1/asri/service_type?name=";
	public static String Test4_SASI = "https://sasi-test4.kubeodc-test.corp.intranet/inventory/v1/asri/service_type?name=";

	public static String Test_GET_IP = "https://sasi-sasiwrap-test1.kubeodc.corp.intranet/wrappers/nisws/ipBlocks?circuitId=";
	public static String Test_IP_Release = "https://sasi-sasiwrap-test1.kubeodc.corp.intranet/wrappers/nisws/ipRelease";

	public static String username;
	public static String password;

	public static String environment;
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

//	    try {
//			myconsole = new PrintStream(new File(fileDir+"\\IP_DATA\\CLEANED_IP_FILES\\CleanedIpSheet_"+dateTime+".txt"));
//			System.setOut(myconsole);
//
//		    
//		} catch (FileNotFoundException e) {
//			System.out.println(e);
//		}

		eu.readExcelData();
//		Asri.getParentServices("CO/KXFN/035470/LUMN");
		
	}

	public static void deactivateService(String serviceID) {

		ArrayList<String> actDeactivate;
		ArrayList<String> act;
		String serviceAlias = serviceID;
		exVar_ServiceAlias = serviceAlias;
		exData.put("colService", exVar_ServiceAlias);
		// checking for successful delete request
		System.out.println("\n=======================ACT CLEANUP START============================");
		System.out.println("------------checking for successful \"delete\" request------------------");
		act = getJSONresponseFromServiceAlias(serviceAlias, "delete");

		if (act.size() == 0) {
			System.out.println("\n------------checking for successful \"deactivate\" request---------------");
			actDeactivate = getJSONresponseFromServiceAlias(serviceAlias, "deactivate");
			if (actDeactivate.size() > 0 && actDeactivate.get(0) != null) {
				System.out.println("Service already deactivated!!\nActID::" + actDeactivate.get(0));
				exVar_ActStatus = "CLEANED";
				exVar_ActId = actDeactivate.get(0);
				exData.put("colActStatus", exVar_ActStatus);
				exData.put("colActId", "Deactivate::" + exVar_ActId);
				exData.put("colError", "NULL");
				exData.put("colDeactivateJobId", "NULL");
			} else {
				{
					System.out.println(
							"\n-----------No successful \"delete\" or \"deactivate\" request found!!!---------");
					exData.put("colActStatus", "NOT FOUND");
					System.out.println("\n-----------Checking for successful \"new\" request---------------");
					act = getJSONresponseFromServiceAlias(serviceAlias, "new");
					if (act.size() > 0) {
						String token = ap.getToken(username, password);
//						System.out.println("Logging into Autopilot\nToken generated::"+token+"\nLogged in Successfully");
						System.out.println("Logging into Autopilot\nLogged in Successfully");
						String jobid_ = ap.triggerWorkflow(act.get(0), act.get(1), "LNAAS_DELETE_TRANSACTION_ACT_TL_V1",
								token);
						System.out.println(
								"Triggering workflow::\"LNAAS_DELETE_TRANSACTION_ACT_TL_V1\"\n Job id::" + jobid_);
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
								System.out.println("............................................................");
								System.out.println(errorString);
								System.out.println("............................................................");
								exVar_Error = errorString;
								exData.put("colError", exVar_Error);

								exVar_ActStatus = "NOT CLEANED";
								exVar_ActId = delActId;
								exData.put("colActStatus", exVar_ActStatus);
								exData.put("colActId", "Delete::" + exVar_ActId);
								exData.put("colRequestId", "NULL");

								// Triggering DEACTIVATE workflow as we got error using DELETE workflow
								if (errorString != null || AUTOPILOT.iterationCount>=20) {
									System.out.println(
											"\n===Triggering DEACTIVATE workflow as we got error using DELETE workflow===\n");
									jobid_ = ap.triggerWorkflow(act.get(0), act.get(1), "Deactivate_Transaction_ACT",
											token);
									System.out.println(
											"Triggering workflow::\"DEACTIVATE_TRANSACTION_ACT\"\n job id::" + jobid_);
									exVar_DeactivateJobId = jobid_;
									exData.put("colDeactivateJobId", "Deactivate::" + exVar_DeactivateJobId);
									try {
										System.out.println(
												"Is Workflow completed?::" + ap.getWorkflowStatus(jobid_, token));
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									try {
										errorString = ap.getTaskDetail(jobid_, "2375", "$..outgoing.return_value",
												token);
										if (errorString != null) {
											System.out.println("Deactivation completed Successfully");
//											String successDelActId = ap.getTaskDetail(jobid_, "662a","$..actIdentifierId", token);
											exData.put("colActStatus", "CLEANED");
											exData.put("colRequestId", "NULL");
											exData.put("colActId", "NULL");
											exData.put("colError", "NULL");
										} else {
											errorString = ap.getTaskDetail(jobid_, "51ed", "$..outgoing..response",
													token);
//											delActId = ap.getTaskDetail(jobid_, "51ed","$..outgoing..actID", token);
											System.out.println("Deactivation failed with error::");
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
			}
		} else {
			System.out.println("Service already deactivated!!\nActID::" + act.get(0));
			exVar_ActStatus = "CLEANED";
			exVar_ActId = act.get(0);
			exData.put("colActStatus", exVar_ActStatus);
			exData.put("colActId", "Delete::" + exVar_ActId);
			exData.put("colError", "NULL");
			exData.put("colDeactivateJobId", "NULL");
		}

		System.out.println("==============================================ACT CLEANUP END===================================================\n\n");

	}
	
	
	public static void deleteDeactivateServiceFromACT(String serviceID) {

		ArrayList<String> actDeactivate;
		ArrayList<String> act;
		String serviceAlias = serviceID;
		exVar_ServiceAlias = serviceAlias;
		exData.put("colService", exVar_ServiceAlias);
		// checking for successful delete request
		System.out.println("\n==============================================ACT CLEANUP START===================================================");
		
		{
			exData.put("colActStatus", "NOT FOUND");
			System.out.println("\n-----------Checking for successful \"new\" request---------------");
			act = getJSONresponseFromServiceAlias(serviceAlias, "new");
			if (act.size() > 0) {
				String token = ap.getToken(username, password);
//				System.out.println("Logging into Autopilot\nToken generated::"+token+"\nLogged in Successfully");
				System.out.println("Logging into Autopilot\nLogged in Successfully");
				String jobid_ = ap.triggerWorkflow(act.get(0), act.get(1), "LNAAS_DELETE_TRANSACTION_ACT_TL_V1",
						token);
				System.out.println(
						"Triggering workflow::\"LNAAS_DELETE_TRANSACTION_ACT_TL_V1\"\nJob id::" + jobid_);
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
						if (errorString != null || AUTOPILOT.iterationCount>=20) {
							System.out.println(
									"\n===Triggering DEACTIVATE workflow as we got error using DELETE workflow===\n");
							jobid_ = ap.triggerWorkflow(act.get(0), act.get(1), "Deactivate_Transaction_ACT",
									token);
							System.out.println(
									"Triggering workflow::\"DEACTIVATE_TRANSACTION_ACT\"\nJob id::" + jobid_);
							exVar_DeactivateJobId = jobid_;
							exData.put("colDeactivateJobId", "Deactivate::" + exVar_DeactivateJobId);
							try {
								System.out.println(
										"Is Workflow completed?::" + ap.getWorkflowStatus(jobid_, token));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							try {
								errorString = ap.getTaskDetail(jobid_, "2375", "$..outgoing.return_value",
										token);
								if (errorString != null) {
									System.out.println("Deactivation completed Successfully");
//									String successDelActId = ap.getTaskDetail(jobid_, "662a","$..actIdentifierId", token);
									exData.put("colActStatus", "CLEANED");
									exData.put("colRequestId", "NULL");
									exData.put("colActId", "NULL");
									exData.put("colError", "NULL");
								} else {
									errorString = ap.getTaskDetail(jobid_, "51ed", "$..outgoing..response",
											token);
//									delActId = ap.getTaskDetail(jobid_, "51ed","$..outgoing..actID", token);
									System.out.println("Deactivation failed with error::");
									System.out.println(
											"............................................................");
									System.out.println(errorString);
									System.out.println(
											"............................................................");
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

		System.out.println("==============================================ACT CLEANUP END===================================================\n\n");

	}

	public static boolean deleteServicefromAsri(String serviceID, String serviceType) {
		System.out.println("==============================================ASRI CLEANUP START=================================================");
		boolean serviceCleanedInAsri = false;
		boolean isInternetService = false;
		String resolvedUrl = Test1_SASI.replaceAll("service_type", serviceType);
		resolvedUrl = resolvedUrl + serviceID;
//		System.out.println(resolvedUrl);
		String serviceBody = given().relaxedHTTPSValidation().get(resolvedUrl).body().asString();
		ArrayList<String> resourceId = JsonPath.read(serviceBody, "$..resources[*].id");
		ArrayList<String> resourceType = JsonPath.read(serviceBody, "$..resources[0].type");
		if (resourceId.size() > 0) {
			System.out.println("Service found in TEST1\nresourceId::" + resourceId.get(0));
			String[] deleteUrl = Test1_SASI.split("service_type");
			String resolvedDeleteUrl = deleteUrl[0] + serviceType + "/".concat(resourceId.get(0));
			System.out.println(resolvedDeleteUrl);
			String delResBody = given().relaxedHTTPSValidation().delete(resolvedDeleteUrl).body().asString();
			System.out.println(delResBody);
			if (delResBody.contains("successfully")) {
				serviceCleanedInAsri = true;
				exVar_AsriStatus = "CLEANED";
				exData.put("colAsri_Status", exVar_AsriStatus);
				if (exVar_Environment.equalsIgnoreCase("NULL")) {
					exVar_Environment = "TEST1";
					exData.put("colEnvironment", exVar_Environment);
				}
				//updation for endpoint
				if(serviceID.contains("_")) {
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
				if(serviceID.contains("_")) {
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
			serviceBody = given().relaxedHTTPSValidation().get(resolvedUrl).body().asString();
			resourceId = JsonPath.read(serviceBody, "$..resources[*].id");
			resourceType = JsonPath.read(serviceBody, "$..resources[0].type");
			if (resourceId.size() > 0) {
				System.out.println("Service found in TEST4\nresourceId::" + resourceId.get(0));
				String[] deleteUrl = Test4_SASI.split("service_type");
				String resolvedDeleteUrl = deleteUrl[0] + serviceType + "/".concat(resourceId.get(0));
//				System.out.println(resolvedDeleteUrl);
				String delResBody = given().relaxedHTTPSValidation().delete(resolvedDeleteUrl).body().asString();
				System.out.println(delResBody);
				if (delResBody.contains("successfully")) {
					serviceCleanedInAsri = true;
					exVar_AsriStatus = "CLEANED";
					exData.put("colAsri_Status", exVar_AsriStatus);
					if (exVar_Environment.equalsIgnoreCase("NULL")) {
						exVar_Environment = "TEST4";
						exData.put("colEnvironment", exVar_Environment);
					}
					if(serviceID.contains("_")) {
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
				if(serviceID.contains("_")) {
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
		System.out.println("==============================================ASRI CLEANUP END===================================================\n\n");
		if (resourceType.size() > 0) {
			if (resourceType.get(0).contains("Internet Access")) {
				isInternetService = true;
			}
		}
		return isInternetService;

	}

	public static boolean cleanIp(String serviceID) {
		System.out.println("==============================================IP CLEANUP START=================================================");
		boolean isIpCleaned = false;
		String resolvedIpUrl = Test_GET_IP + serviceID;
		String iPResBody = given().relaxedHTTPSValidation().get(resolvedIpUrl).body().asString();
		ArrayList<String> ipList = JsonPath.read(iPResBody, "$..ipBlock..cidrRange");
		if (ipList.size() > 0) {
			System.out.println("IPs Found!!\nNumber of IPs occupied by " + serviceID + " is::" + ipList.size());
			for (String ip : ipList) {
				System.out.println("Releasing IP::" + ip);
				String ipReleasePayload = "{\r\n" + "    \"circuitId\" : \"" + serviceID + "\",\r\n"
						+ "    \"cidrRange\" : \"" + ip + "\"\r\n" + "}";
				System.out.println(ipReleasePayload);
				String ipReleaseResponse = given().relaxedHTTPSValidation().header("Content-type", "application/json")
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
		System.out.println("==============================================IP CLEANUP END===================================================\n\n");
		return isIpCleaned;

	}

	public static ArrayList<Map<String, String>> actAuthentication() {

		Response Test1_response = given().relaxedHTTPSValidation().auth().preemptive().basic(username, password)
				.header("authorization", "QUM3MDA2ODpBcnNoaTE5OTQ=").get(Test1_authUrl);
		Map<String, String> Test1_cook = Test1_response.cookies();

		Response Test4_response = given().relaxedHTTPSValidation().auth().preemptive().basic(username, password)
				.header("authorization", "QUM3MDA2ODpBcnNoaTE5OTQ=").get(Test4_authUrl);
		Map<String, String> Test4_cook = Test4_response.cookies();

		ArrayList<Map<String, String>> cookiesMap = new ArrayList<Map<String, String>>();
		cookiesMap.add(Test1_cook);
		cookiesMap.add(Test4_cook);

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
		requestID = getRequestID(jsonPrettyPrintString1, activity);
		if (requestID != null) {
			System.out.println("RequestID found in Test1::" + requestID);
			System.out.println(
					"ACT Url::" + "http://act-env1.idc1.level3.com:8081/ac-ip-rs-web/#/activation/" + requestID);
			AUTOPILOT.environment = "1";
			exVar_Environment = "TEST1";
			exVar_RequestId = requestID;
			exData.put("colEnvironment", exVar_Environment);
			if(activity.equalsIgnoreCase("delete")) {
				exData.put("colRequestId", "Delete::" + exVar_RequestId);
			}else if(activity.equalsIgnoreCase("deactivate")) {
				exData.put("colRequestId", "Deactivate::" + exVar_RequestId);
			}
			else {
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
				header_identifier = (String) header_identifier_list.get(0);
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
			requestID = getRequestID(jsonPrettyPrintString4, activity);
			if (requestID != null) {
				System.out.println("RequestID found in Test4::" + requestID);
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
				if(activity.equalsIgnoreCase("delete")) {
					exData.put("colRequestId", "Delete::" + exVar_RequestId);
				}else if(activity.equalsIgnoreCase("deactivate")) {
					exData.put("colRequestId", "Deactivate::" + exVar_RequestId);
				}else {
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
					header_identifier = (String) header_identifier_list.get(0);
					actInfo.add(identifier_id);
					actInfo.add(header_identifier);
				}

			} else {
				System.out.println("RequestID not found in Test4 also");
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

		return actInfo;

	}

	public static Integer getRequestID(String jsonPrettyPrintString, String activity) {
		ArrayList requestIdLength = JsonPath.read(jsonPrettyPrintString, "$..requestID");
//		 System.out.println(jsonPrettyPrintString);
		Integer requestID = null;
		if (requestIdLength.size() == 1) {
			ArrayList<String> requestActivity = JsonPath.read(jsonPrettyPrintString, "$..data[3].data");
			if (requestActivity.size() > 0 && (activity.equalsIgnoreCase("delete") || activity.equalsIgnoreCase("deactivate"))) {
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
			if(serviceTypeSize>0) {
				String serviceTypeName = (String) givenServiceType.get(serviceTypeSize-1);
//				System.out.println("Given input is of Type::"+serviceTypeName);
				
				for (int i = 0; i < requestIdLength.size(); i++) {
					ArrayList isRequestType = JsonPath.read(jsonPrettyPrintString, "$..rows[" + i + "]..data[3].data");
					ArrayList isRequestComplete = JsonPath.read(jsonPrettyPrintString, "$..rows[" + i + "]..data[0].data");
					ArrayList<String> serviceType = JsonPath.read(jsonPrettyPrintString, "$..rows[" + i + "]..data[5].data");
					
					ArrayList idNewRequest = null;
					String reqType = null;
					String reqComplete = null;


					if (isRequestType.size() > 0) {
						reqType = (String) isRequestType.get(0);
						reqComplete = (String) isRequestComplete.get(0);
						if (reqType.equalsIgnoreCase(activity) && reqComplete.equalsIgnoreCase("complete") && serviceTypeName.equalsIgnoreCase(serviceType.get(0))) {
							idNewRequest = JsonPath.read(jsonPrettyPrintString, "$..rows[" + i + "]..requestID");
							if (idNewRequest.size() > 0) {
								System.out.println("Given input is of Type::"+serviceTypeName);
								requestID = (Integer) idNewRequest.get(0);
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
	
	public static LinkedHashMap<Integer, String> getNewRequestIDs(String serviceAlias, String activity) {
		
		ArrayList<Map<String, String>> cookiesMap = actAuthentication();
		ArrayList<String> actInfo = new ArrayList<String>();
		Integer requestID = null;
		String identifier_id = null;
		String header_identifier = null;

		String x = given().cookies(cookiesMap.get(0)).when().get(Test1_fetch + serviceAlias).body().asString();
		JSONObject xmlJSONObj = XML.toJSONObject(x);
		String jsonPrettyPrintString = xmlJSONObj.toString(4);
		
		ArrayList requestIdLength = JsonPath.read(jsonPrettyPrintString, "$..requestID");
		LinkedHashMap<Integer, String> newRequestIdMap = new LinkedHashMap<Integer, String>();
		Integer requestId = null;
		if (requestIdLength.size() == 1) {			{
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
							newRequestIdMap.put(requestId,serviceTypeName);
						}
					}
				}
			}
		}else {
			ArrayList givenServiceType = JsonPath.read(jsonPrettyPrintString, "$..data[5].data");
			int serviceTypeSize = givenServiceType.size();
			if(serviceTypeSize>0) {
//				String serviceTypeName = (String) givenServiceType.get(serviceTypeSize-1);
//				System.out.println("Given input is of Type::"+serviceTypeName);
				
				for (int i = 0; i < requestIdLength.size(); i++) {
					ArrayList isRequestType = JsonPath.read(jsonPrettyPrintString, "$..rows[" + i + "]..data[3].data");
					ArrayList isRequestComplete = JsonPath.read(jsonPrettyPrintString, "$..rows[" + i + "]..data[0].data");
					ArrayList<String> serviceType = JsonPath.read(jsonPrettyPrintString, "$..rows[" + i + "]..data[5].data");
					
					ArrayList idNewRequest = null;
					String reqType = null;
					String reqComplete = null;
					String serviceTypeName = null;


					if (isRequestType.size() > 0) {
						reqType = (String) isRequestType.get(0);
						reqComplete = (String) isRequestComplete.get(0);
						serviceTypeName = (String) serviceType.get(0);
						if (reqType.equalsIgnoreCase(activity) && reqComplete.equalsIgnoreCase("complete")) {
							idNewRequest = JsonPath.read(jsonPrettyPrintString, "$..rows[" + i + "]..requestID");
							if (idNewRequest.size() > 0) {
								System.out.println("Given input is of Type::"+serviceTypeName);
								requestId = (Integer) idNewRequest.get(0);
								newRequestIdMap.put(requestId,serviceTypeName );
								System.out.println(serviceTypeName+"::"+requestId);
							}
						}
					}
				}
			}
			
		}
		
		return newRequestIdMap;
		
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
			
			ArrayList productType = JsonPath.read(jsonPrettyPrintString,"$..item[?(@.name=='body.productType')].value");
			
			
			ArrayList ovcAlias = JsonPath.read(jsonPrettyPrintString,"$..item[?(@.name=='body.productType')].value");
			if(productType.size()>0) {
				String productypeName = (String) productType.get(0);
				switch (productypeName) {
				case "port":
						System.out.println("Cleaning port");
						ArrayList<String> portAlias = JsonPath.read(jsonPrettyPrintString,"$..item[?(@.name=='body.devices.ports[0].serviceId')].value");
						if(portAlias.size()>0) {
							ServiceCleanupUtility.cleanPort(requestID, portAlias.get(0));
						}
						
					break;
					
				case "elanEvpl":
						System.out.println("Cleaning service ELAN EVPL");
						ArrayList<String> evcAlias = JsonPath.read(jsonPrettyPrintString,"$..item[?(@.name=='body.vrf[0].serviceId')].value");
						if(evcAlias.size()>0) {
							ServiceCleanupUtility.cleanelanEvpl(requestID, evcAlias.get(0));
						}
					break;
				
				case "build_ethernet_path":
					System.out.println("Cleaning service build_ethernet_path");
					ArrayList<String> olineAlias = JsonPath.read(jsonPrettyPrintString,"$..item[?(@.name=='body.endpoints[0].networkPath.serviceId')].value");
					if(olineAlias.size()>0) {
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
			
			System.out.println("identifier_id::"+identifier_id);
			System.out.println("header_identifier::"+header_identifier);
			
			{
				String token = ap.getToken(username, password);
//				System.out.println("Logging into Autopilot\nToken generated::"+token+"\nLogged in Successfully");
				System.out.println("Logging into Autopilot\nLogged in Successfully");
				String jobid_ = ap.triggerWorkflow(identifier_id, header_identifier, "LNAAS_DELETE_TRANSACTION_ACT_TL_V1",
						token);
				System.out.println(
						"Triggering workflow::\"LNAAS_DELETE_TRANSACTION_ACT_TL_V1\"\n Job id::" + jobid_);

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
						if (errorString != null || AUTOPILOT.iterationCount>=20) {
							System.out.println(
									"\n===Triggering DEACTIVATE workflow as we got error using DELETE workflow===\n");
							jobid_ = ap.triggerWorkflow(identifier_id, header_identifier, "Deactivate_Transaction_ACT",
									token);
							System.out.println(
									"Triggering workflow::\"DEACTIVATE_TRANSACTION_ACT\"\n job id::" + jobid_);
							
							try {
								System.out.println(
										"Is Workflow completed?::" + ap.getWorkflowStatus(jobid_, token));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							try {
								errorString = ap.getTaskDetail(jobid_, "2375", "$..outgoing.return_value",
										token);
								if (errorString != null) {
									System.out.println("Deactivation completed Successfully");
//									String successDelActId = ap.getTaskDetail(jobid_, "662a","$..actIdentifierId", token);
								} else {
									errorString = ap.getTaskDetail(jobid_, "51ed", "$..outgoing..response",
											token);
//									delActId = ap.getTaskDetail(jobid_, "51ed","$..outgoing..actID", token);
									System.out.println("Deactivation failed with error::");
									System.out.println(
											"............................................................");
									System.out.println(errorString);
									System.out.println(
											"............................................................");


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
		resolvedUrl = resolvedUrl+serviceID;
		String serviceBody = given().relaxedHTTPSValidation().get(resolvedUrl).body().asString();
		ArrayList<String> parentServiceName = JsonPath.read(serviceBody, "$..parentServices[*].name");
		if(parentServiceName.size()==0) {
			System.out.println("No Parent services found in Test1, checking in Test4");
			resolvedUrl = IPcleanup.Test4_SASI.replaceAll("service_type", "services");
			resolvedUrl = resolvedUrl+serviceID;
			serviceBody = given().relaxedHTTPSValidation().get(resolvedUrl).body().asString();
			parentServiceName = JsonPath.read(serviceBody, "$..parentServices[*].name");
			if(parentServiceName.size()<0) {
				System.out.println("No Parent services found in Test4 also");				
			}
		}else {
			String serviceFromEndpoint[];
			for (String parent : parentServiceName) {
				if(parent.contains("_")) {
					serviceFromEndpoint= parent.split("_");
					parentServiceName.add(serviceFromEndpoint[0]);
					break;
				}
			}
		}
		if(parentServiceName.size()==0) {
			System.out.println("\nNo Parent Associated to the given ServiceID::"+serviceID);
		}else {
			String serviceFromEndpoint[];
			for (String parent : parentServiceName) {
				if(parent.contains("_")) {
					serviceFromEndpoint= parent.split("_");
					parentServiceName.add(serviceFromEndpoint[0]);
					break;
				}
			}
			System.out.println(
					"\n+-----------+---------+-----------+---------+-----------+---------+-----------+---------+-----------+---------+");
			System.out.println("Parent services associated to the given ServiceID::"+serviceID);
			System.out.println(
					"========================================================================");
			for (String parent : parentServiceName) {
				System.out.println(parent);
			}
			System.out.println(
					"+-----------+---------+-----------+---------+-----------+---------+-----------+---------+-----------+---------+");
		}
		return parentServiceName;
		
	}
	
	





	public static void printMap() throws IOException {
		Iterator it = exData.entrySet().iterator();
		System.out.println("+-------------+-------------+-------------+-------------+-------------+-------------+-------------+-------------+");
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			System.out.println(pair.getKey() + " = " + pair.getValue());
		}
		System.out.println("+-------------+-------------+-------------+-------------+-------------+--------------------------+-------------+\n\n");
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
	}

}
