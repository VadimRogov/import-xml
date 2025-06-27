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

    @Column(name = "product_id", unique = true)
    private String productId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "brand")
    private String brand;

    @Column(name = "article")
    private String article;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "image")
    private String image;

    @ManyToMany(mappedBy = "products")
    private Set<Category> categories;

    @Column(name = "group_id")
    private String group;

    @Column(name = "code")
    private String code;

    @Column(name = "product_size")
    private String productSize;

    @Column(name = "matherial", columnDefinition = "TEXT")
    private String matherial;

    @Column(name = "alert")
    private String alert;

    @Column(name = "small_image")
    private String smallImage;

    @Column(name = "super_big_image")
    private String superBigImage;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "status_name")
    private String statusName;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "weight")
    private Integer weight;

    @Column(name = "volume")
    private Integer volume;

    @Column(name = "ondemand")
    private Boolean ondemand;

    @Column(name = "moq")
    private String moq;

    @Column(name = "days")
    private String days;

    @Column(name = "demandtype")
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
        @Column(name = "pack_amount")
        private Integer amount;
        @Column(name = "pack_weight")
        private Integer weight;
        @Column(name = "pack_volume")
        private Integer volume;
        @Column(name = "pack_sizex")
        private Integer sizex;
        @Column(name = "pack_sizey")
        private Integer sizey;
        @Column(name = "pack_sizez")
        private Integer sizez;
        @Column(name = "pack_minpackamount")
        private Integer minpackamount;
    }

    @Embedded
    private Pack pack;

    @Embeddable
    @Data
    public static class Print {
        @Column(name = "print_name")
        private String name;
        @Column(name = "print_description")
        private String description;
    }

    @Embedded
    private Print print;

    @ElementCollection
    @CollectionTable(name = "product_alerts", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "alert")
    private Set<String> alerts;

    /**
     * @deprecated Используйте subproductEntities для связи с субтоварами
     */
    @Deprecated
    @ElementCollection
    @CollectionTable(name = "product_subproducts", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "subproduct_id")
    private Set<String> subproducts;

    @ManyToMany
    @JoinTable(name = "product_subproducts_entities",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "subproduct_id")
    )
    private Set<Product> subproductEntities;

    @Embeddable
    @Data
    public static class ProductAttachment {
        @Column(name = "attachment_meaning")
        private String meaning;
        @Column(name = "attachment_file")
        private String file;
        @Column(name = "attachment_image")
        private String image;
        @Column(name = "attachment_name")
        private String name;
        @Column(name = "attachment_description")
        private String description;
    }

    @ElementCollection
    @CollectionTable(name = "product_attachments", joinColumns = @JoinColumn(name = "product_id"))
    private Set<ProductAttachment> attachments;

    @Embeddable
    @Data
    public static class Price {
        @Column(name = "price_value")
        private java.math.BigDecimal value;
        @Column(name = "price_type")
        private String type;
        @Column(name = "price_currency")
        private String currency;
        @Column(name = "price_date_start")
        private String dateStart;
        @Column(name = "price_date_end")
        private String dateEnd;
    }

    @ElementCollection
    @CollectionTable(name = "product_prices", joinColumns = @JoinColumn(name = "product_id"))
    private Set<Price> prices;

    @Embeddable
    @Data
    public static class Currency {
        @Column(name = "currency_code")
        private String code;
        @Column(name = "currency_rate")
        private java.math.BigDecimal rate;
        @Column(name = "currency_date")
        private String date;
    }

    @ElementCollection
    @CollectionTable(name = "product_currencies", joinColumns = @JoinColumn(name = "product_id"))
    private Set<Currency> currencies;
}
