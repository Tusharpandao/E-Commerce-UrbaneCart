package in.urbanecart.products.entity;



import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int rating;
    private String comment;
    private LocalDateTime date;

    private String reviewerName;
    private String reviewerEmail;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}

