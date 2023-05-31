package cz.davidstudeny.sjddemo.database.repository;

import cz.davidstudeny.sjddemo.database.model.Customer;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface CustomerRepository extends CrudRepository<Customer, UUID> {
}
