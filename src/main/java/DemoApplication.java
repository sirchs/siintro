package com.kris.intro.integration;

//import org.springframework.boot.SpringApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;

import java.util.Scanner;

@EnableIntegration
@IntegrationComponentScan

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);
//		final AbstractApplicationContext context = new AnnotationConfigApplicationContext(DemoApplication.class);
//		context.registerShutdownHook();

//		SpringApplication app = new SpringApplication(DemoApplication.class);
//		app.setDefaultProperties(Collections.singletonMap("server.port", "8082"));
//		app.run(args);

		ToChannelDSL.InputGateway inputGateway = context.getBean(ToChannelDSL.InputGateway.class);

		final Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.print("\n Enter a string to send, <enter>: ");
			final String input = scanner.nextLine();
			if ("q".equals(input.trim())) {
				context.close();
				scanner.close();
				break;
			}
//			System.out.printf("Return by gateway: " + inputGateway.upload(input));
			inputGateway.upload(input);
		}
	}
}
