package anto.services;

import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.io.IOException;
import java.util.List;
import anto.exception.FailedScrappingException;
import anto.model.Product;
import anto.scrapper.HandphoneScrapper;
import anto.scrapper.HandphoneScrapper.Category;

public class HandphoneScrapperService {

    private static final String UNDERSCORE = "_";
    private static final String PRODUCT = "Product";
    private static final String CSV_EXT = ".csv";

    private HandphoneScrapper scrapper;

    public HandphoneScrapperService() {
        scrapper = new HandphoneScrapper();
    }

    @VisibleForTesting
    HandphoneScrapperService(HandphoneScrapper scrapper) {
        this.scrapper = scrapper;
    }

    public String downloadProductListCsv(Category category, int count)
            throws FailedScrappingException {
        String filename = PRODUCT + UNDERSCORE + category.getName()
                + UNDERSCORE + System.currentTimeMillis() + CSV_EXT;
        List<Product> products = scrapper.extractProductList(category, count);

        CsvMapper csvMapper = new CsvMapper();
        csvMapper.enable(Feature.IGNORE_UNKNOWN);
        csvMapper.addMixIn(Product.class, Product.ProductFormat.class);
        CsvSchema schema = csvMapper.schemaFor(Product.class).withHeader();

        try {
            File file = new File(filename);
            csvMapper.writer(schema).writeValue(file, products);
            return filename;
        } catch (IOException | RuntimeException e) {
            throw new FailedScrappingException(e.getMessage());
        }
    }
}
