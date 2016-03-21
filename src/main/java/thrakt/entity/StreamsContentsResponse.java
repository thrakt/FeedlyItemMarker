package thrakt.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StreamsContentsResponse {

    private List<FeedlyItem> items;

    public List<FeedlyItem> getItems() {
        return items;
    }

    public void setItems(List<FeedlyItem> items) {
        this.items = items;
    }

}
