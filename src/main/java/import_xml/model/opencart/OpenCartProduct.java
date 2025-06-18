package import_xml.model.opencart;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OpenCartProduct {
    @JsonProperty("product_id")
    private Long productId;

    private String model;
    private String sku;
    private String upc;
    private String ean;
    private String jan;
    private String isbn;
    private String mpn;
    private String location;
    private Integer quantity;
    private Integer stockStatusId;
    private String image;
    private Boolean shipping;
    private BigDecimal price;
    private Integer points;
    private Integer taxClassId;
    private String dateAvailable;
    private BigDecimal weight;
    private Integer weightClassId;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private Integer lengthClassId;
    private Boolean subtract;
    private Integer minimum;
    private Integer sortOrder;
    private Boolean status;
    private List<Long> categoryIds;
    private List<OpenCartProductDescription> descriptions;
    private List<OpenCartProductImage> images;
    private List<OpenCartProductAttribute> attributes;
    private List<OpenCartProductOption> options;
    private List<OpenCartProductDiscount> discounts;
    private List<OpenCartProductSpecial> specials;

    @Data
    public static class OpenCartProductDescription {
        private Integer languageId;
        private String name;
        private String description;
        private String metaTitle;
        private String metaDescription;
        private String metaKeyword;
        private String tag;
    }

    @Data
    public static class OpenCartProductImage {
        private String image;
        private Integer sortOrder;
    }

    @Data
    public static class OpenCartProductAttribute {
        private Integer attributeId;
        private Integer languageId;
        private String text;
    }

    @Data
    public static class OpenCartProductOption {
        private Integer optionId;
        private Integer optionValueId;
        private Integer quantity;
        private Boolean subtract;
        private BigDecimal price;
        private String pricePrefix;
        private Integer points;
        private String pointsPrefix;
        private BigDecimal weight;
        private String weightPrefix;
    }

    @Data
    public static class OpenCartProductDiscount {
        private Integer customerGroupId;
        private Integer quantity;
        private Integer priority;
        private BigDecimal price;
        private String dateStart;
        private String dateEnd;
    }

    @Data
    public static class OpenCartProductSpecial {
        private Integer customerGroupId;
        private Integer priority;
        private BigDecimal price;
        private String dateStart;
        private String dateEnd;
    }
}