package stack;

import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.Map;

import org.json.JSONObject;
import org.json.XML;

import com.jayway.jsonpath.JsonPath;

import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import utilities.ExcelUtility;
//import ACT.java


public class Test {
	
	IPcleanup ip = new IPcleanup();

	@SuppressWarnings("static-access")
	public static void main(String[] args) {

		IPcleanup ip = new IPcleanup();
		AUTOPILOT ap = new AUTOPILOT();
		Asri asri = new Asri();
		

		// store the above list in an array
		String[] serviceList = { "CO/KXFN/095953/LUMN"};

		ip.username = "AC70068";
		ip.password = "Rsa@2bsafe12345";
		ArrayList<String> ReqID_ServiceType_ReqType = new ArrayList<String>();
		ArrayList<String> actInfo = new ArrayList<String>();

//		String env[] = { "1", "2", "4" };
		String env[] = { "2" };
		String requestType = "infraPort";
		String productType = "infraPort";
		String workflow = "LNAAS_DELETE_TRANSACTION_ACT_TL_V1";
//		String workflow = "DEACTIVATE_TRANSACTION_ACT";

		for (String service : serviceList) {
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			System.out.println("Checking for ServiceID::" + service);
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			for (String environment : env) {
				System.out.println("Checking in Environment::" + environment);
				ReqID_ServiceType_ReqType = ip.getRequestIDsWithServiceType(service, environment, requestType, productType);
				for (String s : ReqID_ServiceType_ReqType) {

//   				System.out.println(s + " " + "Environment: " + environment);
					actInfo = ip.getActDetailsUsingRequestID(s.split("&&")[0], environment);

					if (actInfo.size() > 0) {
						String identifier_id = actInfo.get(0);
						String header_identifier = actInfo.get(1);

						System.out.println(
								"Act request found for ServiceID::" + service + " in the environment::" + environment);
						System.out.println("ServiceID::" + service + "\nIDENTIFIER_ID::" + identifier_id
								+ "\nHEADER_IDENTIFIER::" + header_identifier);
						System.out.println("----------------------------------------------------------");
						
						//triggering autopilot workflow
						ap.environment = environment;
						String token = ap.getToken(ip.username, ip.password);
						//DEACTIVATE_TRANSACTION_ACT
						
						if (workflow.equalsIgnoreCase("DEACTIVATE_TRANSACTION_ACT")) {
							{
								String jobid_ = ap.triggerWorkflow(identifier_id,header_identifier , "Deactivate_Transaction_ACT", token);
								System.out
										.println("Triggering workflow::\"DEACTIVATE_TRANSACTION_ACT\"\nJob id::" + jobid_);
								try {
									System.out.println("Is Workflow completed?::" + ap.getWorkflowStatus(jobid_, token));
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								try {
									String errorString = ap.getTaskDetail(jobid_, "2375", "$..outgoing.return_value", token);
									if (errorString != null) {
										System.out.println("Deactivation completed Successfully");
										ip.cleanIp(service);
										ip.inventoryCleanUp(service, environment);
									} else {
										errorString = ap.getTaskDetail(jobid_, "51ed", "$..outgoing..response", token);
										System.out.println("Deactivation failed with error::");
										System.out.println("............................................................");
										System.out.println(errorString);
										System.out.println("............................................................");

									}
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								
								
							}
                            
						} else if (workflow.equalsIgnoreCase("LNAAS_DELETE_TRANSACTION_ACT_TL_V1")) {
							String jobid_ = ap.triggerWorkflow(identifier_id, header_identifier, workflow, token);				
							System.out.println("Triggering workflow::\""+workflow+"\"\nJob id::" + jobid_);
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
									
									System.out
											.println("Delete Transaction from ACT completed Successfully::" + successDelActId);
									System.out.println("\n=======================ACT CLEANUP END============================");
									System.out.println("====================================================================");
									System.out.println("=========================IP CLEANUP START===========================");
									ip.cleanIp(service);
									ip.inventoryCleanUp(service, environment);
									
									
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
									
									System.out.println("\n=======================ACT CLEANUP END============================");
								}
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
				
						

					}
					else {
						System.out.println("No ACT request found for ServiceID::" + service + " in the environment::"
								+ environment);
					}

				}
			}

			System.out.println("====================================================================");
		}

	}

	
	public void fetchRequestIdFromActId(String actId, String environment) {
        //fetch request id from act id
		ArrayList<Map<String, String>> cookiesMap = ip.actAuthentication();
		
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
