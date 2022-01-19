

import java.util.logging.Level;
import anto.exception.FailedScrappingException;
import anto.scrapper.HandphoneScrapper.Category;
import anto.services.HandphoneScrapperService;
import org.apache.commons.lang3.math.NumberUtils;


public class Main {
    private static final String HELP = "Usage:\n"
            + "\tprogram.jar <category> [count]: extract <count> <category> product(s) to CSV. If not specified, count is 100.\n";

    public static void main(String[] args) {
        java.util.logging.Logger.getLogger("org.openqa").setLevel(Level.OFF);
        if (args.length > 0) {
            int count = 100;
            Category category = extract(args[0]);

            if (category != null) {
                if (args.length > 1 && NumberUtils.isCreatable(args[1])) {
                    count = NumberUtils.createInteger(args[1]);
                }
                System.out.println("Extracting " + count + " " + category + " product(s)...");
                HandphoneScrapperService service = new HandphoneScrapperService();
                try {
                    String csv = service.downloadProductListCsv(category, count);
                    System.out.println("Extraction is successfully saved to " + csv);
                } catch (FailedScrappingException e) {
                    System.err.println("Failed. Please try again.");
                }
            } else {
                System.err.println("Missing or invalid category.\n" + HELP);
            }
        } else {
            System.err.println(HELP);
        }
    }

    private static Category extract(String category) {
        switch (category.toLowerCase()) {
            case "handphone":
                return Category.HANDPHONE;
            default:
                return null;
        }
    }
}