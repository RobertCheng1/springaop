package com.company;


import com.company.advservice.WeChatService;
import com.company.metrics.FarmerService;
import com.company.service.User;
import com.company.service.UserService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

@Configuration //表示该类是一个配置类，因为我们创建ApplicationContext时，使用的实现类是AnnotationConfigApplicationContext，必须传入一个标注了@Configuration的类名。
@ComponentScan //告诉容器，自动搜索当前类所在的包以及子包，把所有标注为 @Component 的Bean自动创建出来，并根据 @Autowired 进行装配。
@EnableAspectJAutoProxy // Spring的IoC容器看到该注解，就会自动查找带有 @Aspect 的Bean，然后根据每个方法的@Before、@Around等注解把AOP注入到特定的Bean中
public class AppConfig {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		/**
		 * 从 Spring开发--使用 AOP 开始：
		 * AOP是一种新的编程方式，它和OOP不同，OOP把系统看作多个对象的交互，AOP把系统分解为不同的关注点，或者称之为切面（Aspect）
		 * 要理解AOP的概念，我们先用OOP举例，比如一个业务组件BookService，它有几个业务方法：
		 *     createBook：添加新的Book；
		 *     updateBook：修改Book；
		 *     deleteBook：删除Book。
		 * 对每个业务方法，例如，createBook()，除了业务逻辑，还需要安全检查、日志记录和事务处理.
		 * 对于安全检查、日志、事务等代码，它们会重复出现在每个业务方法中。使用OOP，我们很难将这些四处分散的代码模块化。
		 *
		 * AOP原理:
		 * 如何把切面织入到核心逻辑中？这正是AOP需要解决的问题。换句话说，如果客户端获得了BookService的引用，
		 * 当调用bookService.createBook()时，如何对调用方法进行拦截，并在拦截前后进行安全检查、日志、事务等处理，就相当于完成了所有业务功能。
		 *
		 * 在Java平台上，对于AOP的织入，有3种方式：
		 *     编译期：在编译时，由编译器把切面调用编译进字节码，这种方式需要定义新的关键字并扩展编译器，
		 *     		  AspectJ就扩展了Java编译器，使用关键字aspect来实现织入；
		 *     类加载器：在目标类被装载到JVM时，通过一个特殊的类加载器，对目标类的字节码重新“增强”；
		 *     运行期：目标对象和切面都是普通Java类，通过JVM的动态代理功能或者第三方库实现运行期动态织入。
		 * 最简单的方式是第三种，Spring的AOP实现就是基于JVM的动态代理。
		 * 由于JVM的动态代理要求必须实现接口，如果一个普通类没有业务接口，就需要通过CGLIB或者Javassist这些第三方库实现。
		 * AOP技术看上去比较神秘，但实际上，它本质就是一个动态代理，让我们把一些常用功能如权限检查、日志、事务等，从每个业务方法中剥离出来。
		 *
		 *
		 * 使用AOP--装配 AOP:
		 * Spring的IoC容器看到这个（@EnableAspectJAutoProxy）注解，就会自动查找带有 @Aspect 的Bean，===承上启下：对理解AOP流程很有帮助===
		 * 然后根据每个方法的@Before、@Around等注解把AOP注入到特定的Bean中。执行代码，我们可以看到以下输出。
		 * 拦截器类型：
		 *     @Before：这种拦截器先执行拦截代码，再执行目标代码。如果拦截器抛异常，那么目标代码就不执行了；
		 *     @After：这种拦截器先执行目标代码，再执行拦截器代码。无论目标代码是否抛异常，拦截器代码都会执行；
		 *     @AfterReturning：和@After不同的是，只有当目标代码正常返回时，才执行拦截器代码；
		 *     @AfterThrowing：和@After不同的是，只有当目标代码抛出了异常时，才执行拦截器代码；
		 *     @Around：能完全控制目标代码是否执行，并可以在执行前后、抛异常后执行任意拦截代码，可以说是包含了上面所有功能。
		 *
		 * 有些童鞋会问，LoggingAspect定义的方法，是如何注入到其他Bean的呢？===这是个好问题===
		 * 其实AOP的原理非常简单。我们以LoggingAspect.doAccessCheck()为例，要把它注入到UserService的每个public方法中，
		 * 最简单的方法是编写一个子类，并持有原始实例的引用：
		 * 		public UserServiceAopProxy extends UserService {
		 * 		    private UserService target;
		 * 		    private LoggingAspect aspect;
		 *
		 * 		    public UserServiceAopProxy(UserService target, LoggingAspect aspect) {
		 * 		        this.target = target;
		 * 		        this.aspect = aspect;
		 * 		    }
		 *
		 * 		    public User login(String email, String password) {
		 * 		        // 先执行Aspect的代码:
		 * 		        aspect.doAccessCheck();
		 * 		        // 再执行UserService的逻辑:
		 * 		        return target.login(email, password);
		 * 		    }
		 *
		 * 		    public User register(String email, String password, String name) {
		 * 		        aspect.doAccessCheck();
		 * 		        return target.register(email, password, name);
		 * 		    }
		 * 		    ...
		 * 		}
		 * 这些都是Spring容器启动时为我们自动创建的注入了Aspect的子类，它取代了原始的UserService（原始的UserService实例作为
		 * 内部变量隐藏在UserServiceAopProxy中）。如果我们打印从Spring容器获取的UserService实例类型，它类似
		 * UserService$$EnhancerBySpringCGLIB$$1f44e01c，实际上是Spring使用CGLIB动态创建的子类，但对于调用方来说，感觉不到任何区别。
		 * Spring对接口类型使用JDK动态代理，对普通类使用CGLIB创建子类。如果一个Bean的class是final，Spring将无法为其创建子类。
		 * 虽然Spring容器内部实现AOP的逻辑比较复杂（需要使用 AspectJ 解析注解，并通过CGLIB实现代理类），===说到Spring容器内部实现AOP的逻辑===
		 * 但我们使用AOP非常简单，一共需要三步：
		 *     1.定义执行方法，并在方法上通过 AspectJ 的注解告诉 Spring 应该在何处调用此方法；
		 *     2.标记 @Component 和 @Aspect；
		 *     3.在 @Configuration 类上标注 @EnableAspectJAutoProxy。
		 * 至于AspectJ的注入语法则比较复杂，请参考Spring文档。
		 * Spring也提供其他方法来装配AOP，但都没有使用AspectJ注解的方式来得简洁明了，所以我们不再作介绍。
		 * 小结：
		 * 		在Spring容器中使用AOP非常简单，只需要定义执行方法，并用AspectJ的注解标注应该在何处触发并执行。
		 * 		Spring通过CGLIB动态创建子类等方式来实现AOP代理模式，大大简化了代码。
		 */
		ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		UserService userService = context.getBean(UserService.class);
		userService.register("test@example.com", "password", "test");
		userService.login("bob@example.com", "password");
		System.out.println(userService.getClass().getName());

