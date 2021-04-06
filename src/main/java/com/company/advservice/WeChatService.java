package com.company.advservice;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.company.metrics.MetricTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WeChatService {

	@Autowired
	CustomerService customerService;


	public String sendWeChat() {
		//如果通过 customerService 访问 zoneId 会发生异常，因为在LoggingAspect 中给CustomerService加了切面，
		//所以只能通过方法来访问字段
		// ZoneId zoneId = customerService.zoneId;
		ZoneId zoneId = customerService.getZoneId();
		// ZoneId zoneId = customerService.getFinalZoneId();

		System.out.println(zoneId);
		String dt = ZonedDateTime.now(zoneId).toString();
		String info = "Hello, it is " + dt;
		System.out.println(info);
		return "Good Message";
	}
}

