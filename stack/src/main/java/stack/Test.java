package stack;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Test {

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		
		IPcleanup ip = new IPcleanup();

		ip.username = "AC70068";
		ip.password = "Arshi1994#";
		ArrayList<String> ReqID_ServiceType_ReqType = new ArrayList<String>();
		ReqID_ServiceType_ReqType = ip.getRequestIDs("CO/IRXX/046748/LUMN", "4");
		for (String s : ReqID_ServiceType_ReqType) {
			System.out.println(s);
		}
		
	}

}
