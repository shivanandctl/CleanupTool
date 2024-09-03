package stack;

import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import stack.AUTOPILOT;

import org.json.JSONObject;
import org.json.XML;

import com.jayway.jsonpath.JsonPath;

public class ServiceCleanupUtility {
	
	static AUTOPILOT ap = new AUTOPILOT();

	public static boolean cleanPort(Integer requestID, String portAlias) {
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
		}
		
		
		return result;
	}
	
	
	public static boolean cleanelanEvpl(Integer requestID, String evcAlias) {
		boolean result = false;
		
		String x = given().when().get(IPcleanup.Test1_fetchDetailsFromReqId + requestID).body().asString();
		JSONObject xmlJSONObj = XML.toJSONObject(x);
		String jsonPrettyPrintString = xmlJSONObj.toString(4);
		ArrayList identifier_id_list = JsonPath.read(jsonPrettyPrintString,"$..item[?(@.name=='identifier_id')].value");
		ArrayList header_identifier_list = JsonPath.read(jsonPrettyPrintString,	"$..item[?(@.name=='header.identifier')].value");
		ArrayList endpoint = JsonPath.read(jsonPrettyPrintString,"$..item[?(@.name=='body.endpoints[0].serviceId')].value");
		ArrayList oline = JsonPath.read(jsonPrettyPrintString,	"$..item[?(@.name=='body.endpoints[0].networkPath.serviceId')].value");
		
		
		if (identifier_id_list.size() > 0 && header_identifier_list.size() > 0) {
			String identifier_id = (String) identifier_id_list.get(0);
			String header_identifier = (String) header_identifier_list.get(0);
			
			String oline_ = (String) oline.get(0);
			
			System.out.println("identifier_id::"+identifier_id);
			System.out.println("header_identifier::"+header_identifier);
			
			
			
		}
		
		if(endpoint.size()>0) {
			String endpoint_ = (String) endpoint.get(0);
			System.out.println("endpoint::"+endpoint_);
			IPcleanup.deleteServicefromAsri(endpoint_, "services");
		}
		
//		if(oline.size()>0) {
//			String oline_ = (String) oline.get(0);
//			System.out.println("oline::"+oline_);
//			clean_build_ethernet_path_usingServiceAlias(oline_);
//
//			
//			
//		}
		
		return result;
	}
	
	
//	public static boolean clean_build_ethernet_path_usingServiceAlias(String olineService) {
//		boolean result = false;
//		Integer olineRequestId = null;
//		LinkedHashMap<Integer, String> newRequestIdMap = IPcleanup.getNewRequestIDs(olineService,"new");
//		Iterator it = newRequestIdMap.entrySet().iterator();
//		while (it.hasNext()) {
//			Map.Entry pair = (Map.Entry) it.next();				
//			String producType = (String) pair.getValue();
//			if(producType.equalsIgnoreCase("build_ethernet_path")) {
//				olineRequestId =  (Integer) pair.getKey();
//			}
//		}
//		IPcleanup.environment = "TEST1";
//		IPcleanup.cleanFromACT(olineRequestId);
//		IPcleanup.deleteServicefromAsri(olineService, "services");		
//		return result;
//	}
	
	public static boolean clean_build_ethernet_path_usingRequestId(Integer requestID, String olineAlias) {
		boolean result = false;
		
		String x = given().when().get(IPcleanup.Test1_fetchDetailsFromReqId + requestID).body().asString();
		JSONObject xmlJSONObj = XML.toJSONObject(x);
		String jsonPrettyPrintString = xmlJSONObj.toString(4);
		ArrayList identifier_id_list = JsonPath.read(jsonPrettyPrintString,"$..item[?(@.name=='identifier_id')].value");
		ArrayList header_identifier_list = JsonPath.read(jsonPrettyPrintString,	"$..item[?(@.name=='header.identifier')].value");
		
		
		if (identifier_id_list.size() > 0 && header_identifier_list.size() > 0) {
			String identifier_id = (String) identifier_id_list.get(0);
			String header_identifier = (String) header_identifier_list.get(0);			
			
			
		}
		return result;
	}
	
	
	
	
}
