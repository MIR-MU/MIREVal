package cz.muni.fi.mireval.query;

import cz.muni.fi.mireval.query.topics.Topic;
import java.util.List;

public interface Queryer {

    /**
     *
     * @param topics
     * @param outputFile
     * @param runName
     * @return created tsv file
     */
    public String performQueries(List<Topic> topics, String outputFile, String runName);
    
}
