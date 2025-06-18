package import_xml.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import import_xml.model.Category;
import import_xml.model.Complect;
import import_xml.model.Filter;
import import_xml.model.Product;
import import_xml.repository.CategoryRepository;
import import_xml.repository.ComplectRepository;
import import_xml.repository.FilterRepository;
import import_xml.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class XmlProcessingService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FilterRepository filterRepository;
    private final ComplectRepository complectRepository;
    private final XmlMapper xmlMapper;

    private static final int BATCH_SIZE = 100;
    private static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newInstance();

    @Transactional
    public void processProductsXml(File xmlFile) {
        processXmlFile(xmlFile, "product", this::processProductElement, productRepository::saveAll);
    }

    @Transactional
    public void processStockXml(File xmlFile) {
        processXmlFile(xmlFile, "stock", this::processStockElement, products -> {
            productRepository.saveAll(products);
        });
    }

    @Transactional
    public void processTreeXml(File xmlFile) {
        processXmlFile(xmlFile, "category", this::processCategoryElement, categoryRepository::saveAll);
    }

    @Transactional
    public void processFiltersXml(File xmlFile) {
        processXmlFile(xmlFile, "filter", this::processFilterElement, filterRepository::saveAll);
    }

    @Transactional
    public void processComplectsXml(File xmlFile) {
        processXmlFile(xmlFile, "complect", this::processComplectElement, complectRepository::saveAll);
    }

    private <T> void processXmlFile(File xmlFile, String rootElement,
                                    Function<XMLStreamReader, T> elementProcessor,
                                    Consumer<List<T>> batchSaver) {
        try (FileInputStream fis = new FileInputStream(xmlFile)) {
            XMLStreamReader reader = XML_INPUT_FACTORY.createXMLStreamReader(fis);
            List<T> batch = new ArrayList<>(BATCH_SIZE);

            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamReader.START_ELEMENT && rootElement.equals(reader.getLocalName())) {
                    T element = elementProcessor.apply(reader);
                    if (element != null) {
                        batch.add(element);

                        if (batch.size() >= BATCH_SIZE) {
                            batchSaver.accept(batch);
                            batch.clear();
                        }
                    }
                }
            }

            if (!batch.isEmpty()) {
                batchSaver.accept(batch);
            }

            reader.close();
        } catch (Exception e) {
            log.error("Error processing XML file: {}", xmlFile.getName(), e);
            throw new RuntimeException("Failed to process XML file", e);
        }
    }

    private Product processProductElement(XMLStreamReader reader) {
        try {
            Product product = new Product();
            product.setLastUpdated(LocalDateTime.now());
            product.setIsActive(true);

            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamReader.END_ELEMENT && "product".equals(reader.getLocalName())) {
                    break;
                }

                if (event == XMLStreamReader.START_ELEMENT) {
                    String elementName = reader.getLocalName();
                    String elementText = reader.getElementText();

                    switch (elementName) {
                        case "id":
                            product.setProductId(elementText);
                            break;
                        case "name":
                            product.setName(elementText);
                            break;
                        case "description":
                            product.setDescription(elementText);
                            break;
                        case "price":
                            if (StringUtils.hasText(elementText)) {
                                product.setPrice(new BigDecimal(elementText));
                            }
                            break;
                        case "category":
                            String categoryId = reader.getAttributeValue(null, "id");
                            if (StringUtils.hasText(categoryId)) {
                                Category category = categoryRepository.findByCategoryId(categoryId)
                                        .orElse(null);
                                if (category != null) {
                                    Set<Category> categories = new HashSet<>();
                                    categories.add(category);
                                    product.setCategories(categories);
                                }
                            }
                            break;
                        case "brand":
                            product.setBrand(elementText);
                            break;
                        case "article":
                            product.setArticle(elementText);
                            break;
                        case "image":
                            product.setImage(elementText);
                            break;
                    }
                }
            }

            return validateProduct(product) ? product : null;
        } catch (Exception e) {
            log.error("Error processing product element", e);
            return null;
        }
    }

    private Product processStockElement(XMLStreamReader reader) {
        try {
            String productId = reader.getAttributeValue(null, "product_id");
            String quantityStr = reader.getAttributeValue(null, "quantity");

            if (productId != null && quantityStr != null) {
                return productRepository.findByProductId(productId)
                        .map(product -> {
                            product.setQuantity(Integer.parseInt(quantityStr));
                            product.setLastUpdated(LocalDateTime.now());
                            return product;
                        })
                        .orElse(null);
            }
            return null;
        } catch (Exception e) {
            log.error("Error processing stock element", e);
            return null;
        }
    }

    private Category processCategoryElement(XMLStreamReader reader) {
        try {
            Category category = new Category();
            category.setLastUpdated(LocalDateTime.now());
            category.setIsActive(true);
            category.setCategoryId(reader.getAttributeValue(null, "id"));
            category.setParentId(reader.getAttributeValue(null, "parent_id"));

            String levelStr = reader.getAttributeValue(null, "level");
            if (StringUtils.hasText(levelStr)) {
                category.setLevel(Integer.parseInt(levelStr));
            }

            String sortOrderStr = reader.getAttributeValue(null, "sort_order");
            if (StringUtils.hasText(sortOrderStr)) {
                category.setSortOrder(Integer.parseInt(sortOrderStr));
            }

            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamReader.END_ELEMENT && "category".equals(reader.getLocalName())) {
                    break;
                }

                if (event == XMLStreamReader.START_ELEMENT) {
                    String elementName = reader.getLocalName();
                    String elementText = reader.getElementText();

                    switch (elementName) {
                        case "name":
                            category.setName(elementText);
                            break;
                        case "description":
                            category.setDescription(elementText);
                            break;
                        case "uri":
                            category.setUri(elementText);
                            break;
                        case "image":
                            category.setImage(elementText);
                            break;
                        case "product":
                            String productId = reader.getAttributeValue(null, "id");
                            if (StringUtils.hasText(productId)) {
                                productRepository.findByProductId(productId)
                                        .ifPresent(product -> {
                                            Set<Product> products = category.getProducts();
                                            if (products == null) {
                                                products = new HashSet<>();
                                                category.setProducts(products);
                                            }
                                            products.add(product);
                                        });
                            }
                            break;
                    }
                }
            }

            return validateCategory(category) ? category : null;
        } catch (Exception e) {
            log.error("Error processing category element", e);
            return null;
        }
    }

    private Filter processFilterElement(XMLStreamReader reader) {
        try {
            Filter filter = new Filter();
            filter.setLastUpdated(LocalDateTime.now());
            filter.setIsActive(true);
            filter.setFilterId(reader.getAttributeValue(null, "id"));
            filter.setFilterTypeId(reader.getAttributeValue(null, "type_id"));

            String sortOrderStr = reader.getAttributeValue(null, "sort_order");
            if (StringUtils.hasText(sortOrderStr)) {
                filter.setSortOrder(Integer.parseInt(sortOrderStr));
            }

            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamReader.END_ELEMENT && "filter".equals(reader.getLocalName())) {
                    break;
                }

                if (event == XMLStreamReader.START_ELEMENT) {
                    String elementName = reader.getLocalName();
                    String elementText = reader.getElementText();

                    switch (elementName) {
                        case "name":
                            filter.setName(elementText);
                            break;
                        case "product":
                            String productId = reader.getAttributeValue(null, "id");
                            if (StringUtils.hasText(productId)) {
                                productRepository.findByProductId(productId)
                                        .ifPresent(product -> {
                                            Set<Product> products = filter.getProducts();
                                            if (products == null) {
                                                products = new HashSet<>();
                                                filter.setProducts(products);
                                            }
                                            products.add(product);
                                        });
                            }
                            break;
                    }
                }
            }

            return validateFilter(filter) ? filter : null;
        } catch (Exception e) {
            log.error("Error processing filter element", e);
            return null;
        }
    }

    private Complect processComplectElement(XMLStreamReader reader) {
        try {
            Complect complect = new Complect();
            complect.setLastUpdated(LocalDateTime.now());
            complect.setIsActive(true);
            complect.setComplectId(reader.getAttributeValue(null, "id"));
            Map<Product, Integer> productQuantities = new HashMap<>();

            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamReader.END_ELEMENT && "complect".equals(reader.getLocalName())) {
                    break;
                }

                if (event == XMLStreamReader.START_ELEMENT) {
                    String elementName = reader.getLocalName();
                    String elementText = reader.getElementText();

                    switch (elementName) {
                        case "name":
                            complect.setName(elementText);
                            break;
                        case "description":
                            complect.setDescription(elementText);
                            break;
                        case "product":
                            String productId = reader.getAttributeValue(null, "id");
                            String quantityStr = reader.getAttributeValue(null, "quantity");
                            if (StringUtils.hasText(productId) && StringUtils.hasText(quantityStr)) {
                                productRepository.findByProductId(productId)
                                        .ifPresent(product -> {
                                            productQuantities.put(product, Integer.parseInt(quantityStr));
                                        });
                            }
                            break;
                    }
                }
            }

            complect.setProductQuantities(productQuantities);
            complect.setProducts(productQuantities.keySet());

            return validateComplect(complect) ? complect : null;
        } catch (Exception e) {
            log.error("Error processing complect element", e);
            return null;
        }
    }

    private boolean validateProduct(Product product) {
        return StringUtils.hasText(product.getProductId()) &&
                StringUtils.hasText(product.getName()) &&
                StringUtils.hasText(product.getArticle());
    }

    private boolean validateCategory(Category category) {
        return StringUtils.hasText(category.getCategoryId()) &&
                StringUtils.hasText(category.getName());
    }

    private boolean validateFilter(Filter filter) {
        return StringUtils.hasText(filter.getFilterId()) &&
                StringUtils.hasText(filter.getName()) &&
                StringUtils.hasText(filter.getFilterTypeId());
    }

    private boolean validateComplect(Complect complect) {
        return StringUtils.hasText(complect.getComplectId()) &&
                StringUtils.hasText(complect.getName()) &&
                !complect.getProductQuantities().isEmpty();
    }
}
