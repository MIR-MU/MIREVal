package cz.muni.fi.mireval;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args) {
        if (args.length != 3) {
            System.exit(1);
        }
        
        File qrels = new File(args[0]);
        File results = new File(args[1]);
        
        Evaluation eval = new AdhocEvaluation(args[0]);
        eval.evaluate(args[1]);
        eval.writeEvaluationResult();
        
        String evalResultFile = args[2];
        evalResultFile = evalResultFile.replace(".eval", "-queries.eval");
        eval.writeEvaluationResultOfEachQuery(evalResultFile);
        
        try {
            QueriesPostProcessor.putInSingleFile();
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
