package cz.muni.fi.mireval.query.topics;

import java.util.List;

public interface TopicLoader {
    
    public List<Topic> loadTopics(String topicsFilePath);
    
}
