package cz.davidstudeny.sjddemo.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
public class Quotation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private LocalDate beginningOfInsurance;

    /**
     * The insured amount in a <a href="https://en.wikipedia.org/wiki/Denomination_(currency)#Subunit_and_super_unit">subunit</a>
     * to prevent rounding errors and accommodate varying decimal precision.
     */
    @Min(0)
    private Long insuredAmount;

    private LocalDate dateOfSigningMortgage;
    @ManyToOne
    @NotNull
    private Customer customer;
}
