package cz.muni.fi.mireval;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class EvalApp {
    
    protected static final Logger logger = Logger.getLogger(EvalApp.class.getName());
    
    public static void main( String[] args) {
        Options options = Settings.getEvalAppOptions();
        try {            
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            
            Evaluation eval = new AdhocEvaluation(cmd.getOptionValue(Settings.OPTION_QRELS));
            eval.evaluate(cmd.getOptionValue(Settings.OPTION_TSV_FILE));
            
            String evalResultFile = cmd.getOptionValue(Settings.OPTION_EVAL_OUTPUT);
            
            eval.writeEvaluationResult(evalResultFile);
            
            evalResultFile = evalResultFile.replace(".eval", "-queries.eval");
            eval.writeEvaluationResultOfEachQuery(evalResultFile);
            
            try {
                QueriesPostProcessor.putInSingleFile(cmd.getOptionValue(Settings.OPTION_OUTPUTDIR));
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        } catch (ParseException ex) {            
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "EvalApp", options );
        }
    }
}
