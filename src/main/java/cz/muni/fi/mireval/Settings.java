package cz.muni.fi.mireval;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Settings class responsible for loading settings from mias.properties Property file.
 * mias.properties file is located in the path specified by the cz.muni.fi.mias.mias.to file or in the working directory.
 *
 * @author Martin Liska
 * @since 14.12.2012
 */
public class Settings {
    
    public static final String MATHML_NAMESPACE_URI = "http://www.w3.org/1998/Math/MathML";
    
    public static final String OPTION_QUERYURL = "queryurl";
    public static final String OPTION_RUNNAME = "runname";
    public static final String OPTION_TOPICS = "topics";
    public static final String OPTION_OUTPUTDIR = "outputdir";
    
    public static final String OPTION_QRELS = "qrels";    
    public static final String OPTION_TSV_FILE = "tsvfile";
    public static final String OPTION_EVAL_OUTPUT = "outputfile";

    public static Options getQueryAppOptions() {
        Options options = new Options();
        options.addOption(Option.builder(OPTION_QUERYURL)
            .required(true)
            .hasArg()
            .desc("URL for querying topics.")
            .build());
        options.addOption(Option.builder(OPTION_TOPICS)
            .required(true)
            .hasArg()
            .desc("Path to topics file.")
            .build());
        options.addOption(Option.builder(OPTION_OUTPUTDIR)
            .required(true)
            .hasArg()
            .desc("Output directory.")
            .build());
        options.addOption(Option.builder(OPTION_RUNNAME)
            .required(true)
            .hasArg()
            .desc("Run name.")
            .build());
        options.addOption(Option.builder(OPTION_QRELS)
            .hasArg()
            .desc("Path to qrels file.")
            .build());
        return options;        
    }
    
    public static Options getEvalAppOptions() {
        Options options = new Options();
        options.addOption(Option.builder(OPTION_OUTPUTDIR)
            .required(true)
            .hasArg()
            .desc("Output directory.")
            .build());
        options.addOption(Option.builder(OPTION_QRELS)
            .required(true)
            .hasArg()
            .desc("Path to qrels file.")
            .build());
        options.addOption(Option.builder(OPTION_TSV_FILE)
            .required(true)
            .hasArg()
            .desc("Path to tsv input file to be evaluated.")
            .build());
        options.addOption(Option.builder(OPTION_EVAL_OUTPUT)
            .required(true)
            .hasArg()
            .desc("Path to output file with evaluation.")
            .build());
        return options;        
    }
}
