package thrakt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/")
public class AppController {

    @Autowired
    private FeedlyService feedlyService;

    @SuppressWarnings("unchecked")
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public String main() {
        return String.format("mark %d item.", feedlyService.execMarkEntities(
                SortingItemFilters.duplicateUrlFilter,
                SortingItemFilters.adsFilter));
    }

}
