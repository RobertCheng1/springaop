package com.company.advservice;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomerService {

	// 成员变量:
	public final ZoneId zoneId = ZoneId.systemDefault();

	// 构造方法:
	public CustomerService() {
		System.out.println("CustomerService(): init...");
		System.out.println("CustomerService(): zoneId = " + this.zoneId);
	}

	// public方法:
	public ZoneId getZoneId() {
		return zoneId;
	}

	// public final方法:
	public final ZoneId getFinalZoneId() {
		return zoneId;
	}
}
