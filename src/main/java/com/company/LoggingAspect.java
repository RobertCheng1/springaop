package com.company;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;


@Aspect // Spring的IoC容器看到@EnableAspectJAutoProxy注解，就会自动查找带有@Aspect的Bean，然后根据每个方法的@Before、@Around等注解把AOP注入到特定的Bean中（和原文略有微调）from: 使用AOP--装配 AOP
@Component
public class LoggingAspect {
	@Before("execution(public * com.company.service.UserService.*(..))")
	public void doAccessCheck() {
		System.err.println("[Before] do access check...");
	}

	@Around("execution(public * com.company.service.MailService.*(..))")
	public Object doLogging(ProceedingJoinPoint pjp) throws Throwable {
		System.err.println("[Around] start " + pjp.getSignature());
		Object retVal = pjp.proceed();
		System.err.println("[Around] done " + pjp.getSignature());
		return retVal;
	}

	//这个表示的在 CustomerService 的每个方法前执行 而不是 WeChatService:
	//说这么一个大白话，是因为尝试过在 WeChatService 的 sendWeChat(该方法就是在 AppConfig 中调用的那个)上加注解  @MetricTime("wheatRelated"),
	//发现并没有发生 zoneId 的 NPE（NullPointerException） 的异常，看下面的评论才恍然大悟 zoneId 是 CustomerService 的字段不是 WeChatService 的
	@Before("execution(public * com.company.advservice.CustomerService.*(..))")
	public void doSomeCheck() {
		System.err.println("[Before] do some check...");
	}

}
