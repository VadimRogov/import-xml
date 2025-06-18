package import_xml.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "products",
        indexes = {
                @Index(name = "idx_product_id", columnList = "product_id"),
                @Index(name = "idx_article", columnList = "article")
        })
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", unique = true, length = 50)
    private String productId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "article", length = 50)
    private String article;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "image", length = 500)
    private String image;

    @ManyToMany(mappedBy = "products")
    private Set<Category> categories;

    @Column(name = "group_id", length = 50)
    private String group;

    @Column(name = "code", length = 100)
    private String code;

    @Column(name = "product_size", length = 100)
    private String productSize;

    @Column(name = "matherial", length = 255)
    private String matherial;

    @Column(name = "alert", columnDefinition = "TEXT")
    private String alert;

    @Column(name = "small_image", length = 500)
    private String smallImage;

    @Column(name = "super_big_image", length = 500)
    private String superBigImage;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "status_name", length = 100)
    private String statusName;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "weight")
    private Integer weight;

    @Column(name = "volume")
    private Integer volume;

    @Column(name = "ondemand")
    private Boolean ondemand;

    @Column(name = "moq", length = 100)
    private String moq;

    @Column(name = "days", length = 100)
    private String days;

    @Column(name = "demandtype", length = 100)
    private String demandtype;

    @Column(name = "multiplicity")
    private Integer multiplicity;

    @ManyToMany
    @JoinTable(
        name = "product_filters",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "filter_id")
    )
    private Set<Filter> filters;

    @Embeddable
    @Data
    public static class Pack {
        private Integer amount;
        private Integer weight;
        private Integer volume;
        private Integer sizex;
        private Integer sizey;
        private Integer sizez;
        private Integer minpackamount;
    }

    @Embedded
    private Pack pack;

    @Embeddable
    @Data
    public static class Print {
        private String name;
        private String description;
    }

    @Embedded
    private Print print;

    @ElementCollection
    @CollectionTable(name = "product_alerts", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "alert")
    private Set<String> alerts;

    @ElementCollection
    @CollectionTable(name = "product_subproducts", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "subproduct_id")
    private Set<String> subproducts;

    @Embeddable
    @Data
    public static class ProductAttachment {
        private String meaning;
        private String file;
        private String image;
        private String name;
    }

    @ElementCollection
    @CollectionTable(name = "product_attachments", joinColumns = @JoinColumn(name = "product_id"))
    private Set<ProductAttachment> attachments;

    @Embeddable
    @Data
    public static class Price {
        private java.math.BigDecimal value;
        private String type;
        private String currency;
        private String dateStart;
        private String dateEnd;
    }

    @ElementCollection
    @CollectionTable(name = "product_prices", joinColumns = @JoinColumn(name = "product_id"))
    private Set<Price> prices;

    @Embeddable
    @Data
    public static class Currency {
        private String code;
        private java.math.BigDecimal rate;
        private String date;
    }

    @ElementCollection
    @CollectionTable(name = "product_currencies", joinColumns = @JoinColumn(name = "product_id"))
    private Set<Currency> currencies;
}
