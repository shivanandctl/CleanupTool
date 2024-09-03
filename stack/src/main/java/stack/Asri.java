package stack;

import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;


import io.restassured.RestAssured;
import io.restassured.response.Response;

public class Asri {
	
	AUTOPILOT autopilot = new AUTOPILOT();
		
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
	
	
	
	// if environment is explicitly mentioned in the service name
	
	
	// Check if mentioned service exists
	public static String isServiceTypeExists(LinkedHashMap<String, String> servicesMap, String env, String serviceType) {
		String isExist = "false";
		ArrayList<String> services = new ArrayList<String>(servicesMap.values());
		for (String service : services) {
			if (service.equalsIgnoreCase(serviceType)) {
				isExist = "true$"+service+"$"+env;
				//get key for the service
				for (String key : servicesMap.keySet()) {
					if (servicesMap.get(key).equalsIgnoreCase(service)) {
						isExist = isExist + "$" + key;
						break;
					}
				}
				break;
			}
		}
		return isExist;
	}
	
public ArrayList<String> getRearragedServices(LinkedHashMap<String, String> servicesMap, String env){
		
		ArrayList<String> rearragedServices = new ArrayList<String>();
		
		String isMpEvcEndpointExists = isServiceTypeExists(servicesMap, env, "MpEvcEndpoint");
		String isMpEvcServiceExists = isServiceTypeExists(servicesMap, env, "MpEvc");
		
		String isEvcEndpointExists = isServiceTypeExists(servicesMap, env, "EvcEndpoint");
		String isEvcServiceExists = isServiceTypeExists(servicesMap, env, "Evc");
		
		String isOvcEndpointExists = isServiceTypeExists(servicesMap, env, "OvcEndpoint");
		String isOvcServiceExists = isServiceTypeExists(servicesMap, env, "Ovc");
		
		String isIpvpnEndpointExists = isServiceTypeExists(servicesMap, env, "IpVpnEndpoint");
		String isIpvpnServiceExists = isServiceTypeExists(servicesMap, env, "IPVPN");
		
		String isDiaServiceExists = isServiceTypeExists(servicesMap, env, "InternetAccess");
		String isOlineServiceExists = isServiceTypeExists(servicesMap, env, "OLine");
		String isUnisServiceExists = isServiceTypeExists(servicesMap, env, "Uni");
		
		
		if (isMpEvcEndpointExists.contains("true")) {
			rearragedServices.add(isMpEvcEndpointExists.split("\\$")[3]);
		}
		if (isMpEvcServiceExists.contains("true")) {
			rearragedServices.add(isMpEvcServiceExists.split("\\$")[3]);
		}
		
		
		if (isEvcEndpointExists.contains("true")) {
			rearragedServices.add(isEvcEndpointExists.split("\\$")[3]);
		}
		if (isEvcServiceExists.contains("true")) {
			rearragedServices.add(isEvcServiceExists.split("\\$")[3]);
		}
		
		if (isOvcEndpointExists.contains("true")) {
			rearragedServices.add(isOvcEndpointExists.split("\\$")[3]);
		}
		if (isOvcServiceExists.contains("true")) {
			rearragedServices.add(isOvcServiceExists.split("\\$")[3]);
		}
		
		if (isDiaServiceExists.contains("true")) {
			rearragedServices.add(isDiaServiceExists.split("\\$")[3]);
		}
		
		if (isIpvpnEndpointExists.contains("true")) {
			rearragedServices.add(isIpvpnEndpointExists.split("\\$")[3]);
		}
		if (isIpvpnServiceExists.contains("true")) {
			rearragedServices.add(isIpvpnServiceExists.split("\\$")[3]);
		}
		
		if (isOlineServiceExists.contains("true")) {
			rearragedServices.add(isOlineServiceExists.split("\\$")[3]);
		}
		if (isUnisServiceExists.contains("true")) {
			rearragedServices.add(isUnisServiceExists.split("\\$")[3]);
		}
		
		return rearragedServices;
	}


public static LinkedHashMap consolidateServices(String service, String environment) {
	
	Asri asri = new Asri();
    
	LinkedHashMap consolidatedServicesMap = new LinkedHashMap();
	
	//get parent services
	LinkedHashMap parentServices = asri.getParentServices(service, environment);
	for (Object parentService : parentServices.keySet()) {
		String parentServiceName = (String) parentService;
		String parentServiceType = (String) parentServices.get(parentService);
		consolidatedServicesMap.put(parentServiceName, parentServiceType);
	}
	
	//adding the given service to the consolidated services
	ArrayList<String> serviceType = asri.getServiceType(service, environment);
	if (serviceType.size() > 0) {
		String serviceTypeName = serviceType.get(0);
		String resName_resType = asri.getReqNameAndReqType(serviceTypeName, environment);
		String resType = resName_resType.split("_")[1];
		consolidatedServicesMap.put(service, resType);
	}
	
	
	System.out.println("++============================================================++");
	System.out.println("Consolidated Services for " + service + " in ENV:" + environment);
	System.out.println("++============================================================++");
	for (Object consolidatedService : consolidatedServicesMap.keySet()) {
		String consolidatedServiceName = (String) consolidatedService;
		String consolidatedServiceType = (String) consolidatedServicesMap.get(consolidatedService);
		System.out.println(consolidatedServiceName + " : " + consolidatedServiceType);
	}
	
	if (consolidatedServicesMap.isEmpty()) {
		System.out.println("Empty");
	}
	
	System.out.println("++============================================================++");
	
	return consolidatedServicesMap;
}

public ArrayList<String> getServiceType(String service, String environment) {

	String sasiRes = "";
	Response response;
	ArrayList<String> serviceType = new ArrayList<String>();

	if (environment.contains("1")) {
		autopilot.environment = "1";
		response = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and().when()
				.get("https://sasi-test1.kubeodc-test.corp.intranet/inventory/v1/asri/services?name=" + service).then().extract().response();
		sasiRes = response.asString();
	} else if (environment.contains("2")) {
		autopilot.environment = "2";
		response = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and().when()
				.get("https://sasi-test2.kubeodc-test.corp.intranet/inventory/v1/asri/services?name=" + service).then().extract().response();
		sasiRes = response.asString();
	} else if (environment.contains("4")) {
		autopilot.environment = "4";
		response = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and().when()
				.get("https://sasi-test4.kubeodc-test.corp.intranet/inventory/v1/asri/services?name=" + service).then().extract().response();
		sasiRes = response.asString();
	}
	serviceType = JsonPath.read(sasiRes, "$..resources[0].type");

//	if (serviceType.isEmpty()) {
//		System.out.println("No service type found for " + service);
//	} else {
//		System.out.println("Service type for " + service + " is " + serviceType.toString());
//	}

	return serviceType;

}

public LinkedHashMap getParentServices(String service, String environment) {

	String sasiRes = "";
	Response response;
	ArrayList<String> parentServiceName = new ArrayList<String>();
	// linkedhashmap
	LinkedHashMap parentServicesMap = new LinkedHashMap();

	if (environment.contains("1")) {
		autopilot.environment = "1";
		response = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and().when()
				.get("https://sasi-test1.kubeodc-test.corp.intranet/inventory/v1/asri/services?name=" + service).then().extract().response();
		sasiRes = response.asString();
	} else if (environment.contains("2")) {
		autopilot.environment = "2";
		response = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and().when()
				.get("https://sasi-test2.kubeodc-test.corp.intranet/inventory/v1/asri/services?name=" + service).then().extract().response();
		sasiRes = response.asString();
	} else if (environment.contains("4")) {
		autopilot.environment = "4";
		response = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and().when()
				.get("https://sasi-test4.kubeodc-test.corp.intranet/inventory/v1/asri/services?name=" + service).then().extract().response();
		sasiRes = response.asString();
	}
	parentServiceName = JsonPath.read(sasiRes, "$..parentServices[*].name");

	if (parentServiceName.isEmpty()) {
//		System.out.println("+++++++++===================================++++++++");
//		System.out.println("No parent services found for " + service);
//		System.out.println("+++++++++===================================++++++++");
	} else {
//		System.out.println("===================================");
//		System.out.println("Parent services for " + service + " in ENV:"+environment);
//		System.out.println("===================================");
		for (String parent : parentServiceName) {
			if (parent.contains("_")) {
				String ServiceFromEP = parent.split("_")[0];
				ArrayList<String> serviceType = new ArrayList<String>();
				serviceType = getServiceType(ServiceFromEP, environment);
				if (!serviceType.isEmpty()) {
					String serviceTypeName = serviceType.get(0);
					String resName_resType = getReqNameAndReqType(serviceTypeName, environment);
					String resType = resName_resType.split("_")[1];
					parentServicesMap.put(ServiceFromEP, resType);

				}
			}
			{
				ArrayList<String> serviceType = new ArrayList<String>();
				serviceType = getServiceType(parent, environment);
				if (!serviceType.isEmpty()) {
					String serviceTypeName = serviceType.get(0);
					String resName_resType = getReqNameAndReqType(serviceTypeName, environment);
					String resType = resName_resType.split("_")[1];
					parentServicesMap.put(parent, resType);
				}
			}

		}
	}

	return parentServicesMap;

}

public String getReqNameAndReqType(String serviceType, String environment) {
	String resTypeFormatted = serviceType.replaceAll("\\s+", "").toLowerCase();
	String resName = "";
	String resType = "";

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
	return resName_resType;
}

}
