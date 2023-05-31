package cz.davidstudeny.sjddemo.controllers;

import cz.davidstudeny.sjddemo.database.model.Customer;
import cz.davidstudeny.sjddemo.database.repository.CustomerRepository;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;


    private static String createCustomerJson(LocalDate birthDate) {
        return String.format("""
                {
                  "firstName": "Test",
                  "lastName": "Tester",
                  "email": "test.tester@example.com",
                  "phoneNumber": "123456789",
                  "birthDate": "%s"
                }
                """, birthDate);
    }

    private Customer createCustomer() {
        var customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("Tester");
        customer.setPhoneNumber("123456789");
        customer.setEmail("test.tester@example.com");
        customer.setBirthDate(LocalDate.now().minus(1, ChronoUnit.YEARS));
        return customerRepository.save(customer);
    }

    @Test
    public void shouldReturnListOfCustomers() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/customers"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(new MediaType("application", "hal+json")))
                .andExpect(jsonPath("$").isNotEmpty());
    }


    @Test
    public void shouldCreateCustomer() throws Exception {
        var birthDate = LocalDate.now().minus(2, ChronoUnit.YEARS);
        var jsonInput = createCustomerJson(birthDate);

        mockMvc.perform(MockMvcRequestBuilders.post("/customers").content(jsonInput))
                .andExpect(status().isCreated());
    }

    @Test
    public void shouldFailToCreateCustomerWithBirthDateInTheFuture() {

        var birthDate = LocalDate.now().plus(1, ChronoUnit.YEARS);
        var jsonInput = createCustomerJson(birthDate);

        Assertions.assertThrows(ServletException.class, () ->
                mockMvc.perform(MockMvcRequestBuilders.post("/customers").content(jsonInput))
                        .andExpect(status().isCreated()));

    }

    @Test
    public void shouldFailToCreateCustomerWithMissingFields() throws Exception {
        var invalidJsonInput = """
                {
                    "firstName": "Test",
                    "lastName": "Tester"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/customers").content(invalidJsonInput))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void shouldFailToRetrieveNonExistingCustomer() throws Exception {
        var invalidId = UUID.randomUUID().toString();

        mockMvc.perform(MockMvcRequestBuilders.get("/customers/" + invalidId))
                .andExpect(status().isNotFound());
    }


    @Test
    public void shouldDeleteCustomer() throws Exception {
        var customer = createCustomer();

        mockMvc.perform(MockMvcRequestBuilders.delete(String.format("/customers/%s", customer.getId())))
                .andExpect(status().is2xxSuccessful());

        var isPresent = customerRepository.findById(customer.getId()).isPresent();
        Assertions.assertFalse(isPresent);
    }

    @Test
    public void shouldFailToDeleteNonExistingCustomer() throws Exception {
        var invalidId = UUID.randomUUID().toString();

        mockMvc.perform(MockMvcRequestBuilders.delete("/customers/" + invalidId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldUpdateCustomerWithPut() throws Exception {

        var customer = createCustomer();

        var updatedBirthDate = customer.getBirthDate().minus(1, ChronoUnit.DAYS);
        var updateInput = String.format("""
                {
                  "firstName": "PutTest",
                  "lastName": "PutTester",
                  "email": "put.test.tester@example.com",
                  "phoneNumber": "987654321",
                  "birthDate": "%s"
                }
                """, updatedBirthDate);

        mockMvc.perform(MockMvcRequestBuilders.put("/customers/" + customer.getId()).content(updateInput))
                .andExpect(status().is2xxSuccessful());

        var updatedCustomer = customerRepository.findById(customer.getId()).orElseThrow();
        Assertions.assertEquals("PutTest", updatedCustomer.getFirstName());
        Assertions.assertEquals("PutTester", updatedCustomer.getLastName());
        Assertions.assertEquals("put.test.tester@example.com", updatedCustomer.getEmail());
        Assertions.assertEquals("987654321", updatedCustomer.getPhoneNumber());
        Assertions.assertEquals(updatedBirthDate, updatedCustomer.getBirthDate());

    }


    @Test
    public void shouldUpdateCustomerWithPatch() throws Exception {

        var customer = createCustomer();

        var updateInput = """
                {
                    "firstName": "PatchTest"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.patch("/customers/" + customer.getId()).content(updateInput))
                .andExpect(status().is2xxSuccessful());

        var updatedCustomer = customerRepository.findById(customer.getId()).orElseThrow();
        Assertions.assertEquals("PatchTest", updatedCustomer.getFirstName());
        Assertions.assertEquals(customer.getLastName(), updatedCustomer.getLastName());
        Assertions.assertEquals(customer.getEmail(), updatedCustomer.getEmail());
        Assertions.assertEquals(customer.getPhoneNumber(), updatedCustomer.getPhoneNumber());
        Assertions.assertEquals(customer.getBirthDate(), updatedCustomer.getBirthDate());
    }

    @Test
    public void shouldFailToUpdateNonExistingCustomerWithPatch() throws Exception {
        var invalidId = UUID.randomUUID().toString();
        var updateInput = """
                {
                    "firstName": "UpdatedTest"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.patch("/customers/" + invalidId).content(updateInput))
                .andExpect(status().isNotFound());
    }
}
