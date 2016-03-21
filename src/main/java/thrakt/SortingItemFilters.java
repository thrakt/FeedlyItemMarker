package thrakt;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import thrakt.entity.FeedlyItem;

public class SortingItemFilters {

    public static final Function<List<FeedlyItem>, List<FeedlyItem>> duplicateUrlFilter = items -> {
        // create first published map
        Map<String, Optional<FeedlyItem>> urlMap = items.stream().collect(
                Collectors.groupingBy(FeedlyItem::getUrl, Collectors
                        .minBy(Comparator
                                .comparingLong(FeedlyItem::getPublished))));

        // get valid item id list
        List<String> validIdList = urlMap.values().stream()
                .filter(Optional::isPresent).map(Optional::get)
                .map(FeedlyItem::getId).collect(Collectors.toList());

        // get duplicate
        return items.stream().filter(i -> !validIdList.contains(i.getId()))
                .collect(Collectors.toList());
    };

    public static final Function<List<FeedlyItem>, List<FeedlyItem>> adsFilter = items -> items
            .stream()
            .filter(i -> i.getTitle().toUpperCase()
                    .matches("^((PR:)|(AD:)|(\\[PR\\])).*"))
            .collect(Collectors.toList());

}
