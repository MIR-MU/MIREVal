package cz.muni.fi.mireval;

public class EvalApp 
{
    public static void main( String[] args) {
        if (args.length != 2) {
            System.exit(1);
        }
        
        Evaluation eval = new AdhocEvaluation(Settings.getQrelsFile());
        eval.evaluate(args[0]);

        String evalResultFile = args[1];

        eval.writeEvaluationResult(evalResultFile);

        evalResultFile = evalResultFile.replace(".eval", "-queries.eval");
        eval.writeEvaluationResultOfEachQuery(evalResultFile);
        
//        try {
//            QueriesPostProcessor.putInSingleFile();
//        } catch (IOException ex) {
//            Logger.getLogger(EvalApp.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
}
