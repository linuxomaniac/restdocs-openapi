package cc.dille.hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.core.EvoInflectorRelProvider;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Bean
    EvoInflectorRelProvider relProvider() {
		return new EvoInflectorRelProvider();
	}
}
