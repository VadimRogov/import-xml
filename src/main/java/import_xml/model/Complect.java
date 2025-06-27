package import_xml.model;

import lombok.Data;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.Map;

@Data
@Entity
@Table(name = "complects",
        indexes = {
                @Index(name = "idx_complect_id", columnList = "complect_id")
        })
public class Complect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "complect_id", unique = true)
    private String complectId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToMany
    @JoinTable(
            name = "complect_products",
            joinColumns = @JoinColumn(name = "complect_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"),
            indexes = {
                    @Index(name = "idx_complect_products_complect", columnList = "complect_id"),
                    @Index(name = "idx_complect_products_product", columnList = "product_id")
            }
    )
    private Set<Product> products;

    @ElementCollection
    @CollectionTable(
            name = "complect_product_quantities",
            joinColumns = @JoinColumn(name = "complect_id"),
            indexes = {
                    @Index(name = "idx_complect_quantities_complect", columnList = "complect_id"),
                    @Index(name = "idx_complect_quantities_product", columnList = "product_id")
            }
    )
    @MapKeyJoinColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<Product, Integer> productQuantities;

    @Column(name = "tocomplect")
    private Boolean tocomplect;

    @Column(name = "complectprice", precision = 10, scale = 2)
    private java.math.BigDecimal complectprice;

    @ElementCollection
    @CollectionTable(name = "complect_parts", joinColumns = @JoinColumn(name = "complect_id"))
    private Set<ComplectPart> parts;

    @Embeddable
    @Data
    public static class ComplectPart {
        @Column(name = "part_id")
        private String partId;
        @Column(name = "published")
        private Boolean published;
        @Column(name = "product_id")
        private String productId;
        @ManyToOne
        @JoinColumn(name = "product_ref_id", referencedColumnName = "product_id")
        private Product product;
        @Column(name = "code")
        private String code;
        @Column(name = "name")
        private String name;
        @Column(name = "small_image")
        private String smallImage;
        @Column(name = "super_big_image")
        private String superBigImage;
        @Column(name = "print_name")
        private String printName;
        @Column(name = "print_description")
        private String printDescription;
    }
}
