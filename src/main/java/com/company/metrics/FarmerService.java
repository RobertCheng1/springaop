package com.company.metrics;

import org.springframework.stereotype.Component;

@Component
public class FarmerService {

    // 监控 wheatRelated()方法性能:
    @MetricTime("wheatRelated")
    public void wheatRelated() {
        System.out.println("In the wheatRelated");
    }
}
