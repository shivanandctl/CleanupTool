package stack;

import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;

public class Asri {
		
	public static ArrayList<String> getParentServices(String serviceID) {
		
		String resolvedUrl = IPcleanup.Test1_SASI.replaceAll("service_type", "services");
		resolvedUrl = resolvedUrl+serviceID;
//		System.out.println(resolvedUrl);
		String serviceBody = given().relaxedHTTPSValidation().get(resolvedUrl).body().asString();
		ArrayList<String> parentServiceName = JsonPath.read(serviceBody, "$..parentServices[*].name");
//		ArrayList<String> parentServiceResourceId = JsonPath.read(serviceBody, "$..parentServices[*].id");
//		ArrayList<String> parentServiceResourceType = JsonPath.read(serviceBody, "$..parentServices[*].type");
		ArrayList<LinkedHashMap<String, String>> parentServicesInfo = new ArrayList<LinkedHashMap<String, String>>();
		
		LinkedHashMap<String, String> parentServiceNameType = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> parentServiceNameResId = new LinkedHashMap<String, String>();
		
//		if(parentServiceName.size()>0) {
//			for (int i = 0; i < parentServiceName.size(); i++) {			
//				parentServiceNameType.put(parentServiceResourceType.get(i),parentServiceName.get(i));			
//				parentServiceNameResId.put(parentServiceName.get(i), parentServiceResourceId.get(i));			
//			}
//		}
		
		parentServicesInfo.add(parentServiceNameType);
		parentServicesInfo.add(parentServiceNameResId);

		
//	    Iterator it = parentServiceNameType.entrySet().iterator();
//	    while (it.hasNext()) {
//	        Map.Entry pair = (Map.Entry)it.next();
//	        System.out.println(pair.getKey() + " = " + pair.getValue());		        
//	    }
//	    
//	    Iterator it2 = parentServiceNameResId.entrySet().iterator();
//	    while (it2.hasNext()) {
//	        Map.Entry pair = (Map.Entry)it2.next();
//	        System.out.println(pair.getKey() + " = " + pair.getValue());		        
//	    }
		
		
		return parentServiceName;
		
	}

}
