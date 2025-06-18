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
                                case "group": product.setGroup(reader.getElementText()); break;
                                case "code": product.setCode(reader.getElementText()); break;
                                case "name": product.setName(reader.getElementText()); break;
                                case "product_size": product.setProductSize(reader.getElementText()); break;
                                case "matherial": product.setMatherial(reader.getElementText()); break;
                                case "alert": product.setAlert(reader.getElementText()); break;
                                case "small_image": product.setSmallImage(reader.getElementText()); break;
                                case "super_big_image": product.setSuperBigImage(reader.getElementText()); break;
                                case "content": product.setContent(reader.getElementText()); break;
                                case "status": product.setStatusId(Integer.valueOf(reader.getAttributeValue(null, "id")));
                                    product.setStatusName(reader.getElementText()); break;
                                case "brand": product.setBrand(reader.getElementText()); break;
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
                                            try {
                                                switch (packEl) {
                                                    case "amount": pack.setAmount(parseIntSafe(packText)); break;
                                                    case "weight": pack.setWeight(parseIntSafe(packText)); break;
                                                    case "volume": pack.setVolume(parseIntSafe(packText)); break;
                                                    case "sizex": pack.setSizex(parseIntSafe(packText)); break;
                                                    case "sizey": pack.setSizey(parseIntSafe(packText)); break;
                                                    case "sizez": pack.setSizez(parseIntSafe(packText)); break;
                                                    case "minpackamount": pack.setMinpackamount(parseIntSafe(packText)); break;
                                                }
                                            } catch (Exception e) {
                                                log.warn("Ошибка при парсинге поля упаковки {}: {}", packEl, packText);
                                            }
                                        }
                                    }
                                    product.setPack(pack);
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
                                case "moq": product.setMoq(reader.getElementText()); break;
                                case "days": product.setDays(reader.getElementText()); break;
                                case "demandtype": product.setDemandtype(reader.getElementText()); break;
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
                                case "print":
                                    Product.Print print = new Product.Print();
                                    while (reader.hasNext()) {
                                        int printEvent = reader.next();
                                        if (printEvent == XMLStreamReader.END_ELEMENT && "print".equals(reader.getLocalName())) break;
                                        if (printEvent == XMLStreamReader.START_ELEMENT) {
                                            String printEl = reader.getLocalName();
                                            String printText = reader.getElementText();
                                            try {
                                                switch (printEl) {
                                                    case "name": print.setName(printText); break;
                                                    case "description": print.setDescription(printText); break;
                                                }
                                            } catch (Exception e) {
                                                log.warn("Ошибка при парсинге блока print {}: {}", printEl, printText);
                                            }
                                        }
                                    }
                                    product.setPrint(print);
                                    break;
                                case "product_attachment":
                                    Set<Product.ProductAttachment> attachments = new HashSet<>();
                                    while (reader.hasNext()) {
                                        int attEvent = reader.next();
                                        if (attEvent == XMLStreamReader.END_ELEMENT && "product_attachment".equals(reader.getLocalName())) break;
                                        if (attEvent == XMLStreamReader.START_ELEMENT && "attachment".equals(reader.getLocalName())) {
                                            Product.ProductAttachment attachment = new Product.ProductAttachment();
                                            while (reader.hasNext()) {
                                                int fieldEvent = reader.next();
                                                if (fieldEvent == XMLStreamReader.END_ELEMENT && "attachment".equals(reader.getLocalName())) break;
                                                if (fieldEvent == XMLStreamReader.START_ELEMENT) {
                                                    String attEl = reader.getLocalName();
                                                    String attText = reader.getElementText();
                                                    try {
                                                        switch (attEl) {
                                                            case "meaning": attachment.setMeaning(attText); break;
                                                            case "file": attachment.setFile(attText); break;
                                                            case "image": attachment.setImage(attText); break;
                                                            case "name": attachment.setName(attText); break;
                                                        }
                                                    } catch (Exception e) {
                                                        log.warn("Ошибка при парсинге вложения {}: {}", attEl, attText);
                                                    }
                                                }
                                            }
                                            attachments.add(attachment);
                                        }
                                    }
                                    product.setAttachments(attachments);
                                    break;
                                case "alerts":
                                    Set<String> alerts = new HashSet<>();
                                    while (reader.hasNext()) {
                                        int alertEvent = reader.next();
                                        if (alertEvent == XMLStreamReader.END_ELEMENT && "alerts".equals(reader.getLocalName())) break;
                                        if (alertEvent == XMLStreamReader.START_ELEMENT && "alert".equals(reader.getLocalName())) {
                                            try {
                                                String alertText = reader.getElementText();
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
                                case "subproducts":
                                    Set<String> subproducts = new HashSet<>();
                                    while (reader.hasNext()) {
                                        int subEvent = reader.next();
                                        if (subEvent == XMLStreamReader.END_ELEMENT && "subproducts".equals(reader.getLocalName())) break;
                                        if (subEvent == XMLStreamReader.START_ELEMENT && "subproduct".equals(reader.getLocalName())) {
                                            try {
                                                String subId = reader.getAttributeValue(null, "product_id");
                                                if (subId != null && !subId.isEmpty()) {
                                                    subproducts.add(subId);
                                                }
                                            } catch (Exception e) {
                                                log.warn("Ошибка при парсинге subproduct: {}", e.getMessage());
                                            }
                                            skipElement(reader, "subproduct");
                                        }
                                    }
                                    product.setSubproducts(subproducts);
                                    break;
                            }
                        }
                    }
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
            String quantityStr = reader.getAttributeValue(null, "quantity");
            String priceStr = reader.getAttributeValue(null, "price");
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
                        .orElse(null);
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
                    switch (elementName) {
                        case "name":
                            category.setName(reader.getElementText());
                            break;
                        case "description":
                            category.setDescription(reader.getElementText());
                            break;
                        case "uri":
                            category.setUri(reader.getElementText());
                            break;
                        case "image":
                            category.setImage(reader.getElementText());
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
                            skipElement(reader, "product");
                            break;
                        case "productsOnPage":
                            Set<String> productsOnPage = new HashSet<>();
                            while (reader.hasNext()) {
                                int prodEvent = reader.next();
                                if (prodEvent == XMLStreamReader.END_ELEMENT && "productsOnPage".equals(reader.getLocalName())) break;
                                if (prodEvent == XMLStreamReader.START_ELEMENT && "product".equals(reader.getLocalName())) {
                                    String prodId = reader.getAttributeValue(null, "id");
                                    if (StringUtils.hasText(prodId)) {
                                        productsOnPage.add(prodId);
                                    }
                                    skipElement(reader, "product");
                                }
                            }
                            category.setProductsOnPage(productsOnPage);
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
                    switch (elementName) {
                        case "name":
                            filter.setName(reader.getElementText());
                            break;
                        case "filter_type_name":
                            filter.setFilterTypeName(reader.getElementText());
                            break;
                        case "filter_name":
                            filter.setFilterName(reader.getElementText());
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
                            skipElement(reader, "product");
                            break;
                        case "children":
                            Set<Filter> children = new HashSet<>();
                            while (reader.hasNext()) {
                                int childEvent = reader.next();
                                if (childEvent == XMLStreamReader.END_ELEMENT && "children".equals(reader.getLocalName())) break;
                                if (childEvent == XMLStreamReader.START_ELEMENT && "filter".equals(reader.getLocalName())) {
                                    Filter child = processFilterElement(reader);
                                    if (child != null) {
                                        children.add(child);
                                    }
                                }
                            }
                            filter.setFilters(children);
                            break;
                    }
                }
            }

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
            // Основные поля
            while (reader.hasNext()) {
                int innerEvent = reader.next();
                if (innerEvent == XMLStreamReader.END_ELEMENT && "complect".equals(reader.getLocalName())) break;
                if (innerEvent == XMLStreamReader.START_ELEMENT) {
                    String el = reader.getLocalName();
                    String text = reader.getElementText();
                    switch (el) {
                        case "id": complect.setComplectId(text); break;
                        case "tocomplect": complect.setTocomplect(Boolean.valueOf(text)); break;
                        case "complectprice": complect.setComplectprice(new java.math.BigDecimal(text)); break;
                        case "name": complect.setName(text); break;
                        case "description": complect.setDescription(text); break;
                        case "parts":
                            Set<Complect.ComplectPart> parts = new HashSet<>();
                            while (reader.hasNext()) {
                                int partEvent = reader.next();
                                if (partEvent == XMLStreamReader.END_ELEMENT && "parts".equals(reader.getLocalName())) break;
                                if (partEvent == XMLStreamReader.START_ELEMENT && "part".equals(reader.getLocalName())) {
                                    Complect.ComplectPart part = new Complect.ComplectPart();
                                    part.setPublished(Boolean.valueOf(reader.getAttributeValue(null, "published")));
                                    while (reader.hasNext()) {
                                        int fieldEvent = reader.next();
                                        if (fieldEvent == XMLStreamReader.END_ELEMENT && "part".equals(reader.getLocalName())) break;
                                        if (fieldEvent == XMLStreamReader.START_ELEMENT) {
                                            String partEl = reader.getLocalName();
                                            String partText = reader.getElementText();
                                            switch (partEl) {
                                                case "id": part.setPartId(partText); break;
                                                case "product_id": part.setProductId(partText); break;
                                                case "code": part.setCode(partText); break;
                                                case "name": part.setName(partText); break;
                                                case "small_image": part.setSmallImage(partText); break;
                                                case "super_big_image": part.setSuperBigImage(partText); break;
                                                case "print":
                                                    while (reader.hasNext()) {
                                                        int printEvent = reader.next();
                                                        if (printEvent == XMLStreamReader.END_ELEMENT && "print".equals(reader.getLocalName())) break;
                                                        if (printEvent == XMLStreamReader.START_ELEMENT) {
                                                            String printEl = reader.getLocalName();
                                                            String printText = reader.getElementText();
                                                            if ("name".equals(printEl)) part.setPrintName(printText);
                                                            if ("description".equals(printEl)) part.setPrintDescription(printText);
                                                        }
                                                    }
                                                    break;
                                            }
                                        }
                                    }
                                    parts.add(part);
                                }
                            }
                            complect.setParts(parts);
                            break;
                        case "products":
                            Set<Product> products = new HashSet<>();
                            Map<Product, Integer> productQuantities = new HashMap<>();
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
}
