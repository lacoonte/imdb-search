package co.empathy.p01;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan({ "co.empathy.p01.config" })
public class P01Application {

	public static void main(String[] args) {
		SpringApplication.run(P01Application.class, args);
	}

}
