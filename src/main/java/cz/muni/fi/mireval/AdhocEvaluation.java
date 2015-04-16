package cz.muni.fi.mireval;

/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org 
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.ac.uk/
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is AdhocEvaluation.java.
 *
 * The Original Code is Copyright (C) 2004-2014 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 *   Ben He <ben{a.}dcs.gla.ac.uk> 
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Performs the evaluation for TREC's tasks, except the named page task. The
 * evaluation measures include the mean average precision and other measures
 * such as precision at 10, precision at 10%, and so on....
 *
 * @author Gianni Amati, Ben He
 */
public class AdhocEvaluation extends Evaluation {

    protected static final Logger logger = Logger.getLogger(AdhocEvaluation.class);

    protected static final int[] PRECISION_RANKS = new int[]{1, 2, 3, 4, 5, 10};
    protected static final int[] PRECISION_PERCENTAGES = new int[]{0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
    protected static final int[] BPREF_RANKS = new int[]{20, 50, 100, 1000};
    
    private static final boolean printRprecision = false;
    private static final boolean printMAP = true;
    private static final boolean printAvgPrecisionForQuery = true;
    private static final boolean printBprefAtRankForQuery = true;
    private static final boolean printBprefRanks = true;
    private static final boolean printPrecisionRanks = true;
    private static final boolean printPrecisionPercentages = false;
    
    /**
     * The maximum number of documents retrieved for a query.
     */
    protected int maxNumberRetrieved;
    /**
     * The number of effective queries. An effective query is a query that has
     * corresponding relevant documents in the qrels file.
     */
    protected int numberOfEffQuery;
    /**
     * The total number of documents retrieved in the task.
     */
    protected int totalNumberOfRetrieved;
    /**
     * The total number of relevant documents in the qrels file for the queries
     * processed in the task.
     */
    protected int totalNumberOfRelevant;
    /**
     * The total number of relevant documents retrieved in the task.
     */
    protected int totalNumberOfRelevantRetrieved;
    /**
     * Precision at rank number of documents
     */
    protected Map<Integer, Double> precisionAtRank = new HashMap<Integer, Double>();

    protected Map<Integer, Double> precisionAtRecall = new HashMap<Integer, Double>();
    
    protected Map<Integer, Double> bprefAtRank = new HashMap<Integer, Double>();
    
    protected List<Double> avgPrecisionsByQuery = new ArrayList<Double>();
    
    protected Map<Integer, List<Double>> bprefAtRankByQuery = new HashMap<Integer, List<Double>>();
    
    protected List<Map<Integer, Double>> relevantDocumentsAtRankByQuery = new ArrayList<Map<Integer, Double>>();

    /**
     * Create adhoc evaluation
     */
    public AdhocEvaluation() {
        super();
    }

    /**
     * Create adhoc evaluation
     *
     * @param qrelsFile
     */
    public AdhocEvaluation(String qrelsFile) {
        super(qrelsFile);
    }

    /**
     * Create adhoc evaluation
     *
     * @param qrelsFiles
     */
    public AdhocEvaluation(String[] qrelsFiles) {
        super(qrelsFiles);
    }

    /**
     * Average Precision.
     */
    protected double meanAveragePrecision;
    /**
     * Relevant Precision.
     */
    protected double meanRelevantPrecision;
    /**
     * The query number of each query.
     */
    protected List<String> queryNoList = new ArrayList<String>();
    
    /**
     * Initialise variables.
     */
    public void initialise() {
        this.maxNumberRetrieved = 1000;
        this.precisionAtRank.clear();
        this.precisionAtRecall.clear();
        this.numberOfEffQuery = 0;
        this.totalNumberOfRetrieved = 0;
        this.totalNumberOfRelevant = 0;
        this.meanAveragePrecision = 0;
        this.meanRelevantPrecision = 0;
    }

    /**
     * Evaluates the given result file.
     *
     * @param resultFilename String the filename of the result file to evaluate.
     */
    public void evaluate(String resultFilename) {
        this.initialise();
        logger.info("Evaluating result file: " + resultFilename);

		//int retrievedQueryCounter = 0;
        //int releventQueryCounter = 0; 
        int effQueryCounter = 0;

        int[] numberOfRelevantRetrieved = null;
        int[] numberOfRelevant = null;
        int[] numberOfRetrieved = null;
        List<Record[]> listOfRetrieved = new ArrayList<Record[]>();
        List<Record[]> listOfRelevantRetrieved = new ArrayList<Record[]>();
        List<Record[]> listOfNonRelevantRetrieved = new ArrayList<Record[]>();
        List<Integer> numberOfRelevantList = new ArrayList<Integer>();
        List<Integer> numberOfRetrievedList = new ArrayList<Integer>();
        List<Integer> numberOfRelevantRetrievedList = new ArrayList<Integer>();
        List<Integer> numberOfNonRelevantRetrievedList = new ArrayList<Integer>();

        /**
         * Read records from the result file
         */
        try {

            final BufferedReader br = new BufferedReader(new FileReader(resultFilename));
            String str = null;
            String previous = ""; // the previous query number
            int numberOfRetrievedCounter = 0;
            int numberOfRelevantRetrievedCounter = 0;
            int numberOfNonRelevantRetrievedCounter = 0;
            List<Record> relevantRetrievedTemp = new ArrayList<Record>();
            List<Record> retrievedTemp = new ArrayList<Record>();
            List<Record> nonRelevantRetrievedTemp = new ArrayList<Record>();
            while ((str = br.readLine()) != null) {
                StringTokenizer stk = new StringTokenizer(str);
                String queryid = stk.nextToken();

                //remove non-numeric letters in the queryNo
                StringBuilder queryNoTmp = new StringBuilder();
                boolean firstNumericChar = false;
                for (int i = queryid.length() - 1; i >= 0; i--) {
                    if (queryid.charAt(i) >= '0' && queryid.charAt(i) <= '9') {
                        queryNoTmp.append(queryid.charAt(i));
                        firstNumericChar = true;
                    } else if (firstNumericChar) {
                        break;
                    }
                }
                queryid = "" + Integer.parseInt(queryNoTmp.reverse().toString());
                if (!qrels.queryExistInQrels(queryid)) {
                    continue;
                }

                stk.nextToken();
                String docID = stk.nextToken();

                int rank = Integer.parseInt(stk.nextToken());
                if (!previous.equals(queryid)) {
                    if (effQueryCounter != 0) {
                        numberOfRetrievedList.add(Integer.valueOf(numberOfRetrievedCounter));
                        numberOfRelevantRetrievedList.add(Integer.valueOf(numberOfRelevantRetrievedCounter));
                        numberOfNonRelevantRetrievedList.add(Integer.valueOf(numberOfNonRelevantRetrievedCounter));
                        listOfRetrieved.add((Record[]) retrievedTemp.toArray(new Record[retrievedTemp.size()]));
                        listOfRelevantRetrieved.add((Record[]) relevantRetrievedTemp.toArray(new Record[relevantRetrievedTemp.size()]));
                        listOfNonRelevantRetrieved.add((Record[]) nonRelevantRetrievedTemp.toArray(new Record[nonRelevantRetrievedTemp.size()]));
                        numberOfRetrievedCounter = 0;
                        numberOfRelevantRetrievedCounter = 0;
                        numberOfNonRelevantRetrievedCounter = 0;
                        retrievedTemp = new ArrayList<Record>();
                        relevantRetrievedTemp = new ArrayList<Record>();
                        nonRelevantRetrievedTemp = new ArrayList<Record>();
                    }
                    effQueryCounter++;
                    queryNoList.add(queryid);
                    numberOfRelevantList.add(Integer.valueOf(qrels.getNumberOfRelevant(queryid)));
                }
                previous = queryid;
                numberOfRetrievedCounter++;
                totalNumberOfRetrieved++;
                retrievedTemp.add(new Record(queryid, docID, rank));
                if (qrels.isRelevant(queryid, docID)) {
                    relevantRetrievedTemp.add(new Record(queryid, docID, rank));
                    numberOfRelevantRetrievedCounter++;
                } else if (qrels.isNonRelevantJudged(queryid, docID)) {
                    nonRelevantRetrievedTemp.add(new Record(queryid, docID, rank));
                    numberOfNonRelevantRetrievedCounter++;
                }
            }
            listOfRelevantRetrieved.add(relevantRetrievedTemp.toArray(new Record[relevantRetrievedTemp.size()]));
            listOfRetrieved.add(retrievedTemp.toArray(new Record[retrievedTemp.size()]));
            listOfNonRelevantRetrieved.add(nonRelevantRetrievedTemp.toArray(new Record[nonRelevantRetrievedTemp.size()]));
            numberOfRetrievedList.add(Integer.valueOf(numberOfRetrievedCounter));
            numberOfRelevantRetrievedList.add(Integer.valueOf(numberOfRelevantRetrievedCounter));
            numberOfNonRelevantRetrievedList.add(Integer.valueOf(numberOfNonRelevantRetrievedCounter));
            br.close();
            numberOfRelevantRetrieved = new int[effQueryCounter];
            numberOfRelevant = new int[effQueryCounter];
            numberOfRetrieved = new int[effQueryCounter];
            this.totalNumberOfRelevant = 0;
            this.totalNumberOfRelevantRetrieved = 0;
            this.totalNumberOfRetrieved = 0;
            for (int i = 0; i < effQueryCounter; i++) {
                numberOfRelevantRetrieved[i]
                        = ((Integer) numberOfRelevantRetrievedList.get(i)).intValue();
                numberOfRelevant[i] = ((Integer) numberOfRelevantList.get(i)).intValue();
                numberOfRetrieved[i] = ((Integer) numberOfRetrievedList.get(i)).intValue();
                this.totalNumberOfRetrieved += numberOfRetrieved[i];
                this.totalNumberOfRelevant += numberOfRelevant[i];
                this.totalNumberOfRelevantRetrieved += numberOfRelevantRetrieved[i];
            }
        } catch (IOException e) {
            logger.error("Exception while evaluating", e);
        }

        List<Map<Integer,Double>> precisionAtRankByQuery = new ArrayList<Map<Integer,Double>>(effQueryCounter);
        List<Map<Integer,Double>> precisionAtRecallByQuery = new ArrayList<Map<Integer,Double>>(effQueryCounter);
        for (int i = 0; i < effQueryCounter; i++) {
            precisionAtRankByQuery.add(new HashMap<Integer,Double>());
            precisionAtRecallByQuery.add(new HashMap<Integer,Double>());
            relevantDocumentsAtRankByQuery.add(new HashMap<Integer, Double>());
        }

        double[] ExactPrecision = new double[effQueryCounter];
        double[] RPrecision = new double[effQueryCounter];
        Arrays.fill(ExactPrecision, 0.0d);
        Arrays.fill(RPrecision, 0.0d);

        meanAveragePrecision = 0d;
        meanRelevantPrecision = 0d;
        numberOfEffQuery = effQueryCounter;
        
        for (int i = 0; i < effQueryCounter; i++) {
            Record[] relevantRetrievedForQuery = (Record[]) listOfRelevantRetrieved.get(i);
            Record[] nonRelevantRetrievedForQuery = (Record[]) listOfNonRelevantRetrieved.get(i);
            int relevantDocsCountForQuery = qrels.getRelevantDocuments(queryNoList.get(i)).size();
            int nonRelevantDocsCountForQuery = qrels.getNonRelevantDocuments(queryNoList.get(i)).size();
            for (int j = 0; j < relevantRetrievedForQuery.length; j++) {
                if (relevantRetrievedForQuery[j].rank < numberOfRelevant[i]) {
                    RPrecision[i] += 1d;
                }
                for (int precisionRank : PRECISION_RANKS) {
                    if (relevantRetrievedForQuery[j].rank <= precisionRank) {
                        adjustOrPutValue(precisionAtRankByQuery.get(i), precisionRank, 1.0d, 1.0d);
                        adjustOrPutValue(relevantDocumentsAtRankByQuery.get(i), precisionRank, 1.0d, 1.0d);
                    }
                }

                ExactPrecision[i] += (double) (j + 1)
                        / (1d + relevantRetrievedForQuery[j].rank);
                relevantRetrievedForQuery[j].precision
                        = (double) (j + 1)
                        / (1d + relevantRetrievedForQuery[j].rank);
                relevantRetrievedForQuery[j].recall
                        = (double) (j + 1) / numberOfRelevant[i];                
            }
            
            for (int bprefRank : BPREF_RANKS) {
                double rSum = 0d;
                List<Double> bprefsAtRank = bprefAtRankByQuery.get(bprefRank);
                if (bprefsAtRank == null) {
                    bprefsAtRank = new ArrayList<Double>();
                    bprefAtRankByQuery.put(bprefRank, bprefsAtRank);
                }
                for (int j = 0; j < relevantRetrievedForQuery.length; j++) {
                    if (relevantRetrievedForQuery[j].rank <= bprefRank) {
                        double n = 0;
                        for (Record r : nonRelevantRetrievedForQuery) {
                            if (r.rank < relevantRetrievedForQuery[j].rank) {
                                n += 1;
                            }
                        }
                        if (n > 0) {
                            n = Math.min(n, relevantDocsCountForQuery) / Math.min(relevantDocsCountForQuery, nonRelevantDocsCountForQuery);
                        }
                        n = 1 - n;
                        rSum += n;
                    }
                }
                double queryBprefAtRank = rSum / relevantDocsCountForQuery;
                adjustOrPutValue(bprefAtRank, bprefRank, queryBprefAtRank, queryBprefAtRank);
                bprefsAtRank.add(queryBprefAtRank);
            }
            
            for (int j = 0; j < relevantRetrievedForQuery.length; j++) {
                for (int precisionPercentage : PRECISION_PERCENTAGES) {
                    final double fraction = ((double) precisionPercentage) / 100.0d;
                    if (relevantRetrievedForQuery[j].recall >= fraction
                            && (relevantRetrievedForQuery[j].precision >= getValueOrZero(precisionAtRecallByQuery.get(i).get(precisionPercentage)))) {
                        precisionAtRecallByQuery.get(i).put(precisionPercentage, relevantRetrievedForQuery[j].precision);
                    }
                }

            }
            //Modified by G.AMATI 7th May 2002
            if (numberOfRelevant[i] > 0) {
                ExactPrecision[i] /= ((double) numberOfRelevant[i]);
            } else {
                numberOfEffQuery--;
            }
            if (numberOfRelevant[i] > 0) {
                RPrecision[i] /= ((double) numberOfRelevant[i]);
            }
            if (printAvgPrecisionForQuery) {
                avgPrecisionsByQuery.add(ExactPrecision[i]);
            }
            meanAveragePrecision += ExactPrecision[i];
            meanRelevantPrecision += RPrecision[i];

            for (int precisionRank : PRECISION_RANKS) {
                adjustOrPutValue(precisionAtRank, precisionRank,
                        getValueOrZero(precisionAtRankByQuery.get(i).get(precisionRank)) / (double) precisionRank,
                        getValueOrZero(precisionAtRankByQuery.get(i).get(precisionRank)) / (double) precisionRank);
            }
            
            
        }
        for (int i = 0; i < effQueryCounter; i++) {
            for (int precisionRecall : PRECISION_PERCENTAGES) {
                adjustOrPutValue(precisionAtRecall, precisionRecall,
                        getValueOrZero(precisionAtRecallByQuery.get(i).get(precisionRecall)),
                        getValueOrZero(precisionAtRecallByQuery.get(i).get(precisionRecall)));
            }
        }

        final double numberOfEffQueryD = (double) numberOfEffQuery;
        
        transformValues(precisionAtRecall, numberOfEffQueryD);
        transformValues(precisionAtRank, numberOfEffQueryD);
        meanAveragePrecision /= (double) numberOfEffQuery;
        meanRelevantPrecision /= (double) numberOfEffQuery;
        
        transformValues(bprefAtRank, numberOfEffQueryD);
    }

    /**
     * Output the evaluation result of each query to the specific file.
     *
     * @param resultEvalFilename String the name of the file in which to save
     * the evaluation results.
     */
    public void writeEvaluationResultOfEachQuery(String resultEvalFilename) {
        try {
            final PrintWriter out = new PrintWriter(new FileWriter(resultEvalFilename));
            final StringBuilder sb = new StringBuilder();
            List<Double> bpref1000byQuery = bprefAtRankByQuery.get(1000);
            Collections.sort(this.queryNoList, new NtcirQueryNoComparator());
            sb.append("QUERYNO,MAP,BPREF,REL5,REL10\n");
            for (int i = 0; i < this.queryNoList.size(); i++) {
                sb.append(
                        queryNoList.get(i)
                        + ","
                        + round(this.avgPrecisionsByQuery.get(i), 4)
                        + ","
                        + round(bpref1000byQuery.get(i), 4)
                        + ","
                        + (relevantDocumentsAtRankByQuery.get(i).get(5) == null ? "0" : round(relevantDocumentsAtRankByQuery.get(i).get(5),0))
                        + ","
                        + (relevantDocumentsAtRankByQuery.get(i).get(10) == null ? "0" : round(relevantDocumentsAtRankByQuery.get(i).get(10),0))
                        + "\n");
            }
            out.print(sb.toString());
            out.close();
        } catch (IOException fnfe) {
            logger.error("Couldn't create evaluation file " + resultEvalFilename, fnfe);
        }
    }

    /**
     * Output the evaluation result to the specific file.
     *
     * @param out java.io.PrintWriter the stream to which the results are
     * printed.
     */
    public void writeEvaluationResult(PrintWriter out) {
        out.println("____________________________________");
        out.println("Number of queries  = " + numberOfEffQuery);
        out.println("Retrieved          = " + totalNumberOfRetrieved);
        out.println("Relevant           = " + totalNumberOfRelevant);
        out.println("Relevant retrieved = " + totalNumberOfRelevantRetrieved);
        out.println("____________________________________");
        if (printMAP) {
            out.println("MAP: " + round(meanAveragePrecision, 4));
            out.println("____________________________________");
        }
        if (printRprecision) {
            out.println("R Precision      : " + round(meanRelevantPrecision, 4));
            out.println("____________________________________");
        }
        if (printBprefRanks) {
            for (int bprefRank : BPREF_RANKS) {
                out.printf("Bpref at   %d : %s\n", bprefRank, round(bprefAtRank.get(bprefRank), 4));
            }
            out.println("____________________________________");
        }
        if (printPrecisionRanks) {
            for (int precisionRank : PRECISION_RANKS) {
                out.printf("Precision at   %d : %s\n", precisionRank, round(precisionAtRank.get(precisionRank), 4));
            }
            out.println("____________________________________");
        }
        if (printPrecisionPercentages) {
            for (int precisionPercent : PRECISION_PERCENTAGES) {
                out.printf("Precision at   %d%%: %s\n", precisionPercent, round(precisionAtRecall.get(precisionPercent), 4));
            }
            out.println("____________________________________");
        }
//        if (printAvgPrecisionForQuery) {
//            out.println("Avg precision by query");
//            for (int i = 0; i < avgPrecisionsByQuery.size(); i++) {                
//                out.printf("Query %d: %s\n", i, round(avgPrecisionsByQuery.get(i), 4));
//            }
//            out.println("____________________________________");
//        }
//        if (printBprefAtRankForQuery) {
//            out.println("Bpref at rank by query");
//            for (Integer bprefRank : bprefAtRankByQuery.keySet()) {
//                List<Double> queryBprefs = bprefAtRankByQuery.get(bprefRank);
//                out.printf("Bpref Rank %d\n", bprefRank);
//                for (int i = 0; i < queryBprefs.size(); i++) {
//                    out.printf("Query %d: %s\n", i, round(queryBprefs.get(i), 4));
//                }
//            }
//        }
        out.flush();
    }

    private void adjustOrPutValue(Map<Integer, Double> map, int precisionRank, double adjustValue, double putValue) {
        Double getValue = map.get(precisionRank);
        if (getValue == null) {
            map.put(precisionRank, putValue);
        } else {
            map.put(precisionRank, getValue + adjustValue);
        }
    }
    
    private void transformValues(Map<Integer, Double> map, double value) {
        for (Integer i : map.keySet()) {
            map.put(i, map.get(i) / value);
        }
    }
    
    public static String round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.toPlainString();
    }
    
    private Double getValueOrZero(Double d) {
        return d == null ? 0 : d;
    }
    
    public class NtcirQueryNoComparator implements Comparator<String> {

        public int compare(String o1, String o2) {
            int i1 = Integer.parseInt(o1);
            int i2 = Integer.parseInt(o2);
            
            return i1 - i2;
        }
        
    }
}
