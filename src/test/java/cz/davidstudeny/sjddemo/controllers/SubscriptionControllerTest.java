package cz.davidstudeny.sjddemo.controllers;

import cz.davidstudeny.sjddemo.database.model.Customer;
import cz.davidstudeny.sjddemo.database.model.Quotation;
import cz.davidstudeny.sjddemo.database.model.Subscription;
import cz.davidstudeny.sjddemo.database.repository.CustomerRepository;
import cz.davidstudeny.sjddemo.database.repository.QuotationRepository;
import cz.davidstudeny.sjddemo.database.repository.SubscriptionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private static String createJsonInput(Quotation quotation, LocalDate startDate, LocalDate validUntil) {
        return String.format("""
                {
                    "quotation": "/quotations/%s",
                    "startDate": "%s",
                    "validUntil": "%s"
                }
                """, quotation.getId(), startDate, validUntil);
    }

    private Customer createCustomer() {
        var customer = new Customer();
        customer.setBirthDate(LocalDate.now().minus(1, ChronoUnit.YEARS));
        customer.setFirstName("SubscriptionCustomer");
        customer.setLastName("SubscriptionCustomer");
        customer.setPhoneNumber("123456789");
        customer.setEmail("subscription.customer@example.com");
        return customerRepository.save(customer);
    }

    private Quotation createQuotation(Customer customer) {
        var quotation = new Quotation();
        quotation.setCustomer(customer);
        quotation.setInsuredAmount(1L);
        quotation.setBeginningOfInsurance(LocalDate.now().minus(1, ChronoUnit.YEARS));
        quotation.setDateOfSigningMortgage(LocalDate.now().minus(1, ChronoUnit.YEARS));

        return quotationRepository.save(quotation);
    }

    private Subscription createSubscription(Quotation quotation) {
        var subscription = new Subscription();
        subscription.setQuotation(quotation);
        subscription.setStartDate(LocalDate.now().minus(1, ChronoUnit.YEARS));
        subscription.setValidUntil(LocalDate.now().plus(1, ChronoUnit.YEARS));

        return subscriptionRepository.save(subscription);
    }


    @Test
    public void shouldRetrieveAllSubscriptions() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$._embedded.subscriptions").isArray());
    }

    @Test
    public void shouldCreateSubscription() throws Exception {

        var customer = createCustomer();
        var quotation = createQuotation(customer);

        var startDate = LocalDate.now().minus(1, ChronoUnit.YEARS);
        var validUntil = LocalDate.now().minus(1, ChronoUnit.YEARS);

        var validJsonInput = createJsonInput(quotation, startDate, validUntil);

        mockMvc.perform(MockMvcRequestBuilders.post("/subscriptions")
                        .content(validJsonInput))
                .andExpect(status().isCreated());
    }

    @Test
    public void shouldFailToCreateCustomerWithValidUntilBeforeStartDate() throws Exception {

        var customer = createCustomer();
        var quotation = createQuotation(customer);

        var startDate = LocalDate.now().plus(1, ChronoUnit.YEARS);
        var validUntil = LocalDate.now().minus(1, ChronoUnit.YEARS);

        var validJsonInput = createJsonInput(quotation, startDate, validUntil);

        mockMvc.perform(MockMvcRequestBuilders.post("/subscriptions")
                        .content(validJsonInput))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void shouldUpdateSubscriptionWithPut() throws Exception {
        var customer = createCustomer();
        var quotation = createQuotation(customer);
        var subscription = createSubscription(quotation);

        var updatedStartDate = quotation.getDateOfSigningMortgage().minus(1, ChronoUnit.YEARS);
        var updatedValidUntil = quotation.getBeginningOfInsurance().plus(1, ChronoUnit.YEARS);
        var updateInput = createJsonInput(
                quotation,
                updatedStartDate,
                updatedValidUntil
        );

        mockMvc.perform(MockMvcRequestBuilders.put("/subscriptions/" + subscription.getId())
                        .content(updateInput))
                .andExpect(status().is2xxSuccessful());

        var updatedSubscription = subscriptionRepository.findById(subscription.getId()).orElseThrow();
        Assertions.assertEquals(updatedValidUntil, updatedSubscription.getValidUntil());
        Assertions.assertEquals(updatedStartDate, updatedSubscription.getStartDate());
        Assertions.assertEquals(quotation.getId(), updatedSubscription.getQuotation().getId());
    }

    @Test
    public void shouldUpdateQuotationWithPatch() throws Exception {
        var customer = createCustomer();
        var quotation = createQuotation(customer);
        var subscription = createSubscription(quotation);

        var updatedValidUntil = subscription.getValidUntil().plus(1, ChronoUnit.YEARS);
        var updateInput = String.format("""
                {
                    "validUntil": "%s"
                }
                """, updatedValidUntil);

        mockMvc.perform(MockMvcRequestBuilders.patch("/subscriptions/" + subscription.getId())
                        .content(updateInput))
                .andExpect(status().is2xxSuccessful());

        var updatedSubscription = subscriptionRepository.findById(subscription.getId()).orElseThrow();
        Assertions.assertEquals(updatedValidUntil, updatedSubscription.getValidUntil());
        Assertions.assertEquals(subscription.getStartDate(), updatedSubscription.getStartDate());
        Assertions.assertEquals(quotation.getId(), updatedSubscription.getQuotation().getId());
    }

    @Test
    public void shouldDeleteSubscription() throws Exception {
        var customer = createCustomer();
        var quotation = createQuotation(customer);
        var subscription = createSubscription(quotation);

        mockMvc.perform(MockMvcRequestBuilders.delete("/subscriptions/" + subscription.getId()))
                .andExpect(status().is2xxSuccessful());

        var isPresent = subscriptionRepository.findById(subscription.getId()).isPresent();
        Assertions.assertFalse(isPresent);
    }
}
