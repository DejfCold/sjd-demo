package cz.davidstudeny.sjddemo;

import cz.davidstudeny.sjddemo.validators.SubscriptionValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

@SpringBootApplication
public class SjdDemoApplication implements RepositoryRestConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(SjdDemoApplication.class, args);
    }

    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener v) {
        var subscriptionValidator = new SubscriptionValidator();
        v.addValidator("beforeCreate", subscriptionValidator);
        v.addValidator("beforeSave", subscriptionValidator);
    }

}
