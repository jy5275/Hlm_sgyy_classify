
import java.io.*;
import java.util.*;

import weka.classifiers.*;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.*;
import weka.classifiers.rules.*;
import weka.classifiers.trees.*;
import weka.core.*;
//import weka.datagenerators.classifiers.classification.BayesNet;
/*
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.seg.*;
import com.hankcs.hanlp.seg.NShort.*;
*/
public class WekaTest {
    public WekaTest(){super();}
    public static Evaluation classify(Classifier model, Instances trainSet, Instances testSet) throws Exception {
        Evaluation eva = new Evaluation(trainSet);
        model.buildClassifier(trainSet);
        eva.evaluateModel(model, testSet);
        return eva; // 返回一个集合,元素是testSet中条目的分类结果
    }

    public static double calculateAccuracy(FastVector pred) {
        double correct = 0;
        for (int i = 0; i < pred.size(); i++) { // 挨个看pred中各条目是否正确分类
            NominalPrediction np = (NominalPrediction) pred.elementAt(i);
            if (np.predicted() == np.actual())
                correct++;
        }

        return 100 * correct / pred.size(); // 准确率 = 正确分类数 / 测试总数
    }

    public static Instances[][] crossValidationSplit(Instances data, int folds) {
        Instances split[][] = new Instances[2][folds];
        for (int i = 0; i < folds; i++) { // 每次得一个划分,存在split[][i]中
            split[0][i] = data.trainCV(folds, i);
            split[1][i] = data.testCV(folds, i);
        }
        return split;
    }

    public static void main(String args[]) throws Exception {
        BufferedReader datafile = null;
        String filename = "extractarff.txt";
        datafile = new BufferedReader(new FileReader(filename));

        Instances data = new Instances(datafile); // data有13个instance
        data.setClassIndex(data.numAttributes() - 1);

        // Do 10-split corss validation
        Instances split[][] = crossValidationSplit(data, 240);

        Instances trainSplits[] = split[0]; // 第i次测试,用trainSplits[i]训练
        Instances testSplits[] = split[1]; // testSplits[i]是测试集,

        Classifier models[] = { new BayesNet(), new J48(), new DecisionTable(), new DecisionStump() };

        for (int j = 0; j < models.length; j++) {
            FastVector pred = new FastVector();
            for (int i = 0; i < trainSplits.length; i++) {
                Evaluation vali = classify(models[j], trainSplits[i], testSplits[i]);
                pred.appendElements(vali.predictions());
            }

            double accuracy = calculateAccuracy(pred);
            System.out.println("Accuracy of " + models[j].getClass().getSimpleName() + ": "
                    + String.format("%.2f%%", accuracy) + "\n--------------------");
        }
        System.out.println("Test finish.");
    }

}