package com.company.metrics;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect // Spring的IoC容器看到@EnableAspectJAutoProxy注解，就会自动查找带有@Aspect的Bean，然后根据每个方法的@Before、@Around等注解把AOP注入到特定的Bean中（和原文略有微调）from: 使用AOP--装配 AOP
@Component
public class MetricAspect {
	// 注意 metric()方法标注了 @Around("@annotation(metricTime)")，
	// 它的意思是，符合条件的目标方法是带有 @MetricTime 注解的方法，（这是因为 metric方法的第二个参数的类型是MetricTime，另外把本例所有的 metricTime换成metricTimeee 也是可以的）
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
