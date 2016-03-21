package thrakt.entity;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedlyItem {

    private String id;
    private String title;
    private Long published;
    private List<FeedlyItemAlternate> alternate;

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FeedlyItemAlternate {

        private String href;

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

    }

    public String getUrl() {
        return Optional.ofNullable(alternate).filter(a -> !a.isEmpty())
                .map(a -> a.get(0)).map(FeedlyItemAlternate::getHref)
                .orElse("");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getPublished() {
        return published;
    }

    public void setPublished(Long published) {
        this.published = published;
    }

    public List<FeedlyItemAlternate> getAlternate() {
        return alternate;
    }

    public void setAlternate(List<FeedlyItemAlternate> alternate) {
        this.alternate = alternate;
    }

}