		/**
		 * 使用AOP--使用注解装配 AOP:
		 * 我们在使用 AOP 时，要注意到虽然Spring容器可以把指定的方法通过AOP规则装配到指定的Bean的指定方法前后，
		 * 但是，如果自动装配时，因为不恰当的范围，容易导致意想不到的结果，即很多不需要AOP代理的Bean也被自动代理了，
		 * 并且，后续新增的Bean，如果不清楚现有的AOP装配规则，容易被强迫装配。
		 * 使用 AOP时，被装配的Bean最好自己能清清楚楚地知道自己被安排了；例如，Spring提供的@Transactional就是一个非常好的例子。
		 * 因此，装配AOP的时候，使用注解是最好的方式。===很有实战指导意义===
		 * 小结：
		 * 		使用注解实现AOP需要先定义注解，然后使用@Around("@annotation(name)")实现装配；
		 * 		使用注解既简单，又能明确标识AOP装配，是使用AOP推荐的方式。
		 */
		System.out.println("---Next we will test 使用注解装配 AOP---");
		FarmerService farmerService = context.getBean(FarmerService.class);
		farmerService.wheatRelated();

		/**
		 * 使用AOP--AOP避坑指南：
		 * 无论是使用AspectJ语法，还是配合Annotation，使用AOP，实际上就是让Spring自动为我们创建一个Proxy，使得调用方能无感知地调用指定方法，
		 * 但运行期却动态“织入”了其他逻辑，因此，AOP本质上就是一个代理模式。
		 * 因为Spring使用了CGLIB来实现运行期动态创建Proxy，如果我们没能深入理解其运行原理和实现机制，就极有可能遇到各种诡异的问题。
		 *
		 * 为什么（在 LoggingAspect 中给 CustomerService）加了AOP就报NPE，去了AOP就一切正常？final字段不执行，难道JVM有问题？
		 * 为了解答这个诡异的问题，我们需要深入理解Spring使用CGLIB生成Proxy的原理：
		 * （为了和已有的UserService、MailService区分： 下文讨论的 UserService 是指 CustomerService, MailService 是指 WeChatService）
		 * 第一步，正常创建一个UserService的原始实例，这是通过反射调用构造方法实现的，它的行为和我们预期的完全一致；
		 * 第二步，通过CGLIB创建一个UserService的子类，并引用了原始实例和LoggingAspect：
		 * 		public UserService$$EnhancerBySpringCGLIB extends UserService {
		 * 		    UserService target;
		 * 		    LoggingAspect aspect;
		 *
		 * 		    public UserService$$EnhancerBySpringCGLIB() {
		 * 		    }
		 *
		 * 		    public ZoneId getZoneId() {
		 * 		        aspect.doAccessCheck();
		 * 		        return target.getZoneId();
		 * 		    }
		 * 		}
		 * 如果我们观察Spring创建的 AOP 代理，它的类名总是类似UserService$$EnhancerBySpringCGLIB$$1c76af9d（你没看错，Java的类名实际上允许$字符）。
		 * 为了让调用方获得UserService的引用，它必须继承自UserService。然后，
		 * 该代理类会覆写所有public和protected方法，并在内部将调用委托给原始的UserService实例。
		 * 这里出现了两个UserService实例：
		 * 一个是我们代码中定义的原始实例，它的成员变量已经按照我们预期的方式被初始化完成：
		 * 		UserService original = new UserService();
		 * 第二个UserService实例实际上类型是UserService$$EnhancerBySpringCGLIB，它引用了原始的UserService实例：
		 * 		UserService$$EnhancerBySpringCGLIB proxy = new UserService$$EnhancerBySpringCGLIB();
		 * 		proxy.target = original;
		 * 		proxy.aspect = ...
		 * 注意到这种情况仅出现在启用了AOP的情况，此刻，
		 * 从 ApplicationContext 中获取的 UserService 实例是proxy，注入到 MailService 中的 UserService 实例也是proxy。===说到 AOP 本质了===
		 *
		 * 那么最终的问题来了：proxy实例的成员变量，也就是从UserService继承的zoneId，它的值是null。
		 * 原因在于，UserService成员变量的初始化：
		 * 		public class UserService {
		 *     		public final ZoneId zoneId = ZoneId.systemDefault();
		 *     		...
		 * 		}
		 * 在UserService$$EnhancerBySpringCGLIB中，并未执行。原因是，没必要初始化proxy的成员变量，因为proxy的目的是代理方法。===proxy 的本质===
		 * 实际上，成员变量的初始化是在构造方法中完成的。这是我们看到的代码：
		 * 		public class UserService {
		 * 		    public final ZoneId zoneId = ZoneId.systemDefault();
		 * 		    public UserService() {
		 * 		    }
		 * 		}
		 * 这是编译器实际编译的代码：===Java 类的成员变量的初始化本质===
		 * 		public class UserService {
		 * 		    public final ZoneId zoneId;
		 * 		    public UserService() {
		 * 		        super(); // 构造方法的第一行代码总是调用super()
		 * 		        zoneId = ZoneId.systemDefault(); // 继续初始化成员变量
		 * 		    }
		 * 		}
		 * 然而，对于Spring通过CGLIB动态创建的UserService$$EnhancerBySpringCGLIB代理类，它的构造方法中，并未调用super()，
		 * 因此，从父类继承的成员变量，包括final类型的成员变量，统统都没有初始化。
		 * 有的童鞋会问：Java语言规定，任何类的构造方法，第一行必须调用super()，如果没有，编译器会自动加上，怎么Spring的CGLIB就可以搞特殊？
		 * 这是因为自动加super()的功能是Java编译器实现的，它发现你没加，就自动给加上，发现你加错了，就报编译错误。
		 * 但实际上，如果直接构造字节码，一个类的构造方法中，不一定非要调用super()。
		 * Spring使用CGLIB构造的Proxy类，是直接生成字节码，并没有源码-编译-字节码这个步骤，===CGLIB可以这么神奇===
		 * 因此：Spring通过CGLIB创建的代理类，不会初始化代理类自身继承的任何成员变量，包括final类型的成员变量！
		 * 为什么Spring刻意不初始化Proxy继承的字段？来自下面评论
		 *     1. 因为你初始化的时候很可能会用到注入的其他类：
		 *         @Component
		 *         public class MailService {
		 *             @Value("${smtp.from:xxx}")
		 *             String mailFrom;
		 *
		 *             SmtpSender sender;
		 *
		 *             @PostConstruct
		 *             public void init() {
		 *                 sender = new SmtpSender(mailFrom, ...);
		 *             }
		 *
		 *             public void sentMail(String to) {
		 *                 ...
		 *             }
		 *         }
		 *     你看，MailService的字段sender初始化需要依赖其他注入，并且已经初始化了一次，proxy类没法正确初始化sender
		 *     主要原因就是spring无法在逻辑上正常初始化proxy的字段，所以干脆不初始化，并通过NPE直接暴露出来
		 *     2. 还有一个原因是如果对字段进行修改，proxy的字段其实根本没改：
		 *         @Component
		 *         public class MailService {
		 *             String status = "init";
		 *
		 *             public void sentMail(String to) {
		 *                 this.status = "sent";
		 *             }
		 *         }
		 *     因为只有原始Bean的方法会对自己的字段进行修改，他无法改proxy的字段
		 *
		 * 启用了AOP，如何修复？
		 * 修复很简单，只需要把直接访问字段的代码，改为通过方法访问。
		 * 如果在MailService中，调用的不是getZoneId()，而是getFinalZoneId()，又会出现NullPointerException，
		 * 这是因为，代理类无法覆写final方法（这一点绕不过JVM的ClassLoader检查），该方法返回的是代理类的zoneId字段，即null
		 * 实际上，如果我们加上日志，Spring在启动时会打印一个警告：
		 * 10:43:09.929 [main] DEBUG org.springframework.aop.framework.CglibAopProxy - Final method [public final java.time.ZoneId xxx.UserService.getFinalZoneId()] cannot get proxied via CGLIB: Calls to this method will NOT be routed to the target instance and might lead to NPEs against uninitialized fields in the proxy instance.
		 * 上面的日志大意就是，因为被代理的UserService有一个final方法getFinalZoneId()，这会导致其他Bean如果调用此方法，无法将其代理到真正的原始实例，从而可能发生NPE异常。
		 * 因此，正确使用AOP，我们需要一个避坑指南：
		 *     1. 访问被注入的Bean时，总是调用方法而非直接访问字段；
		 *     2. 编写Bean时，如果可能会被代理，就不要编写 public final方法。
		 * 这样才能保证有没有AOP，代码都能正常工作。
		 */
		System.out.println("---Next we will test AOP避坑---");
		WeChatService weChatService = context.getBean(WeChatService.class);
		weChatService.sendWeChat();
		System.out.println(weChatService.getClass().getName());
	}
}
