package cz.davidstudeny.sjddemo.validators;

import cz.davidstudeny.sjddemo.database.model.Subscription;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.format.DateTimeFormatter;

public class SubscriptionValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return Subscription.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Subscription subscription = (Subscription) target;
        var startDate = subscription.getStartDate();
        var validUntil = subscription.getValidUntil();

        if (startDate.isAfter(validUntil)) {
            var dateFormatter = DateTimeFormatter.ISO_DATE;
            var errorMessage = String.format(
                    "The <validUntil> field must be after startDate <%s> but is <%s>",
                    dateFormatter.format(startDate),
                    dateFormatter.format(validUntil)
            );

            errors.rejectValue(
                    "validUntil",
                    "validUntil.beforeStartDate",
                    errorMessage
            );
        }

    }
}
