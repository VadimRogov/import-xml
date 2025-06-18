package import_xml.model.opencart;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OpenCartCategory {
    @JsonProperty("category_id")
    private Long categoryId;

    @JsonProperty("parent_id")
    private Long parentId;

    private String name;
    private String description;
    private String metaTitle;
    private String metaDescription;
    private String metaKeyword;
    private String image;
    private Integer sortOrder;
    private Boolean status;
    private List<OpenCartCategoryDescription> descriptions;
    private List<OpenCartCategoryPath> paths;

    @Data
    public static class OpenCartCategoryDescription {
        private Integer languageId;
        private String name;
        private String description;
        private String metaTitle;
        private String metaDescription;
        private String metaKeyword;
    }

    @Data
    public static class OpenCartCategoryPath {
        private Long categoryId;
        private Long pathId;
        private Integer level;
    }
}
