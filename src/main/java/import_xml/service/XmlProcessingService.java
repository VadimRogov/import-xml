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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
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
        try (FileInputStream fis = new FileInputStream(xmlFile)) {
            XMLStreamReader reader = XML_INPUT_FACTORY.createXMLStreamReader(fis);
            List<Product> products = new ArrayList<>();
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamReader.START_ELEMENT && "product".equals(reader.getLocalName())) {
                    Product product = new Product();
                    product.setLastUpdated(LocalDateTime.now());
                    product.setIsActive(true);
                    while (reader.hasNext()) {
                        int innerEvent = reader.next();
                        if (innerEvent == XMLStreamReader.END_ELEMENT && "product".equals(reader.getLocalName())) break;
                        if (innerEvent == XMLStreamReader.START_ELEMENT) {
                            String el = reader.getLocalName();
                            switch (el) {
                                case "product_id": product.setProductId(reader.getElementText()); break;
                                case "group": product.setGroup(readElementAsXml(reader)); break;
                                case "code": product.setCode(reader.getElementText()); break;
                                case "name": product.setName(readElementAsXml(reader)); break;
                                case "product_size": product.setProductSize(readElementAsXml(reader)); break;
                                case "matherial": product.setMatherial(readElementAsXml(reader)); break;
                                case "alert": product.setAlert(readElementAsXml(reader)); break;
                                case "small_image": product.setSmallImage(reader.getElementText()); break;
                                case "super_big_image": product.setSuperBigImage(reader.getElementText()); break;
                                case "content": product.setContent(readElementAsXml(reader)); break;
                                case "status": product.setStatusId(Integer.valueOf(reader.getAttributeValue(null, "id")));
                                    product.setStatusName(readElementAsXml(reader)); break;
                                case "brand": product.setBrand(readElementAsXml(reader)); break;
                                case "barcode": product.setBarcode(reader.getElementText()); break;
                                case "weight": product.setWeight(parseIntSafe(reader.getElementText())); break;
                                case "volume": product.setVolume(parseIntSafe(reader.getElementText())); break;
                                case "pack":
                                    Product.Pack pack = new Product.Pack();
                                    while (reader.hasNext()) {
                                        int packEvent = reader.next();
                                        if (packEvent == XMLStreamReader.END_ELEMENT && "pack".equals(reader.getLocalName())) break;
                                        if (packEvent == XMLStreamReader.START_ELEMENT) {
                                            String packEl = reader.getLocalName();
                                            String packText = reader.getElementText();
                                            switch (packEl) {
                                                case "amount": pack.setAmount(parseIntSafe(packText)); break;
                                                case "weight": pack.setWeight(parseIntSafe(packText)); break;
                                                case "volume": pack.setVolume(parseIntSafe(packText)); break;
                                                case "sizex": pack.setSizex(parseIntSafe(packText)); break;
                                                case "sizey": pack.setSizey(parseIntSafe(packText)); break;
                                                case "sizez": pack.setSizez(parseIntSafe(packText)); break;
                                                case "minpackamount": pack.setMinpackamount(parseIntSafe(packText)); break;
                                            }
                                        }
                                    }
                                    product.setPack(pack);
                                    break;
                                case "print":
                                    Product.Print print = new Product.Print();
                                    while (reader.hasNext()) {
                                        int printEvent = reader.next();
                                        if (printEvent == XMLStreamReader.END_ELEMENT && "print".equals(reader.getLocalName())) break;
                                        if (printEvent == XMLStreamReader.START_ELEMENT) {
                                            String printEl = reader.getLocalName();
                                            switch (printEl) {
                                                case "name": print.setName(readElementAsXml(reader)); break;
                                                case "description": print.setDescription(readElementAsXml(reader)); break;
                                            }
                                        }
                                    }
                                    product.setPrint(print);
                                    break;
                                case "attachments":
                                case "product_attachment":
                                    Set<Product.ProductAttachment> attachments = new HashSet<>();
                                    while (reader.hasNext()) {
                                        int attEvent = reader.next();
                                        if (attEvent == XMLStreamReader.END_ELEMENT && ("attachments".equals(reader.getLocalName()) || "product_attachment".equals(reader.getLocalName()))) break;
                                        if (attEvent == XMLStreamReader.START_ELEMENT && "attachment".equals(reader.getLocalName())) {
                                            Product.ProductAttachment attachment = new Product.ProductAttachment();
                                            while (reader.hasNext()) {
                                                int fieldEvent = reader.next();
                                                if (fieldEvent == XMLStreamReader.END_ELEMENT && "attachment".equals(reader.getLocalName())) break;
                                                if (fieldEvent == XMLStreamReader.START_ELEMENT) {
                                                    String attEl = reader.getLocalName();
                                                    String attText = reader.getElementText();
                                                    switch (attEl) {
                                                        case "meaning": attachment.setMeaning(readElementAsXml(reader)); break;
                                                        case "file": attachment.setFile(attText); break;
                                                        case "image": attachment.setImage(attText); break;
                                                        case "name": attachment.setName(readElementAsXml(reader)); break;
                                                        case "description": attachment.setDescription(readElementAsXml(reader)); break;
                                                    }
                                                }
                                            }
                                            attachments.add(attachment);
                                        }
                                    }
                                    product.setAttachments(attachments);
                                    break;
                                case "subproducts":
                                    Set<String> subproducts = new HashSet<>();
                                    Set<Product> subproductEntities = new HashSet<>();
                                    while (reader.hasNext()) {
                                        int subEvent = reader.next();
                                        if (subEvent == XMLStreamReader.END_ELEMENT && "subproducts".equals(reader.getLocalName())) break;
                                        if (subEvent == XMLStreamReader.START_ELEMENT && "subproduct".equals(reader.getLocalName())) {
                                            try {
                                                String subId = reader.getAttributeValue(null, "product_id");
                                                if (subId != null && !subId.isEmpty()) {
                                                    subproducts.add(subId);
                                                    productRepository.findByProductId(subId).ifPresent(subproductEntities::add);
                                                }
                                            } catch (Exception e) {
                                                log.warn("Ошибка при парсинге subproduct: {}", e.getMessage());
                                            }
                                            skipElement(reader, "subproduct");
                                        }
                                    }
                                    product.setSubproducts(subproducts);
                                    product.setSubproductEntities(subproductEntities);
                                    break;
                                case "filters":
                                    Set<Filter> productFilters = new HashSet<>();
                                    while (reader.hasNext()) {
                                        int filterEvent = reader.next();
                                        if (filterEvent == XMLStreamReader.END_ELEMENT && "filters".equals(reader.getLocalName())) break;
                                        if (filterEvent == XMLStreamReader.START_ELEMENT && "filter".equals(reader.getLocalName())) {
                                            String filterId = reader.getAttributeValue(null, "id");
                                            if (filterId != null) {
                                                Filter filter = filterRepository.findByFilterId(filterId);
                                                if (filter != null) {
                                                    productFilters.add(filter);
                                                } else {
                                                    log.warn("Фильтр с id {} не найден для продукта {}", filterId, product.getProductId());
                                                }
                                            }
                                            // пропустить содержимое filter
                                            skipElement(reader, "filter");
                                        }
                                    }
                                    product.setFilters(productFilters);
                                    break;
                                case "ondemand": product.setOndemand(Boolean.valueOf(reader.getElementText())); break;
                                case "moq": product.setMoq(readElementAsXml(reader)); break;
                                case "days": product.setDays(readElementAsXml(reader)); break;
                                case "demandtype": product.setDemandtype(readElementAsXml(reader)); break;
                                case "multiplicity": product.setMultiplicity(parseIntSafe(reader.getElementText())); break;
                                case "price":
                                    Set<Product.Price> prices = new HashSet<>();
                                    while (reader.hasNext()) {
                                        int priceEvent = reader.next();
                                        if (priceEvent == XMLStreamReader.END_ELEMENT && "price".equals(reader.getLocalName())) break;
                                        if (priceEvent == XMLStreamReader.START_ELEMENT && "item".equals(reader.getLocalName())) {
                                            Product.Price price = new Product.Price();
                                            while (reader.hasNext()) {
                                                int fieldEvent = reader.next();
                                                if (fieldEvent == XMLStreamReader.END_ELEMENT && "item".equals(reader.getLocalName())) break;
                                                if (fieldEvent == XMLStreamReader.START_ELEMENT) {
                                                    String priceEl = reader.getLocalName();
                                                    String priceText = reader.getElementText();
                                                    try {
                                                        switch (priceEl) {
                                                            case "value": price.setValue(new java.math.BigDecimal(priceText)); break;
                                                            case "type": price.setType(priceText); break;
                                                            case "currency": price.setCurrency(priceText); break;
                                                            case "dateStart": price.setDateStart(priceText); break;
                                                            case "dateEnd": price.setDateEnd(priceText); break;
                                                        }
                                                    } catch (Exception e) {
                                                        log.warn("Ошибка при парсинге цены {}: {}", priceEl, priceText);
                                                    }
                                                }
                                            }
                                            prices.add(price);
                                        }
                                    }
                                    product.setPrices(prices);
                                    break;
                                case "currency":
                                    Set<Product.Currency> currencies = new HashSet<>();
                                    while (reader.hasNext()) {
                                        int currEvent = reader.next();
                                        if (currEvent == XMLStreamReader.END_ELEMENT && "currency".equals(reader.getLocalName())) break;
                                        if (currEvent == XMLStreamReader.START_ELEMENT && "item".equals(reader.getLocalName())) {
                                            Product.Currency currency = new Product.Currency();
                                            while (reader.hasNext()) {
                                                int fieldEvent = reader.next();
                                                if (fieldEvent == XMLStreamReader.END_ELEMENT && "item".equals(reader.getLocalName())) break;
                                                if (fieldEvent == XMLStreamReader.START_ELEMENT) {
                                                    String currEl = reader.getLocalName();
                                                    String currText = reader.getElementText();
                                                    try {
                                                        switch (currEl) {
                                                            case "code": currency.setCode(currText); break;
                                                            case "rate": currency.setRate(new java.math.BigDecimal(currText)); break;
                                                            case "date": currency.setDate(currText); break;
                                                        }
                                                    } catch (Exception e) {
                                                        log.warn("Ошибка при парсинге валюты {}: {}", currEl, currText);
                                                    }
                                                }
                                            }
                                            currencies.add(currency);
                                        }
                                    }
                                    product.setCurrencies(currencies);
                                    break;
                                case "alerts":
                                    Set<String> alerts = new HashSet<>();
                                    while (reader.hasNext()) {
                                        int alertEvent = reader.next();
                                        if (alertEvent == XMLStreamReader.END_ELEMENT && "alerts".equals(reader.getLocalName())) break;
                                        if (alertEvent == XMLStreamReader.START_ELEMENT && "alert".equals(reader.getLocalName())) {
                                            try {
                                                String alertText = readElementAsXml(reader);
                                                if (alertText != null && !alertText.isEmpty()) {
                                                    alerts.add(alertText);
                                                }
                                            } catch (Exception e) {
                                                log.warn("Ошибка при парсинге alert: {}", e.getMessage());
                                            }
                                        }
                                    }
                                    product.setAlerts(alerts);
                                    break;
                            }
                        }
                    }
                    // -------------- ПРОВЕРКА НА ИМЯ ПРОДУКТА -------------- //
                    if (product.getName() == null || product.getName().trim().isEmpty()) {
                        log.warn("Продукт без имени пропущен: {}", product.getProductId());
                        continue;
                    }
                    // -------------- ПРОВЕРКА НА ИМЯ ПРОДУКТА -------------- //
                    products.add(product);
                }
            }
            productRepository.saveAll(products);
        } catch (Exception e) {
            log.error("Ошибка при обработке файла товаров: {}", xmlFile.getName(), e);
            throw new RuntimeException("Не удалось обработать файл товаров: " + xmlFile.getName(), e);
        }
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

    @Transactional
    public void processCatalogueXml(File xmlFile) {
        log.info("Начата обработка catalogue.xml (каталог товаров и дерева категорий)");
        try {
            // Сначала парсим дерево категорий
            processTreeXml(xmlFile);
            // Затем парсим товары
            processProductsXml(xmlFile);
            log.info("Обработка catalogue.xml завершена успешно");
        } catch (Exception e) {
            log.error("Ошибка при обработке catalogue.xml: {}", xmlFile.getName(), e);
            throw new RuntimeException("Не удалось обработать файл catalogue.xml: " + xmlFile.getName(), e);
        }
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
            log.error("Ошибка при обработке файла: {}", xmlFile.getName(), e);
            throw new RuntimeException("Не удалось обработать файл: " + xmlFile.getName(), e);
        }
    }

    private Product processStockElement(XMLStreamReader reader) {
        try {
            String productId = reader.getAttributeValue(null, "product_id");
            String quantityStr = reader.getAttributeValue(null, "amount");
            String priceStr = reader.getAttributeValue(null, "enduserprice");
            String statusIdStr = reader.getAttributeValue(null, "status_id");
            String statusName = reader.getAttributeValue(null, "status_name");

            if (productId != null) {
                return productRepository.findByProductId(productId)
                        .map(product -> {
                            if (quantityStr != null) {
                                product.setQuantity(Integer.parseInt(quantityStr));
                            }
                            if (priceStr != null) {
                                try {
                                    product.setPrice(new java.math.BigDecimal(priceStr));
                                } catch (Exception e) {
                                    log.warn("Ошибка при парсинге цены для товара {}: {}", productId, priceStr);
                                }
                            }
                            if (statusIdStr != null) {
                                try {
                                    product.setStatusId(Integer.parseInt(statusIdStr));
                                } catch (Exception e) {
                                    log.warn("Ошибка при парсинге статуса для товара {}: {}", productId, statusIdStr);
                                }
                            }
                            if (statusName != null) {
                                product.setStatusName(statusName);
                            }
                            product.setLastUpdated(LocalDateTime.now());
                            return product;
                        })
                        .orElseGet(() -> {
                            log.warn("Товар с id {} не найден при обновлении остатков", productId);
                            return null;
                        });
            }
            return null;
        } catch (Exception e) {
            log.error("Ошибка при обработке элемента остатка", e);
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
            if (org.springframework.util.StringUtils.hasText(levelStr)) {
                category.setLevel(Integer.parseInt(levelStr));
            }

            String sortOrderStr = reader.getAttributeValue(null, "sort_order");
            if (org.springframework.util.StringUtils.hasText(sortOrderStr)) {
                category.setSortOrder(Integer.parseInt(sortOrderStr));
            }

            Set<Product> products = new java.util.HashSet<>();
            Set<String> productsOnPage = new java.util.HashSet<>();

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamReader.END_ELEMENT && "category".equals(reader.getLocalName())) {
                    break;
                }
                if (event == XMLStreamReader.START_ELEMENT) {
                    String elementName = reader.getLocalName();
                    switch (elementName) {
                        case "name":
                            category.setName(readElementAsXml(reader));
                            break;
                        case "description":
                            category.setDescription(readElementAsXml(reader));
                            break;
                        case "uri":
                            category.setUri(reader.getElementText());
                            break;
                        case "image":
                            category.setImage(reader.getElementText());
                            break;
                        case "product":
                            String productId = reader.getAttributeValue(null, "id");
                            if (org.springframework.util.StringUtils.hasText(productId)) {
                                productRepository.findByProductId(productId)
                                    .ifPresentOrElse(products::add,
                                        () -> log.warn("Продукт с id {} не найден для категории {}", productId, category.getCategoryId()));
                            }
                            skipElement(reader, "product");
                            break;
                        case "productsOnPage":
                            while (reader.hasNext()) {
                                int prodEvent = reader.next();
                                if (prodEvent == XMLStreamReader.END_ELEMENT && "productsOnPage".equals(reader.getLocalName())) break;
                                if (prodEvent == XMLStreamReader.START_ELEMENT && "product".equals(reader.getLocalName())) {
                                    String prodId = reader.getAttributeValue(null, "id");
                                    if (org.springframework.util.StringUtils.hasText(prodId)) {
                                        productsOnPage.add(prodId);
                                    }
                                    skipElement(reader, "product");
                                }
                            }
                            break;
                        case "children":
                            while (reader.hasNext()) {
                                int childEvent = reader.next();
                                if (childEvent == XMLStreamReader.END_ELEMENT && "children".equals(reader.getLocalName())) break;
                                if (childEvent == XMLStreamReader.START_ELEMENT && "category".equals(reader.getLocalName())) {
                                    Category child = processCategoryElement(reader);
                                    if (child != null) {
                                        child.setParentId(category.getCategoryId());
                                    }
                                }
                            }
                            break;
                    }
                }
            }
            category.setProducts(products);
            category.setProductsOnPage(productsOnPage);
            return validateCategory(category) ? category : null;
        } catch (Exception e) {
            log.error("Ошибка при обработке элемента категории", e);
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
            if (org.springframework.util.StringUtils.hasText(sortOrderStr)) {
                filter.setSortOrder(Integer.parseInt(sortOrderStr));
            }

            Set<Product> products = new java.util.HashSet<>();
            Set<Filter> children = new java.util.HashSet<>();

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamReader.END_ELEMENT && "filter".equals(reader.getLocalName())) {
                    break;
                }
                if (event == XMLStreamReader.START_ELEMENT) {
                    String elementName = reader.getLocalName();
                    switch (elementName) {
                        case "name":
                            filter.setName(readElementAsXml(reader));
                            break;
                        case "filter_type_name":
                            filter.setFilterTypeName(readElementAsXml(reader));
                            break;
                        case "filter_name":
                            filter.setFilterName(readElementAsXml(reader));
                            break;
                        case "product":
                            String productId = reader.getAttributeValue(null, "id");
                            if (org.springframework.util.StringUtils.hasText(productId)) {
                                productRepository.findByProductId(productId)
                                    .ifPresentOrElse(products::add,
                                        () -> log.warn("Продукт с id {} не найден для фильтра {}", productId, filter.getFilterId()));
                            }
                            skipElement(reader, "product");
                            break;
                        case "filter":
                            Filter child = processFilterElement(reader);
                            if (child != null) {
                                children.add(child);
                            }
                            break;
                    }
                }
            }
            filter.setProducts(products);
            filter.setFilters(children);
            return validateFilter(filter) ? filter : null;
        } catch (Exception e) {
            log.error("Ошибка при обработке элемента фильтра", e);
            return null;
        }
    }

    private Complect processComplectElement(XMLStreamReader reader) {
        try {
            Complect complect = new Complect();
            complect.setLastUpdated(LocalDateTime.now());
            complect.setIsActive(true);
            complect.setComplectId(reader.getAttributeValue(null, "id"));
            Set<Complect.ComplectPart> parts = new HashSet<>();
            Set<Product> products = new HashSet<>();
            Map<Product, Integer> productQuantities = new HashMap<>();
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamReader.END_ELEMENT && "complect".equals(reader.getLocalName())) break;
                if (event == XMLStreamReader.START_ELEMENT) {
                    String el = reader.getLocalName();
                    switch (el) {
                        case "name": complect.setName(readElementAsXml(reader)); break;
                        case "description": complect.setDescription(readElementAsXml(reader)); break;
                        case "tocomplect": complect.setTocomplect(Boolean.valueOf(reader.getElementText())); break;
                        case "complectprice": complect.setComplectprice(new java.math.BigDecimal(reader.getElementText())); break;
                        case "parts":
                            while (reader.hasNext()) {
                                int partEvent = reader.next();
                                if (partEvent == XMLStreamReader.END_ELEMENT && "parts".equals(reader.getLocalName())) break;
                                if (partEvent == XMLStreamReader.START_ELEMENT && "part".equals(reader.getLocalName())) {
                                    Complect.ComplectPart part = new Complect.ComplectPart();
                                    part.setPartId(reader.getAttributeValue(null, "id"));
                                    String publishedStr = reader.getAttributeValue(null, "published");
                                    part.setPublished(publishedStr != null && publishedStr.equalsIgnoreCase("true"));
                                    String partProductId = null;
                                    while (reader.hasNext()) {
                                        int fieldEvent = reader.next();
                                        if (fieldEvent == XMLStreamReader.END_ELEMENT && "part".equals(reader.getLocalName())) break;
                                        if (fieldEvent == XMLStreamReader.START_ELEMENT) {
                                            String partEl = reader.getLocalName();
                                            switch (partEl) {
                                                case "product_id":
                                                    part.setProductId(reader.getElementText());
                                                    partProductId = part.getProductId();
                                                    break;
                                                case "code":
                                                    part.setCode(reader.getElementText());
                                                    break;
                                                case "name":
                                                    part.setName(readElementAsXml(reader));
                                                    break;
                                                case "small_image":
                                                    part.setSmallImage(reader.getElementText());
                                                    break;
                                                case "super_big_image":
                                                    part.setSuperBigImage(reader.getElementText());
                                                    break;
                                                case "print":
                                                    while (reader.hasNext()) {
                                                        int printEvent = reader.next();
                                                        if (printEvent == XMLStreamReader.END_ELEMENT && "print".equals(reader.getLocalName())) break;
                                                        if (printEvent == XMLStreamReader.START_ELEMENT) {
                                                            String printEl = reader.getLocalName();
                                                            switch (printEl) {
                                                                case "name":
                                                                    part.setPrintName(readElementAsXml(reader));
                                                                    break;
                                                                case "description":
                                                                    part.setPrintDescription(readElementAsXml(reader));
                                                                    break;
                                                            }
                                                        }
                                                    }
                                                    break;
                                            }
                                        }
                                    }
                                    if (part.getPublished() != null && part.getPublished() && partProductId != null) {
                                        // published=true: связываем с продуктом
                                        Product product = productRepository.findByProductId(partProductId).orElse(null);
                                        if (product != null) {
                                            part.setProduct(product);
                                            products.add(product);
                                        } else {
                                            log.warn("Продукт с id {} не найден для part {} в комплекте {}", partProductId, part.getPartId(), complect.getComplectId());
                                        }
                                    }
                                    parts.add(part);
                                }
                            }
                            complect.setParts(parts);
                            complect.setProducts(products);
                            break;
                        case "products":
                            while (reader.hasNext()) {
                                int prodEvent = reader.next();
                                if (prodEvent == XMLStreamReader.END_ELEMENT && "products".equals(reader.getLocalName())) break;
                                if (prodEvent == XMLStreamReader.START_ELEMENT && "product".equals(reader.getLocalName())) {
                                    String prodId = reader.getAttributeValue(null, "id");
                                    String qtyStr = reader.getAttributeValue(null, "quantity");
                                    if (prodId != null) {
                                        productRepository.findByProductId(prodId).ifPresentOrElse(product -> {
                                            products.add(product);
                                            if (qtyStr != null) {
                                                try {
                                                    productQuantities.put(product, Integer.parseInt(qtyStr));
                                                } catch (Exception e) {
                                                    log.warn("Ошибка при парсинге количества продукта {} в комплекте {}: {}", prodId, complect.getComplectId(), qtyStr);
                                                }
                                            }
                                        }, () -> log.warn("Продукт с id {} не найден для комплекта {}", prodId, complect.getComplectId()));
                                    }
                                    skipElement(reader, "product");
                                }
                            }
                            complect.setProducts(products);
                            complect.setProductQuantities(productQuantities);
                            break;
                    }
                }
            }
            return validateComplect(complect) ? complect : null;
        } catch (Exception e) {
            log.error("Ошибка при обработке элемента комплектов", e);
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
                StringUtils.hasText(complect.getName());
    }

    private Integer parseIntSafe(String value) {
        try {
            return value != null && !value.isEmpty() ? Integer.valueOf(value) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void skipElement(XMLStreamReader reader, String elementName) throws Exception {
        int depth = 1;
        while (reader.hasNext() && depth > 0) {
            int event = reader.next();
            if (event == XMLStreamReader.START_ELEMENT && elementName.equals(reader.getLocalName())) depth++;
            if (event == XMLStreamReader.END_ELEMENT && elementName.equals(reader.getLocalName())) depth--;
        }
    }

    // Универсальный метод для чтения содержимого элемента с вложенными тегами
    private String readElementAsXml(XMLStreamReader reader) throws XMLStreamException {
        StringWriter writer = new StringWriter();
        int depth = 0;
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                writer.append("<").append(reader.getLocalName()).append(">");
                depth++;
            } else if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) {
                writer.append(reader.getText());
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                writer.append("</").append(reader.getLocalName()).append(">");
                if (depth == 0) break;
                depth--;
            }
        }
        return writer.toString();
    }

    /**
     * Парсит и сохраняет только первые n продуктов из XML-файла
     */
    @Transactional
    public void processFirstNProductsXml(File xmlFile, int n) {
        try (FileInputStream fis = new FileInputStream(xmlFile)) {
            XMLStreamReader reader = XML_INPUT_FACTORY.createXMLStreamReader(fis);
            List<Product> products = new ArrayList<>();
            int count = 0;
            while (reader.hasNext() && count < n) {
                int event = reader.next();
                if (event == XMLStreamReader.START_ELEMENT && "product".equals(reader.getLocalName())) {
                    Product product = new Product();
                    product.setLastUpdated(LocalDateTime.now());
                    product.setIsActive(true);
                    while (reader.hasNext()) {
                        int innerEvent = reader.next();
                        if (innerEvent == XMLStreamReader.END_ELEMENT && "product".equals(reader.getLocalName())) break;
                        if (innerEvent == XMLStreamReader.START_ELEMENT) {
                            String el = reader.getLocalName();
                            switch (el) {
                                case "product_id": product.setProductId(reader.getElementText()); break;
                                case "group": product.setGroup(readElementAsXml(reader)); break;
                                case "code": product.setCode(reader.getElementText()); break;
                                case "name": product.setName(readElementAsXml(reader)); break;
                                case "product_size": product.setProductSize(readElementAsXml(reader)); break;
                                case "matherial": product.setMatherial(readElementAsXml(reader)); break;
                                case "alert": product.setAlert(readElementAsXml(reader)); break;
                                case "small_image": product.setSmallImage(reader.getElementText()); break;
                                case "super_big_image": product.setSuperBigImage(reader.getElementText()); break;
                                case "content": product.setContent(readElementAsXml(reader)); break;
                                case "status": product.setStatusId(Integer.valueOf(reader.getAttributeValue(null, "id")));
                                    product.setStatusName(readElementAsXml(reader)); break;
                                case "brand": product.setBrand(readElementAsXml(reader)); break;
                                case "barcode": product.setBarcode(reader.getElementText()); break;
                                case "weight": product.setWeight(parseIntSafe(reader.getElementText())); break;
                                case "volume": product.setVolume(parseIntSafe(reader.getElementText())); break;
                                case "pack":
                                    Product.Pack pack = new Product.Pack();
                                    while (reader.hasNext()) {
                                        int packEvent = reader.next();
                                        if (packEvent == XMLStreamReader.END_ELEMENT && "pack".equals(reader.getLocalName())) break;
                                        if (packEvent == XMLStreamReader.START_ELEMENT) {
                                            String packEl = reader.getLocalName();
                                            String packText = reader.getElementText();
                                            switch (packEl) {
                                                case "amount": pack.setAmount(parseIntSafe(packText)); break;
                                                case "weight": pack.setWeight(parseIntSafe(packText)); break;
                                                case "volume": pack.setVolume(parseIntSafe(packText)); break;
                                                case "sizex": pack.setSizex(parseIntSafe(packText)); break;
                                                case "sizey": pack.setSizey(parseIntSafe(packText)); break;
                                                case "sizez": pack.setSizez(parseIntSafe(packText)); break;
                                                case "minpackamount": pack.setMinpackamount(parseIntSafe(packText)); break;
                                            }
                                        }
                                    }
                                    product.setPack(pack);
                                    break;
                                case "print":
                                    Product.Print print = new Product.Print();
                                    while (reader.hasNext()) {
                                        int printEvent = reader.next();
                                        if (printEvent == XMLStreamReader.END_ELEMENT && "print".equals(reader.getLocalName())) break;
                                        if (printEvent == XMLStreamReader.START_ELEMENT) {
                                            String printEl = reader.getLocalName();
                                            switch (printEl) {
                                                case "name": print.setName(readElementAsXml(reader)); break;
                                                case "description": print.setDescription(readElementAsXml(reader)); break;
                                            }
                                        }
                                    }
                                    product.setPrint(print);
                                    break;
                                case "attachments":
                                case "product_attachment":
                                    Set<Product.ProductAttachment> attachments = new HashSet<>();
                                    while (reader.hasNext()) {
                                        int attEvent = reader.next();
                                        if (attEvent == XMLStreamReader.END_ELEMENT && ("attachments".equals(reader.getLocalName()) || "product_attachment".equals(reader.getLocalName()))) break;
                                        if (attEvent == XMLStreamReader.START_ELEMENT && "attachment".equals(reader.getLocalName())) {
                                            Product.ProductAttachment attachment = new Product.ProductAttachment();
                                            while (reader.hasNext()) {
                                                int fieldEvent = reader.next();
                                                if (fieldEvent == XMLStreamReader.END_ELEMENT && "attachment".equals(reader.getLocalName())) break;
                                                if (fieldEvent == XMLStreamReader.START_ELEMENT) {
                                                    String attEl = reader.getLocalName();
                                                    String attText = reader.getElementText();
                                                    switch (attEl) {
                                                        case "meaning": attachment.setMeaning(readElementAsXml(reader)); break;
                                                        case "file": attachment.setFile(attText); break;
                                                        case "image": attachment.setImage(attText); break;
                                                        case "name": attachment.setName(readElementAsXml(reader)); break;
                                                        case "description": attachment.setDescription(readElementAsXml(reader)); break;
                                                    }
                                                }
                                            }
                                            attachments.add(attachment);
                                        }
                                    }
                                    product.setAttachments(attachments);
                                    break;
                                case "subproducts":
                                    Set<String> subproducts = new HashSet<>();
                                    Set<Product> subproductEntities = new HashSet<>();
                                    while (reader.hasNext()) {
                                        int subEvent = reader.next();
                                        if (subEvent == XMLStreamReader.END_ELEMENT && "subproducts".equals(reader.getLocalName())) break;
                                        if (subEvent == XMLStreamReader.START_ELEMENT && "subproduct".equals(reader.getLocalName())) {
                                            try {
                                                String subId = reader.getAttributeValue(null, "product_id");
                                                if (subId != null && !subId.isEmpty()) {
                                                    subproducts.add(subId);
                                                    productRepository.findByProductId(subId).ifPresent(subproductEntities::add);
                                                }
                                            } catch (Exception e) {
                                                log.warn("Ошибка при парсинге subproduct: {}", e.getMessage());
                                            }
                                            skipElement(reader, "subproduct");
                                        }
                                    }
                                    product.setSubproducts(subproducts);
                                    product.setSubproductEntities(subproductEntities);
                                    break;
                                case "filters":
                                    Set<Filter> productFilters = new HashSet<>();
                                    while (reader.hasNext()) {
                                        int filterEvent = reader.next();
                                        if (filterEvent == XMLStreamReader.END_ELEMENT && "filters".equals(reader.getLocalName())) break;
                                        if (filterEvent == XMLStreamReader.START_ELEMENT && "filter".equals(reader.getLocalName())) {
                                            String filterId = reader.getAttributeValue(null, "id");
                                            if (filterId != null) {
                                                Filter filter = filterRepository.findByFilterId(filterId);
                                                if (filter != null) {
                                                    productFilters.add(filter);
                                                } else {
                                                    log.warn("Фильтр с id {} не найден для продукта {}", filterId, product.getProductId());
                                                }
                                            }
                                            // пропустить содержимое filter
                                            skipElement(reader, "filter");
                                        }
                                    }
                                    product.setFilters(productFilters);
                                    break;
                                case "ondemand": product.setOndemand(Boolean.valueOf(reader.getElementText())); break;
                                case "moq": product.setMoq(readElementAsXml(reader)); break;
                                case "days": product.setDays(readElementAsXml(reader)); break;
                                case "demandtype": product.setDemandtype(readElementAsXml(reader)); break;
                                case "multiplicity": product.setMultiplicity(parseIntSafe(reader.getElementText())); break;
                                case "price":
                                    Set<Product.Price> prices = new HashSet<>();
                                    while (reader.hasNext()) {
                                        int priceEvent = reader.next();
                                        if (priceEvent == XMLStreamReader.END_ELEMENT && "price".equals(reader.getLocalName())) break;
                                        if (priceEvent == XMLStreamReader.START_ELEMENT && "item".equals(reader.getLocalName())) {
                                            Product.Price price = new Product.Price();
                                            while (reader.hasNext()) {
                                                int fieldEvent = reader.next();
                                                if (fieldEvent == XMLStreamReader.END_ELEMENT && "item".equals(reader.getLocalName())) break;
                                                if (fieldEvent == XMLStreamReader.START_ELEMENT) {
                                                    String priceEl = reader.getLocalName();
                                                    String priceText = reader.getElementText();
                                                    try {
                                                        switch (priceEl) {
                                                            case "value": price.setValue(new java.math.BigDecimal(priceText)); break;
                                                            case "type": price.setType(priceText); break;
                                                            case "currency": price.setCurrency(priceText); break;
                                                            case "dateStart": price.setDateStart(priceText); break;
                                                            case "dateEnd": price.setDateEnd(priceText); break;
                                                        }
                                                    } catch (Exception e) {
                                                        log.warn("Ошибка при парсинге цены {}: {}", priceEl, priceText);
                                                    }
                                                }
                                            }
                                            prices.add(price);
                                        }
                                    }
                                    product.setPrices(prices);
                                    break;
                                case "currency":
                                    Set<Product.Currency> currencies = new HashSet<>();
                                    while (reader.hasNext()) {
                                        int currEvent = reader.next();
                                        if (currEvent == XMLStreamReader.END_ELEMENT && "currency".equals(reader.getLocalName())) break;
                                        if (currEvent == XMLStreamReader.START_ELEMENT && "item".equals(reader.getLocalName())) {
                                            Product.Currency currency = new Product.Currency();
                                            while (reader.hasNext()) {
                                                int fieldEvent = reader.next();
                                                if (fieldEvent == XMLStreamReader.END_ELEMENT && "item".equals(reader.getLocalName())) break;
                                                if (fieldEvent == XMLStreamReader.START_ELEMENT) {
                                                    String currEl = reader.getLocalName();
                                                    String currText = reader.getElementText();
                                                    try {
                                                        switch (currEl) {
                                                            case "code": currency.setCode(currText); break;
                                                            case "rate": currency.setRate(new java.math.BigDecimal(currText)); break;
                                                            case "date": currency.setDate(currText); break;
                                                        }
                                                    } catch (Exception e) {
                                                        log.warn("Ошибка при парсинге валюты {}: {}", currEl, currText);
                                                    }
                                                }
                                            }
                                            currencies.add(currency);
                                        }
                                    }
                                    product.setCurrencies(currencies);
                                    break;
                                case "alerts":
                                    Set<String> alerts = new HashSet<>();
                                    while (reader.hasNext()) {
                                        int alertEvent = reader.next();
                                        if (alertEvent == XMLStreamReader.END_ELEMENT && "alerts".equals(reader.getLocalName())) break;
                                        if (alertEvent == XMLStreamReader.START_ELEMENT && "alert".equals(reader.getLocalName())) {
                                            try {
                                                String alertText = readElementAsXml(reader);
                                                if (alertText != null && !alertText.isEmpty()) {
                                                    alerts.add(alertText);
                                                }
                                            } catch (Exception e) {
                                                log.warn("Ошибка при парсинге alert: {}", e.getMessage());
                                            }
                                        }
                                    }
                                    product.setAlerts(alerts);
                                    break;
                            }
                        }
                    }
                    if (product.getName() == null || product.getName().trim().isEmpty()) {
                        log.warn("Продукт без имени пропущен: {}", product.getProductId());
                        continue;
                    }
                    products.add(product);
                    count++;
                }
            }
            productRepository.saveAll(products);
        } catch (Exception e) {
            log.error("Ошибка при обработке первых {} товаров из файла: {}", n, xmlFile.getName(), e);
            throw new RuntimeException("Не удалось обработать файл товаров: " + xmlFile.getName(), e);
        }
    }
}
