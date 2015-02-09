package cz.muni.fi.mireval;

import java.io.File;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args) {
        
        System.out.println(args.length);
        if (args.length != 2) {
            System.exit(1);
        }
        
        File qrels = new File(args[0]);
        File results = new File(args[1]);
        
        Evaluation eval = new AdhocEvaluation(args[0]);
        eval.evaluate(args[1]);
        eval.writeEvaluationResult();
    }
}
