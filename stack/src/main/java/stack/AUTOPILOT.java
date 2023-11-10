package stack;

//import io.restassured.path.json.JsosnPath;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import com.jayway.jsonpath.JsonPath;

import java.util.ArrayList;

public class AUTOPILOT {
	
	public static int iterationCount = 20; 
	public static String environment;
	
	public String getToken(String userName, String password) {
		String token = null;
		String payload = autopilotLoginPayload(userName,password);
		
		if(environment.contains("1") || environment.contains("TEST1")) {
			token = given().relaxedHTTPSValidation().header("Content-type", "application/json").and().body(payload)
	                .when().post("https://autopilotapp-test1-01.test.intranet:3443/login")
	                .then().extract().response().asString();
		}else if(environment.contains("2") || environment.contains("TEST2")) {
			token = given().relaxedHTTPSValidation().header("Content-type", "application/json").and().body(payload)
	                .when().post("https://autopilotapp-test2-01.test.intranet:3443/login")
	                .then().extract().response().asString();
		}else if(environment.contains("4") || environment.contains("TEST4")) {
			token = given().relaxedHTTPSValidation().header("Content-type", "application/json").and().body(payload)
	                .when().post("https://autopilotapp-test4-01.test.intranet:3443/login")
	                .then().extract().response().asString();
		}
		return token;
		
	}
	
	private String autopilotLoginPayload(String userName,String password) {
		
		String loginPayload = "{\r\n"
				+ "    \"user\": {\r\n"
				+ "        \"username\": \""+userName+"\",\r\n"
				+ "        \"password\": \""+password+"\"\r\n"
				+ "    }\r\n"
				+ "}";
		
		return loginPayload;
		
	}

	public String triggerWorkflow(String identifierId,String header_identifier,String workflowName, String token) {
		String jobId="";
		Response response;
		String payload = getWorkflowJobIDpayload(identifierId,header_identifier, workflowName);

//		System.out.println(payload);
		
		 if(environment.contains("1") || environment.contains("TEST1")) {
				 response = given().relaxedHTTPSValidation().header("cookie", "token=" + token).header("Content-type", "application/json").and().body(payload)
						  .when().post("https://autopilotapp-test1-01.test.intranet:3443/workflow_engine/startJobWithOptions/"+workflowName)
		                  .then().extract().response();
					ArrayList<String> job_id_list = JsonPath.read(response.asString(), "$.._id");
					jobId = job_id_list.get(0);
			}
		 else if(environment.contains("2") || environment.contains("TEST2")) {
				response = given().relaxedHTTPSValidation().header("cookie", "token=" + token).header("Content-type", "application/json").and().body(payload)
						  .when().post("https://autopilotapp-test2-01.test.intranet:3443/workflow_engine/startJobWithOptions/"+workflowName)
		                  .then().extract().response();
				ArrayList<String> job_id_list = JsonPath.read(response.asString(), "$.._id");
				jobId = job_id_list.get(0);
			}
		 else if(environment.contains("4") || environment.contains("TEST4")) {
				response = given().relaxedHTTPSValidation().header("cookie", "token=" + token).header("Content-type", "application/json").and().body(payload)
						  .when().post("https://autopilotapp-test4-01.test.intranet:3443/workflow_engine/startJobWithOptions/"+workflowName)
		                  .then().extract().response();
//				System.out.println(response.prettyPrint());
				ArrayList<String> job_id_list = JsonPath.read(response.asString(), "$.._id");
				jobId = job_id_list.get(0);
			}
		 return jobId;
	}

	public String getWorkflowJobIDpayload(String identifierId, String header_identifier, String workflowName) {
		String payload = null;
		if(workflowName.toLowerCase().contains("delete")) {
			payload = "{\r\n"
					+ "    \"options\" : {\r\n"
					+ "        \"description\" : \"\",\r\n"
					+ "        \"variables\" : {\r\n"
					+ "            \"requestPayload\" : {\r\n"
					+ "                \"correlationId\" : \""+header_identifier+"\",\r\n"
					+ "                \"actIdentifierId\" : \""+identifierId+"\"\r\n"
					+ "            }\r\n"
					+ "        },\r\n"
					+ "        \"groups\" : [\r\n"
					+ "        ],\r\n"
					+ "        \"type\" : \"automation\"\r\n"
					+ "    }\r\n"
					+ "}";
		}else if(workflowName.toLowerCase().contains("deactivate")) {
			payload = "{\r\n"
					+ "    \"options\" : {\r\n"
					+ "        \"description\" : \"\",\r\n"
					+ "        \"variables\" : {\r\n"
					+ "                \"correlationId\" : \""+header_identifier+"\",\r\n"
					+ "                \"identifierId\" : \""+identifierId+"\",\r\n"
					+ "				  \"retryCount\" : \""+0+"\"\r\n"					
					+ "            }\r\n"
					+ "            },\r\n"
					+ "        \"groups\" : [\r\n"
					+ "        ],\r\n"
					+ "        \"type\" : \"automation\"\r\n"
					+ "    }\r\n";
				
		}
		
				
				
				return payload;
			}
		
