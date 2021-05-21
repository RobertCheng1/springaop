package com.company.metrics;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MetricAspect {
	// 注意 metric()方法标注了 @Around("@annotation(metricTime)")，
	// ===注意不是 @Around("@annotation(MetricTime)")，准确的说这里应该是 metric 方法的第二个参数的参数名而不是参数类型===
	// 它的意思是，符合条件的目标方法是带有 @MetricTime 注解的方法，
	// 因为metric()方法参数类型是MetricTime（注意参数名是metricTime不是MetricTime），我们通过它获取性能监控的名称。
	// 有了@MetricTime注解，再配合MetricAspect，任何Bean，只要方法标注了@MetricTime注解，就可以自动实现性能监控
	@Around("@annotation(metricTime)")
	public Object metric(ProceedingJoinPoint joinPoint, MetricTime metricTime) throws Throwable {
		String name = metricTime.value();
		long start = System.currentTimeMillis();
		try {
			return joinPoint.proceed();
		} finally {
			long t = System.currentTimeMillis() - start;
			// 写入日志或发送至JMX:
			System.out.println("[Metrics] " + name + ": " + t + "ms");
		}
	}
}
