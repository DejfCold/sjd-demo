package cz.davidstudeny.sjddemo.database.repository;

import cz.davidstudeny.sjddemo.database.model.Subscription;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface SubscriptionRepository extends CrudRepository<Subscription, UUID> {
}
