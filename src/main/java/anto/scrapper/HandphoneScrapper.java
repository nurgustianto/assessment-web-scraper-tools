package anto.scrapper;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import anto.exception.FailedScrappingException;
import anto.model.Product;
import anto.webdriver.DefaultWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class HandphoneScrapper {
    private static final String BASE_URL = "https://www.tokopedia.com";
    private static final String TOP_ADS_URL = "https://topads.tokopedia.com/promo";
    private static final String HANDPHONE_PATH = "/p/handphone-tablet/handphone";
    private static final String PAGE = "?page=";

    private static final String XPATH_PRODUCT_LIST = "//div[@data-testid='lstCL2ProductList']/div";
    private static final String XPATH_PRODUCT_LINK = "a[@data-testid='lnkProductContainer']";
    private static final String XPATH_FIRST_TIME_OVERLAY = "//div[@aria-label='unf-overlay']";
    private static final String XPATH_PRODUCT_NAME = "//h1[@data-testid='lblPDPDetailProductName']";
    private static final String XPATH_PRODUCT_DESCRIPTION = "//*[@data-testid='lblPDPDescriptionProduk']";
    private static final String XPATH_PRODUCT_IMG_LINK = "//*[@data-testid='PDPImageMain']//img";
    private static final String XPATH_PRODUCT_PRICE = "//*[@data-testid='lblPDPDetailProductPrice']";
    private static final String XPATH_PRODUCT_RATING = "//*[@data-testid='lblPDPDetailProductRatingNumber']";
    private static final String XPATH_MERCHANT_NAME = "//*[@data-testid='llbPDPFooterShopName']//h2";

    private static final String DOM_FIRST_TIME_OVERLAY = "div[aria-label=unf-overlay]";

    private static final String HREF = "href";
    private static final String SRC = "src";
    private static final String AMP = "&";
    private static final String PARAM_R = "r=";
    private static final String EMPTY = "";
    private static final String DOT = ".";
    private static final String RUPIAH_SIGN = "Rp";

    @AllArgsConstructor
    public enum Category {
        HANDPHONE;

        @Getter
        int category;

        public String getName() {
            switch (category) {
                case 24:
                    return "Handphone";
               
                default:
                    return EMPTY;
            }
        }
    }

    public List<Product> extractProductList(Category category, int count)
            throws FailedScrappingException {
        final DefaultWebDriver webDriver = new DefaultWebDriver();
        final List<Product> products = new ArrayList<>(count);
        final String baseUrl = getUrl(category);

        try {
            List<String> tabs = webDriver.prepareTwoTabs();
            for (int page = 1; products.size() < count; page++) {
                String url = baseUrl + PAGE + page;
                final List<WebElement> items = webDriver.getElementListByScrollingDown(url,
                        XPATH_PRODUCT_LIST, tabs.get(0)); // switch to main tab

                for (WebElement item : items) {
                    String path = item.findElement(By.xpath(XPATH_PRODUCT_LINK)).getAttribute(HREF);
                    if (isTopAdsLink(path)) {
                        path = extractTopAdsLink(path);
                    }

                    webDriver.getWebpage(path, tabs.get(1)); 

                    if (products.isEmpty()) {
                        webDriver.waitOnElement(XPATH_FIRST_TIME_OVERLAY);
                        webDriver.removeElement(DOM_FIRST_TIME_OVERLAY);
                    }

                    webDriver.scrollDownSmall();
                    webDriver.waitOnElement(XPATH_MERCHANT_NAME);

                    //products.add(extractProduct(webDriver, path));

                    if (products.size() == count) {
                        break;
                    }
                    webDriver.switchTab(tabs.get(0)); 
                }

            }
        } catch (Exception e) {
            throw new FailedScrappingException(e.getMessage());
        } finally {
            webDriver.quit();
        }

        return products;
    }
    
    /*
    private Product extractProduct(DefaultWebDriver webDriver, String path) {
        String name = webDriver.getText(XPATH_PRODUCT_NAME);
        String desc = webDriver.getText(XPATH_PRODUCT_DESCRIPTION);
        String imageLink = webDriver.getText(XPATH_PRODUCT_IMG_LINK, SRC);
        String price = webDriver.getText(XPATH_PRODUCT_PRICE).split(RUPIAH_SIGN)[1].replace(DOT, EMPTY);
        String merchant = webDriver.getText(XPATH_MERCHANT_NAME);
        String rating = webDriver.getText(XPATH_PRODUCT_RATING);
    
        return Product.builder()
                .name(name)
                .description(desc)
                .imageLink(imageLink)
                .merchant(merchant)
                .price(Double.parseDouble(price))
                .rating(rating == null || rating.isEmpty() ? 0.0 : Double.parseDouble(rating))
                .link(path)
                .build();
    }
	*/
    private String getUrl(Category category) {
        String url = "";

        switch (category) {
            case HANDPHONE:
                url = BASE_URL + HANDPHONE_PATH;
                break;
            default:
                break;
        }

        return url;
    }

    private boolean isTopAdsLink(String path) {
        return path.contains(TOP_ADS_URL);
    }

    private String extractTopAdsLink(String path) throws IOException {
        return URLDecoder.decode(path.substring(path.indexOf(PARAM_R) + 2).split(AMP)[0],
                StandardCharsets.UTF_8.name());
    }
}
