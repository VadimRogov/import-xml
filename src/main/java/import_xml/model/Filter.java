package import_xml.model;

import lombok.Data;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "filters",
        indexes = {
                @Index(name = "idx_filter_id", columnList = "filter_id"),
                @Index(name = "idx_filter_type_id", columnList = "filter_type_id")
        })
public class Filter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filter_id", unique = true, length = 50)
    private String filterId;

    @Column(name = "filter_type_id", length = 50)
    private String filterTypeId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToMany
    @JoinTable(
            name = "product_filters",
            joinColumns = @JoinColumn(name = "filter_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"),
            indexes = {
                    @Index(name = "idx_product_filters_filter", columnList = "filter_id"),
                    @Index(name = "idx_product_filters_product", columnList = "product_id")
            }
    )
    private Set<Product> products;
}
