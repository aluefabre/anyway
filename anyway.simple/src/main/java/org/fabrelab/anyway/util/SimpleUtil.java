package org.fabrelab.anyway.util;

import java.util.List;

import org.fabrelab.anyway.constant.ProxyConstant;
import org.simpleframework.http.Address;
import org.simpleframework.http.Request;
import org.simpleframework.http.parse.AddressParser;

public class SimpleUtil {
	
	public static Address getAddress(Request request){
		Address address = request.getAddress();
		if(address.getDomain()!=null){
			return address;
		}else{
			fixAddress(request, address);
		}
		return address;
	}

	private static void fixAddress(Request request, Address address) {
		List<String> enumerationOfHeaderNames = request.getNames();
		for(String stringHeaderName : enumerationOfHeaderNames) {
			if(stringHeaderName.equalsIgnoreCase(ProxyConstant.STRING_HOST_HEADER_NAME)){
				List<String> enumerationOfHeaderValues = request.getValues(stringHeaderName);
				if(enumerationOfHeaderValues.size()>0){
					String value = enumerationOfHeaderValues.get(0);
					String parts[] = value.split(":");
					AddressParser addressParser = (AddressParser)address;
					if(parts.length==1){
						addressParser.setDomain(parts[0]);
						addressParser.setPort(80);
					}else{
						addressParser.setDomain(parts[0]);
						addressParser.setPort(Integer.parseInt(parts[1]));
					}
				}
			}
		}
	}
}
