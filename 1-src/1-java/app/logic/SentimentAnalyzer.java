package logic;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import unused.SentimentClassification;
import models.SentimentResult;
import org.ejml.simple.SimpleMatrix;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

import edu.stanford.nlp.util.logging.RedwoodConfiguration;

public class SentimentAnalyzer {


    /*
     * "Very negative" = 0 "Negative" = 1 "Neutral" = 2 "Positive" = 3
     * "Very positive" = 4
     */

    static Properties props;
    static StanfordCoreNLP pipeline;

    public void initialize() {
        // this is your print stream, store the reference
        PrintStream err = System.err;

        // now make all writes to the System.err stream silent
        System.setErr(new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        }));


        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and sentiment
        props = new Properties();
        RedwoodConfiguration.current().clear().apply();

        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment, pos, lemma"); // ("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        pipeline = new StanfordCoreNLP(props);

        // set everything bck to its original state afterwards
        System.setErr(err);
    }

    public SentimentResult getSentimentResult(String text) {

        SentimentResult sentimentResult = new SentimentResult();
        SentimentClassification sentimentClass = new SentimentClassification();

        if (text != null && text.length() > 0) {

            // run all Annotators on the text
            Annotation annotation = pipeline.process(text);

            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                // this is the parse tree of the current sentence
                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                SimpleMatrix sm = RNNCoreAnnotations.getPredictions(tree);
                String sentimentType = sentence.get(SentimentCoreAnnotations.SentimentClass.class);

                sentimentClass.setVeryPositive((double)Math.round(sm.get(4) * 100d));
                sentimentClass.setPositive((double)Math.round(sm.get(3) * 100d));
                sentimentClass.setNeutral((double)Math.round(sm.get(2) * 100d));
                sentimentClass.setNegative((double)Math.round(sm.get(1) * 100d));
                sentimentClass.setVeryNegative((double)Math.round(sm.get(0) * 100d));

                sentimentResult.setSentimentScore(RNNCoreAnnotations.getPredictedClass(tree));
                sentimentResult.setSentimentType(sentimentType);
                sentimentResult.setSentimentClass(sentimentClass);
            }

        }


        return sentimentResult;
    }

}