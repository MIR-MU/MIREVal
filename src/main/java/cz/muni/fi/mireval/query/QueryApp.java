package cz.muni.fi.mireval.query;

import cz.muni.fi.mireval.EvalApp;
import cz.muni.fi.mireval.Settings;
import static cz.muni.fi.mireval.Settings.OPTION_OUTPUTDIR;
import static cz.muni.fi.mireval.Settings.OPTION_RUNNAME;
import static cz.muni.fi.mireval.Settings.OPTION_TOPICS;
import cz.muni.fi.mireval.query.topics.NTCIRTopicLoader;
import cz.muni.fi.mireval.query.topics.Topic;
import cz.muni.fi.mireval.query.topics.TopicLoader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author Martin Liska <martin.liska@ibacz.eu>
 */
public class QueryApp {
        
    public static void main( String[] args) {
        
        Options options = Settings.getQueryAppOptions();
              
        try {
            
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            
            TopicLoader topicLoader = new NTCIRTopicLoader();
            List<Topic> topics = topicLoader.loadTopics(cmd.getOptionValue(OPTION_TOPICS));
            
            String date = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss").format(new Date());
            Queryer queryer = new MIaSQueryer(cmd.getOptionValue(Settings.OPTION_QUERYURL));
            String tsvFile = queryer.performQueries(topics, cmd.getOptionValue(OPTION_OUTPUTDIR), date + "_" + cmd.getOptionValue(OPTION_RUNNAME));
            
            if (cmd.hasOption(Settings.OPTION_QRELS)) {
                EvalApp.main(new String[]{"-"+Settings.OPTION_TSV_FILE, tsvFile, "-"+Settings.OPTION_EVAL_OUTPUT, tsvFile.replace(".tsv", ".eval"), "-"+Settings.OPTION_OUTPUTDIR, cmd.getOptionValue(OPTION_OUTPUTDIR), "-"+Settings.OPTION_QRELS, cmd.getOptionValue(Settings.OPTION_QRELS)});
            }
        } catch (ParseException ex) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "QueryApp", options );
        }
    }
    
}
