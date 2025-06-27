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

    @Column(name = "filter_id", unique = true)
    private String filterId;

    @Column(name = "filter_type_id")
    private String filterTypeId;

    @Column(name = "name", nullable = false)
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

    @Column(name = "filter_type_name")
    private String filterTypeName;

    @OneToMany
    @JoinTable(name = "filter_children", joinColumns = @JoinColumn(name = "parent_id"), inverseJoinColumns = @JoinColumn(name = "child_id"))
    private Set<Filter> filters;

    @Column(name = "filter_name")
    private String filterName;

    @Column(name = "parent_filter_type_id")
    private String parentFilterTypeId;
}
