package cz.davidstudeny.sjddemo.controllers;

import cz.davidstudeny.sjddemo.database.model.Customer;
import cz.davidstudeny.sjddemo.database.model.Quotation;
import cz.davidstudeny.sjddemo.database.repository.CustomerRepository;
import cz.davidstudeny.sjddemo.database.repository.QuotationRepository;
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
public class QuotationControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private QuotationRepository quotationRepository;


    private static String createJsonInput(Customer customer, LocalDate dateOfSigningMortgage, LocalDate beginningOfInsurance) {
        return String.format("""
                {
                    "beginningOfInsurance": "%s",
                    "insuredAmount": 1,
                    "dateOfSigningMortgage": "%s",
                    "customer": "/customers/%s"
                }
                """, beginningOfInsurance, dateOfSigningMortgage, customer.getId());
    }

    private Customer createCustomer() {
        var customer = new Customer();
        customer.setBirthDate(LocalDate.now().minus(1, ChronoUnit.YEARS));
        customer.setFirstName("QuotationCustomer");
        customer.setLastName("QuotationCustomer");
        customer.setPhoneNumber("123456789");
        customer.setEmail("quotation.customer@example.com");
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

    @Test
    public void shouldRetrieveAllQuotations() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/quotations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$._embedded.quotations").isArray());
    }

    @Test
    public void shouldCreateQuotation() throws Exception {

        var customer = createCustomer();

        var dateOfSigningMortgage = LocalDate.now().minus(1, ChronoUnit.YEARS);
        var beginningOfInsurance = LocalDate.now().minus(1, ChronoUnit.YEARS);

        var validJsonInput = createJsonInput(customer, dateOfSigningMortgage, beginningOfInsurance);

        mockMvc.perform(MockMvcRequestBuilders.post("/quotations")
                        .content(validJsonInput))
                .andExpect(status().isCreated());
    }

    @Test
    public void shouldRetrieveQuotation() throws Exception {
        var customer = createCustomer();
        var quotation = createQuotation(customer);

        mockMvc.perform(MockMvcRequestBuilders.get("/quotations/" + quotation.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"));
    }

    @Test
    public void shouldUpdateQuotationWithPut() throws Exception {
        var customer = createCustomer();
        var quotation = createQuotation(customer);

        var updatedDateOfSigningMortgage = quotation.getDateOfSigningMortgage().minus(1, ChronoUnit.YEARS);
        var updatedBeginningOfInsurance = quotation.getBeginningOfInsurance().minus(1, ChronoUnit.YEARS);
        var updateInput = createJsonInput(
                customer,
                updatedDateOfSigningMortgage,
                updatedBeginningOfInsurance
        );

        mockMvc.perform(MockMvcRequestBuilders.put("/quotations/" + quotation.getId())
                        .content(updateInput))
                .andExpect(status().is2xxSuccessful());

        var updatedQuotation = quotationRepository.findById(quotation.getId()).orElseThrow();
        Assertions.assertEquals(updatedBeginningOfInsurance, updatedQuotation.getBeginningOfInsurance());
        Assertions.assertEquals(updatedDateOfSigningMortgage, updatedQuotation.getDateOfSigningMortgage());
        Assertions.assertEquals(customer.getId(), updatedQuotation.getCustomer().getId());
    }

    @Test
    public void shouldUpdateQuotationWithPatch() throws Exception {
        var customer = createCustomer();
        var quotation = createQuotation(customer);

        var updateInput = """
                {
                    "insuredAmount": 2
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.patch("/quotations/" + quotation.getId())
                        .content(updateInput))
                .andExpect(status().is2xxSuccessful());

        var updatedQuotation = quotationRepository.findById(quotation.getId()).orElseThrow();
        Assertions.assertEquals(2L, updatedQuotation.getInsuredAmount());
        Assertions.assertEquals(quotation.getBeginningOfInsurance(), updatedQuotation.getBeginningOfInsurance());
        Assertions.assertEquals(quotation.getDateOfSigningMortgage(), updatedQuotation.getDateOfSigningMortgage());
        Assertions.assertEquals(customer.getId(), updatedQuotation.getCustomer().getId());
    }

    @Test
    public void shouldDeleteQuotation() throws Exception {
        var customer = createCustomer();
        var quotation = createQuotation(customer);

        mockMvc.perform(MockMvcRequestBuilders.delete("/quotations/" + quotation.getId()))
                .andExpect(status().is2xxSuccessful());

        var isPresent = quotationRepository.findById(quotation.getId()).isPresent();
        Assertions.assertFalse(isPresent);
    }


}