	public boolean getWorkflowStatus(String jobId, String token) throws InterruptedException {
			boolean isCompleted = false;
			String status = "";
			Response response;
			 if(environment.contains("1") || environment.contains("TEST1")) {
				 response = given().relaxedHTTPSValidation().header("cookie", "token=" + token).and()
						  .when().get("https://autopilotapp-test1-01.test.intranet:3443/workflow_engine/getJobShallow/"+jobId)
		                 .then().extract().response();
				 ArrayList<String> wfStatus_list = JsonPath.read(response.asString(), "$..workflow_end.status");
				 status = wfStatus_list.get(0);
				for (int i = 0; i < iterationCount; i++) {
					if(status.equalsIgnoreCase("complete")) {
						isCompleted=true;
						break;
					}
					System.out.println("iteration::"+(i+1)+" Is Workflow Completed??::"+isCompleted);
					Thread.sleep(30000);
					 response = given().relaxedHTTPSValidation().header("cookie", "token=" + token).and()
							  .when().get("https://autopilotapp-test1-01.test.intranet:3443/workflow_engine/getJobShallow/"+jobId)
			                 .then().extract().response();
//					 System.out.println(response.asPrettyString());
	                 wfStatus_list = JsonPath.read(response.asString(), "$..workflow_end.status");
					 status = wfStatus_list.get(0);
					
				}
			}
		 else if(environment.contains("2") || environment.contains("TEST2")) {
			 response = given().relaxedHTTPSValidation().header("cookie", "token=" + token).and()
					  .when().get("https://autopilotapp-test2-01.test.intranet:3443/workflow_engine/getJobShallow/"+jobId)
	                 .then().extract().response();
			 ArrayList<String> wfStatus_list = JsonPath.read(response.asString(), "$..workflow_end.status");
			 status = wfStatus_list.get(0);
			for (int i = 0; i < iterationCount; i++) {
				if(status.equalsIgnoreCase("complete")) {
					isCompleted=true;
					break;
				}
				System.out.println("iteration::"+(i+1)+" Is Workflow Completed??::"+isCompleted);
				Thread.sleep(30000);
				 response = given().relaxedHTTPSValidation().header("cookie", "token=" + token).and()
						  .when().get("https://autopilotapp-test2-01.test.intranet:3443/workflow_engine/getJobShallow/"+jobId)
		                 .then().extract().response();
//				 System.out.println(response.asPrettyString());
                wfStatus_list = JsonPath.read(response.asString(), "$..workflow_end.status");
				 status = wfStatus_list.get(0);
				
			}
			}
		 else if(environment.contains("4") || environment.contains("TEST4")) {
			 response = given().relaxedHTTPSValidation().header("cookie", "token=" + token).and()
					  .when().get("https://autopilotapp-test4-01.test.intranet:3443/workflow_engine/getJobShallow/"+jobId)
	                 .then().extract().response();
			 ArrayList<String> wfStatus_list = JsonPath.read(response.asString(), "$..workflow_end.status");
			 status = wfStatus_list.get(0);
			for (int i = 0; i < iterationCount; i++) {
				if(status.equalsIgnoreCase("complete")) {
					isCompleted=true;
					break;
				}
				System.out.println("iteration::"+(i+1)+" Is Workflow Completed??::"+isCompleted);
				Thread.sleep(30000);
				 response = given().relaxedHTTPSValidation().header("cookie", "token=" + token).and()
						  .when().get("https://autopilotapp-test4-01.test.intranet:3443/workflow_engine/getJobShallow/"+jobId)
		                 .then().extract().response();
//				 System.out.println(response.asPrettyString());
                 wfStatus_list = JsonPath.read(response.asString(), "$..workflow_end.status");
				 status = wfStatus_list.get(0);
				
			}
		}
		return isCompleted;
		}
	
	
	public static String getTaskDetail(String jobId,String taskId, String jsonPath, String token) throws InterruptedException {
		boolean isCompleted = false;
		String status = "";
		String outgoingAttribute = "";
		Response response;
		 if(environment.contains("1") || environment.contains("TEST1")) {
			 response = given().relaxedHTTPSValidation().header("cookie", "token=" + token).and()
					  .when().get("https://autopilotapp-test1-01.test.intranet:3443/workflow_engine/getTaskIterations/"+jobId+"/"+taskId)
	                 .then().extract().response();
//			 System.out.println(response.asString());
			 {
				 ArrayList<String> outgoing_list = JsonPath.read(response.asString(), jsonPath);
				 if(outgoing_list.size()>0) {
					try {
						outgoingAttribute = outgoing_list.get(0);
					} catch (Exception e) {
						outgoingAttribute ="true";
					}
					  
				 }else {
					 outgoingAttribute = null;
				 }		 			
			}
		}
	 else if(environment.contains("2") || environment.contains("TEST2")) {
		 response = given().relaxedHTTPSValidation().header("cookie", "token=" + token).and()
				  .when().get("https://autopilotapp-test2-01.test.intranet:3443/workflow_engine/getTaskIterations/"+jobId+"/"+taskId)
                .then().extract().response();
//		 System.out.println(response.asString());
		 {
			 ArrayList<String> outgoing_list = JsonPath.read(response.asString(), jsonPath);
			 if(outgoing_list.size()>0) {
					try {
						outgoingAttribute = outgoing_list.get(0);
					} catch (Exception e) {
						outgoingAttribute ="true";
					}
			 }else {
				 outgoingAttribute = null;
			 }		 			
		}
		}
	 else if(environment.contains("4") || environment.contains("TEST4")) {
		 response = given().relaxedHTTPSValidation().header("cookie", "token=" + token).and()
				  .when().get("https://autopilotapp-test4-01.test.intranet:3443/workflow_engine/getTaskIterations/"+jobId+"/"+taskId)
                 .then().extract().response();
//		 System.out.println(response.asString());
		 {
			 ArrayList<String> outgoing_list = JsonPath.read(response.asString(), jsonPath);
			 if(outgoing_list.size()>0) {
					try {
						outgoingAttribute = outgoing_list.get(0);
					} catch (Exception e) {
						outgoingAttribute ="true";
					} 
			 }else {
				 outgoingAttribute = null;
			 }		 			
		}
	}
	return outgoingAttribute;
	}
}
