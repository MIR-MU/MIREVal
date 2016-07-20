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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Martin Liska <martin.liska@ibacz.eu>
 */
public class QueriesPostProcessor {

    private static final String SEP = ";";

    private static class QueryResult {

        public int queryno;
        public double map;
        public double bpref;

    }

    private static class FileQueryResult {

        public String fileName;
        public List<QueryResult> queryResults = new ArrayList<QueryResult>();

        public QueryResult getQueryResultForQuery(int queryNo) {
            for (QueryResult qe : queryResults) {
                if (qe.queryno == queryNo) {
                    return qe;
                }
            }
            return null;
        }
    }

    private static class FileEvalResult {

        public String fileName;
        public SortedMap<Integer, Double> bpref = new TreeMap<>();
        public double map;
        public SortedMap<Integer, Double> precision = new TreeMap<>();
    }

    public static void putInSingleFile(String outputDir) throws FileNotFoundException, IOException {
        File dir = new File(outputDir);
        List<FileQueryResult> fqrs = new ArrayList<FileQueryResult>();
        List<FileEvalResult> fers = new ArrayList<FileEvalResult>();
        Pattern metricAtPattern = Pattern.compile("^(.*?)\\s+at\\s+(\\d+)\\s+:\\s+(\\d+(\\.\\d+)?)$");
        for (File f : dir.listFiles()) {
            if (f.getName().contains("-queries.eval")) {
                FileQueryResult fqr = new FileQueryResult();
                fqr.fileName = f.getName();
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line = br.readLine();
                while ((line = br.readLine()) != null) {
                    String[] split = line.split(",");
                    QueryResult qr = new QueryResult();
                    qr.queryno = Integer.parseInt(split[0]);
                    qr.map = Double.parseDouble(split[1]);
                    qr.bpref = Double.parseDouble(split[2]);
                    fqr.queryResults.add(qr);
                }
                fqrs.add(fqr);
                br.close();
            } else if (f.getName().contains(".eval")) {
                FileEvalResult fer = new FileEvalResult();
                fer.fileName = f.getName();
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line = br.readLine();
                while ((line = br.readLine()) != null) {
                    if (line.contains("MAP:")) {
                        String[] split = line.split(" ");
                        fer.map = Double.valueOf(split[split.length - 1]);
                    } else if (line.contains("Bpref")) {
                        Matcher metricAtMatcher = metricAtPattern.matcher(line);
                        if (metricAtMatcher.find()) {
                            int at = Integer.valueOf(metricAtMatcher.group(2));
                            double value = Double.valueOf(metricAtMatcher.group(3));
                            fer.bpref.put(at, value);
                        }
                    } else if (line.contains("Precision")) {
                        Matcher metricAtMatcher = metricAtPattern.matcher(line);
                        if (metricAtMatcher.find()) {
                            int at = Integer.valueOf(metricAtMatcher.group(2));
                            double value = Double.valueOf(metricAtMatcher.group(3));
                            fer.precision.put(at, value);
                        }
                    }
                }
                fers.add(fer);
            }
        }

        printMetricsToFile(fqrs, "MAP", outputDir);
        printMetricsToFile(fqrs, "BPREF", outputDir);
        printEvalsToFile(fers, outputDir);
    }

    private static void printMetricsToFile(List<FileQueryResult> fqrs, String metric, String outputDir) throws IOException {
        DecimalFormat formatter = new DecimalFormat("#.####");
        StringBuilder result = new StringBuilder();
        result.append("queryNo");
        for (FileQueryResult fqr : fqrs) {
            result.append(SEP).append(getStrategyName(fqr.fileName));
        }
        Map<Integer, Double> avgs = new HashMap<Integer, Double>();
        result.append("\n");
        for (int i = 1; i <= 50; i++) {
            result.append(i);
            for (int j = 0; j < fqrs.size(); j++) {
                FileQueryResult fqr = fqrs.get(j);
                QueryResult queryResultForQuery = fqr.getQueryResultForQuery(i);
                result.append(SEP);
                if (queryResultForQuery != null) {
                    if (metric.equals("MAP")) {
//                        result.append(formatter.format(queryResultForQuery.map));
                        result.append(queryResultForQuery.map);
                        addToAvgList(avgs, j, queryResultForQuery.map);
                    }
                    if (metric.equals("BPREF")) {
//                        result.append(formatter.format(queryResultForQuery.bpref));
                        result.append(queryResultForQuery.bpref);
                        addToAvgList(avgs, j, queryResultForQuery.bpref);
                    }
                }
            }
            result.append("\n");
        }
        result.append("avg");
        for (int i = 0; i < avgs.size(); i++) {
            result.append(SEP);
            Double sum = avgs.get(i);
            sum = sum / fqrs.get(i).queryResults.size();
            result.append(formatter.format(sum).replaceAll(",", "."));
        }

        FileWriter fw = new FileWriter(outputDir + "/all-runs-queries-" + metric + ".csv");
        fw.write(result.toString());
        fw.close();
    }

    private static String getStrategyName(String fileName) {
        String result = fileName.substring(fileName.indexOf("MIRMU_") + 6, fileName.length());
        String[] fileSplit = result.split("\\.");
        String[] split = fileSplit[1].split("_");
        String strategy = "";
        for (int i = 0; i < split.length; i++) {
            strategy += split[i].charAt(0);
        }
        strategy = strategy.toUpperCase();
        result = fileSplit[0] + "-" + strategy;
        result = result.replaceAll("ath", "");
        return result;
    }

    private static void addToAvgList(Map<Integer, Double> map, int position, double value) {
        Double d = map.get(position);
        if (d == null) {
            d = 0d;
        }
        d = d + value;
        map.put(position, d);
    }

    private static void printEvalsToFile(List<FileEvalResult> fers, String outputDir) throws IOException {
        StringBuilder result = new StringBuilder();
        result.append("metric");
        for (FileEvalResult fer : fers) {
            result.append(SEP).append(getStrategyName(fer.fileName));
        }
        result.append("\n");
        result.append("MAP");
        for (FileEvalResult fer : fers) {
            result.append(SEP).append(fer.map);
        }
        if (!fers.isEmpty()) {
            for (int at : fers.get(0).bpref.keySet()) {
                result.append("\n");
                result.append("BPREF").append(at);
                for (FileEvalResult fer : fers) {
                    result.append(SEP).append(fer.bpref.get(at));
                }
            }
            for (int at : fers.get(0).precision.keySet()) {
                result.append("\n");
                result.append("PRECISION").append(at);
                for (FileEvalResult fer : fers) {
                    result.append(SEP).append(fer.precision.get(at));
                }
            }
        }
        FileWriter fw = new FileWriter(outputDir + "/all-runs-eval.csv");
        fw.write(result.toString());
        fw.close();
    }

}
