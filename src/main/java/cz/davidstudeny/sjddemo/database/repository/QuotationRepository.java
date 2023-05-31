package cz.davidstudeny.sjddemo.database.repository;

import cz.davidstudeny.sjddemo.database.model.Quotation;
import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

public interface QuotationRepository extends CrudRepository<Quotation, UUID> {
}
