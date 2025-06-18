package import_xml.model;

import lombok.Data;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "categories",
        indexes = {
                @Index(name = "idx_category_id", columnList = "category_id"),
                @Index(name = "idx_parent_id", columnList = "parent_id"),
                @Index(name = "idx_uri", columnList = "uri")
        })
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id", unique = true, length = 50)
    private String categoryId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "parent_id", length = 50)
    private String parentId;

    @Column(name = "uri", length = 255)
    private String uri;

    @Column(name = "level")
    private Integer level;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "image", length = 500)
    private String image;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "page_id", unique = true, length = 50)
    private String pageId;

    @ElementCollection
    @CollectionTable(name = "category_products_on_page", joinColumns = @JoinColumn(name = "category_id"))
    @Column(name = "product_id")
    private Set<String> productsOnPage;

    @ManyToMany
    @JoinTable(
            name = "category_products",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"),
            indexes = {
                    @Index(name = "idx_category_products_category", columnList = "category_id"),
                    @Index(name = "idx_category_products_product", columnList = "product_id")
            }
    )
    private Set<Product> products;
}
