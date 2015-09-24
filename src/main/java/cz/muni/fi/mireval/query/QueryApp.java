/*
 * ===========================================================================
 *  IBA CZ Confidential
 * 
 *  (c) Copyright IBA CZ 2014 ALL RIGHTS RESERVED
 *  The source code for this program is not published or otherwise
 *  divested of its trade secrets.
 * 
 * ===========================================================================
 */

package cz.muni.fi.mireval.query;

import cz.muni.fi.mireval.Settings;
import cz.muni.fi.mireval.query.topics.NTCIRTopicLoader;
import cz.muni.fi.mireval.query.topics.Topic;
import cz.muni.fi.mireval.query.topics.TopicLoader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Martin Liska <martin.liska@ibacz.eu>
 */
public class QueryApp {
    
    public static void main( String[] args) {
        TopicLoader topicLoader = new NTCIRTopicLoader();
        List<Topic> topics = topicLoader.loadTopics(Settings.getTopicsFile());
        
        String date = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss").format(new Date());
        Queryer queryer = new MIaSQueryer();
        queryer.performQueries(topics, Settings.getOutputDir(), "MIRMU_CMath_" + date);
    }
    
}
