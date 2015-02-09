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
package cz.muni.fi.mireval;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import org.apache.log4j.Logger;


/**
 *
 * @author Martin Liska <martin.liska@ibacz.eu>
 */
public abstract class Evaluation {

    protected static final Logger logger = Logger.getLogger(Evaluation.class);

    /**
     * A structure of a record of retrieved document.
     */
    static public class Record {

        /**
         * The topic number.
         */
        String queryNo;

        /**
         * The rank of the document.
         */
        int rank;
        /**
         * The document identifier.
         */
        String docNo;
        /**
         * The precision at this document.
         */
        double precision;
        /**
         * The recall at this document.
         */
        double recall;

        /**
         * create a record of retrieved document
         *
         * @param _queryNo
         * @param _docNo
         * @param _rank
         */
        public Record(String _queryNo, String _docNo, int _rank) {
            this.queryNo = _queryNo;
            this.rank = _rank;
            this.docNo = _docNo;
        }

        /**
         * get rank number
         *
         * @return int
         */
        public int getRank() {
            return rank;
        }

        /**
         * set rank position
         *
         * @param _rank
         */
        public void setRank(int _rank) {
            this.rank = _rank;
        }

        /**
         * get document number
         *
         * @return String
         */
        public String getDocNo() {
            return docNo;
        }

        /**
         * set query number
         *
         * @param _queryNo
         */
        public void setQueryNo(String _queryNo) {
            this.queryNo = _queryNo;
        }

        /**
         * get query number
         *
         * @return query number
         */
        public String getQueryNo() {
            return queryNo;
        }
    }

    protected Evaluation() {        
    }

    protected Evaluation(String qrelsFile) {
        qrels = new QrelsInMemory(qrelsFile);
    }

    protected Evaluation(String[] qrelsFiles) {
        qrels = new QrelsInMemory(qrelsFiles);
    }

    /**
     * A structure of all the records in the qrels files.
     */
    public QrelsInMemory qrels;

    /**
     * Evaluates the given result file for the given qrels file. All subclasses
     * must implement this method.
     *
     * @param resultFilename java.lang.String the filename of the result file to
     * evaluate.
     */
    abstract public void evaluate(String resultFilename);

    /**
     * Output the evaluation result to standard output
     */
    public void writeEvaluationResult() {
        writeEvaluationResult(new PrintWriter(new OutputStreamWriter(System.out)));
    }

    /**
     * The abstract method that evaluates and prints the results. All the
     * subclasses of Evaluation must implement this method.
     *
     * @param out java.io.PrintWriter
     */
    abstract public void writeEvaluationResult(PrintWriter out);

    /**
     * Output the evaluation result of each query to the specific file.
     *
     * @param evaluationResultFilename String the name of the file in which to
     * save the evaluation results.
     */
    abstract public void writeEvaluationResultOfEachQuery(String evaluationResultFilename);

    /**
     * Output the evaluation result to the specific file.
     *
     * @param resultEvalFilename java.lang.String the filename of the file to
     * output the result.
     */
    public void writeEvaluationResult(String resultEvalFilename) {
        try {
            final PrintWriter out = new PrintWriter(new File(resultEvalFilename));
            writeEvaluationResult(out);
            out.close();
        } catch (IOException fnfe) {
            logger.fatal(
                    "File not found exception occurred when trying to write to file" + resultEvalFilename,
                    fnfe);
        }
    }
}
