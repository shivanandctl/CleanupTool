package stack;

import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.Map;

import org.json.JSONObject;
import org.json.XML;

import com.jayway.jsonpath.JsonPath;

import io.restassured.response.Response;

public class ACT {
	
	public static String Test4_authUrl = "http://act-env4.idc1.level3.com:8081/ac-ip-rs-web/rs/auth";
	public static String Test4_fetch = "http://act-env4.idc1.level3.com:8081/ac-ip-rs-web/rs/view/default/data?q0=";
	public static String Test4_fetchDetailsFromReqId = "http://act-env4.idc1.level3.com:8081/ac-ip-rs-web/rs/requestPayload?requestID=";
	
	public static String Test1_authUrl = "http://act-env1.idc1.level3.com:8081/ac-ip-rs-web/rs/auth";
	public static String Test1_fetch = "http://act-env1.idc1.level3.com:8081/ac-ip-rs-web/rs/view/default/data?q0=";
	public static String Test1_fetchDetailsFromReqId = "http://act-env1.idc1.level3.com:8081/ac-ip-rs-web/rs/requestPayload?requestID=";
	
	public static String username = "AC70068";
	public static String password = "Shiv1146";
	
	public static ArrayList<Map<String, String>> actAuthentication() {
		
		Response Test1_response = given().relaxedHTTPSValidation().auth().preemptive().basic(username,password)
				.header("authorization", "QUM3MDA2ODpTaGl2MTE0Ng==").get(Test1_authUrl);		
		Map<String, String> Test1_cook = Test1_response.cookies();
		
		Response Test4_response = given().relaxedHTTPSValidation().auth().preemptive().basic(username,password)
				.header("authorization", "QUM3MDA2ODpTaGl2MTE0Ng==").get(Test4_authUrl);		
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
		
		
			String x = given().cookies(cookiesMap.get(0)).when().get(Test1_fetch+serviceAlias).body().asString();
			JSONObject xmlJSONObj = XML.toJSONObject(x);
	        String jsonPrettyPrintString = xmlJSONObj.toString(4);
	        requestID = getRequestID(jsonPrettyPrintString, activity);
	        System.out.println(jsonPrettyPrintString);
	        if(requestID!=null) {
	        	System.out.println("RequestID found in Test1::"+requestID);
	        	//fetching info using requestID
	        	
	        	x = given().when().get(Test1_fetchDetailsFromReqId+requestID).body().asString();
				xmlJSONObj = XML.toJSONObject(x);
		        jsonPrettyPrintString = xmlJSONObj.toString(4);
		        ArrayList identifier_id_list = JsonPath.read(jsonPrettyPrintString, "$..item[?(@.name=='identifier_id')].value");
		        ArrayList header_identifier_list = JsonPath.read(jsonPrettyPrintString, "$..item[?(@.name=='header.identifier')].value");
		        if(identifier_id_list.size()>0&&header_identifier_list.size()>0) {
		        	identifier_id= (String) identifier_id_list.get(0);
		        	header_identifier = (String) header_identifier_list.get(0);
		        	actInfo.add(identifier_id);
		        	actInfo.add(header_identifier);			        	
		        }
	        	
	        }else {
	        	System.out.println("RequestID not found in Test1, checking in Test4");
	        	x = given().cookies(cookiesMap.get(1)).when().get(Test4_fetch+serviceAlias).body().asString();
				xmlJSONObj = XML.toJSONObject(x);
		        jsonPrettyPrintString = xmlJSONObj.toString(4);
		        requestID = getRequestID(jsonPrettyPrintString, activity);
		        if(requestID!=null) {
		        	System.out.println("RequestID found in Test4::"+requestID);
		        	x = given().when().get(Test4_fetchDetailsFromReqId+requestID).body().asString();
					xmlJSONObj = XML.toJSONObject(x);
			        jsonPrettyPrintString = xmlJSONObj.toString(4);
			        ArrayList identifier_id_list = JsonPath.read(jsonPrettyPrintString, "$..item[?(@.name=='identifier_id')].value");
			        ArrayList header_identifier_list = JsonPath.read(jsonPrettyPrintString, "$..item[?(@.name=='header.identifier')].value");
			        if(identifier_id_list.size()>0&&header_identifier_list.size()>0) {
			        	identifier_id= (String) identifier_id_list.get(0);
			        	header_identifier = (String) header_identifier_list.get(0);
			        	actInfo.add(identifier_id);
			        	actInfo.add(header_identifier);			        	
			        }
			        
		        }else {
		        	System.out.println("RequestID not found in Test4 also");
		        }
	        }  		
        return actInfo;
		
	}
	
	
	 public static Integer getRequestID(String jsonPrettyPrintString, String activity) {		 
		 ArrayList requestIdLength = JsonPath.read(jsonPrettyPrintString, "$..requestID");
		 Integer requestID = null;	        
	        for(int i=0;i<requestIdLength.size();i++) {
	        	ArrayList isRequestType = JsonPath.read(jsonPrettyPrintString, "$..rows["+i+"]..data[3].data");
	        	ArrayList isRequestComplete = JsonPath.read(jsonPrettyPrintString, "$..rows["+i+"]..data[0].data");
	        	ArrayList idNewRequest = null;
	        	String reqType = null;
	        	String reqComplete = null;
	        	
	        	if(isRequestType.size()>0) {
	        		 reqType = (String) isRequestType.get(0);
	        		 reqComplete = (String) isRequestComplete.get(0);
	        		 if(reqType.equalsIgnoreCase(activity)&& reqComplete.equalsIgnoreCase("complete")) {
	        			 idNewRequest = JsonPath.read(jsonPrettyPrintString, "$..rows["+i+"]..requestID");
	        			 if(idNewRequest.size()>0) {
	        				 requestID = (Integer) idNewRequest.get(0);
	        			 }	        			 
	        		 }
	        	}
	        }	        
		return requestID;     	
     }
	 

}
