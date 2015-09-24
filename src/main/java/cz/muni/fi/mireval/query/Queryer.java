package cz.muni.fi.mireval.query;

import cz.muni.fi.mireval.query.topics.Topic;
import java.util.List;

public interface Queryer {
    
    public void performQueries(List<Topic> topics, String outputFile, String runName);
    
}
