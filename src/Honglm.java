
/*
 * 从命令行第一个参数指定的路径打开记录TrainingSet路径的文件, 文件每行的格式为 “数字|路径”
 * 从命令行第二个参数指定的路径打开TestingSetDir.txt
 * TestingSetDir.txt每行为一个测试章回的路径
 * 总共240个instace,240折交叉验证准确率为93.33%
 */
import java.io.*;
import java.util.*;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.evaluation.Prediction;

import com.hankcs.hanlp.seg.*;
import com.hankcs.hanlp.seg.NShort.*;
import com.hankcs.hanlp.seg.common.Term;

import weka.classifiers.*;
import weka.classifiers.Evaluation;
import weka.classifiers.rules.*;
import weka.classifiers.trees.J48;
import weka.core.*;

public class Honglm {
    static HashMap<Character, Integer> hm_ch2id = null;
    static HashMap<Integer, Character> hm_id2ch = null;

    public static boolean isCN(char c) {
        return String.valueOf(c).matches("[\u4e00-\u9fa5]");
    }

    public static Evaluation classify(Classifier model, Instances trainSet, Instances testSet) throws Exception {
        Evaluation eva = new Evaluation(trainSet);
        model.buildClassifier(trainSet);
        eva.evaluateModel(model, testSet);
        return eva; // 返回一个集合,元素是testSet中条目的分类结果
    }

    static void createTrainInstances(String dirpath) throws Exception {
        String filepath = dirpath + "TrainingSet.txt";
        BufferedReader datafile = new BufferedReader(new FileReader(filepath));
        String str = null, sdict = null;
        Segment segm = new NShortSegment();

        BufferedReader readdict = new BufferedReader(new FileReader("./dict.txt"));
        int dictsize = 0;
        while ((sdict = readdict.readLine()) != null) {
            for (int i = 0; i < sdict.length(); i++) {
                Character tmpch = sdict.charAt(i);
                if (isCN(tmpch)) {
                    hm_id2ch.put(dictsize, tmpch);
                    hm_ch2id.put(tmpch, dictsize);
                    dictsize++;
                }
            }
        }

        BufferedWriter extractfile = new BufferedWriter(new FileWriter("./TrainInstances.txt"));
        extractfile.write("@relation TrainInstances\n");
        String prefix = "@attribute keyword", suffix = " numeric\n";
        for (int i = 0; i < dictsize; i++) {
            String attrLine = prefix + i + suffix;
            extractfile.write(attrLine);
        } // relation和attribute部分OK！
        extractfile.write("@attribute author {0, 1, 2}\n");
        extractfile.write("@data\n");

        while ((str = datafile.readLine()) != null) { // 每个文件(回)
            int cnt[] = new int[dictsize]; // id->cnt
            int beg = str.indexOf("|");
            String path = str.substring(beg + 1);
            BufferedReader filetoextract = new BufferedReader(new FileReader(path));
            String s = null;
            while ((s = filetoextract.readLine()) != null) { // 逐行读取这一回
                if (s.contains("手机") || s.contains("上卷") || s.contains("下卷"))
                    continue;
                List<Term> termList = segm.seg(s);
                for (int i = 0; i < termList.size(); i++) {
                    String word = termList.get(i).word;
                    if (word.length() > 1)
                        continue;
                    if (hm_ch2id.containsKey(word.charAt(0))) {
                        int tmpid = hm_ch2id.get(word.charAt(0));
                        cnt[tmpid]++;
                    }
                }
            }
            // 输出cnt中存储的虚词统计信息
            for (int i = 0; i < dictsize; i++) {
                extractfile.write(cnt[i] + ",");
                // System.out.println(hm_id2ch.get(i) + ":" + cnt[i]);
            }
            extractfile.write(str.charAt(beg - 1) + "\n");
            filetoextract.close();
        }
        datafile.close();
        extractfile.close();
        System.out.println("TrainingInstaces Built.");

    }

    static void createTestInstances(String dirpath) throws Exception {
        String filepath = dirpath + "TestingSetDir.txt";
        BufferedReader datafile = new BufferedReader(new FileReader(filepath));
        String str = null, sdict = null;
        Segment segm = new NShortSegment();

        BufferedReader readdict = new BufferedReader(new FileReader("./dict.txt"));
        int dictsize = 0;
        while ((sdict = readdict.readLine()) != null) {
            for (int i = 0; i < sdict.length(); i++) {
                Character tmpch = sdict.charAt(i);
                if (isCN(tmpch)) {
                    hm_id2ch.put(dictsize, tmpch);
                    hm_ch2id.put(tmpch, dictsize);
                    dictsize++;
                }
            }
        }

        BufferedWriter extractfile = new BufferedWriter(new FileWriter("./TestInstances.txt"));
        extractfile.write("@relation TestInstances\n");
        String prefix = "@attribute keyword", suffix = " numeric\n";
        for (int i = 0; i < dictsize; i++) {
            String attrLine = prefix + i + suffix;
            extractfile.write(attrLine);
        } // relation和attribute部分OK
        extractfile.write("@attribute author {0, 1, 2}\n");
        extractfile.write("@data\n");

        while ((str = datafile.readLine()) != null) { // 每个文件(回)
            int cnt[] = new int[dictsize]; // id->cnt
            int beg = str.indexOf(".");
            String path = str.substring(beg);
            BufferedReader filetoextract = new BufferedReader(new FileReader(path));
            String s = null;
            while ((s = filetoextract.readLine()) != null) { // 逐行读取这一回
                if (s.contains("手机") || s.contains("上卷") || s.contains("下卷"))
                    continue;
                List<Term> termList = segm.seg(s);
                for (int i = 0; i < termList.size(); i++) {
                    String word = termList.get(i).word;
                    if (word.length() > 1)
                        continue;
                    if (hm_ch2id.containsKey(word.charAt(0))) {
                        int tmpid = hm_ch2id.get(word.charAt(0));
                        cnt[tmpid]++;
                    }
                }
            }
            // 输出cnt中存储的虚词统计信息
            for (int i = 0; i < dictsize; i++)
                extractfile.write(cnt[i] + ",");
            extractfile.write("?\n");
            filetoextract.close();
        }
        datafile.close();
        extractfile.close();
        System.out.println("TestingInstaces Built.");
    }

    public static void main(String args[]) throws Exception {
        hm_ch2id = new HashMap<Character, Integer>();
        hm_id2ch = new HashMap<Integer, Character>();

        createTrainInstances(args[0]);
        createTestInstances(args[1]);
        BufferedReader datafile_train = new BufferedReader(new FileReader("./TrainInstances.txt"));
        BufferedReader datafile_test = new BufferedReader(new FileReader("./TestInstances.txt"));

        Instances data_train = new Instances(datafile_train);
        Instances data_test = new Instances(datafile_test);
        data_train.setClassIndex(data_train.numAttributes() - 1);
        data_test.setClassIndex(data_test.numAttributes() - 1);

        Classifier model = new J48();
        FastVector predictions = new FastVector();
        Evaluation validation = classify(model, data_train, data_test);
        predictions.appendElements(validation.predictions());

        FileWriter fw = new FileWriter("[java19]HW2_1600013239.txt");

        // double correct = 0;
        for (int i = 0; i < predictions.size(); i++) {
            Prediction np = (Prediction) predictions.elementAt(i);
            String res = (int) (np.predicted()) + " \n";
            fw.write(res);
            /*
             * if (i < 60) { if (np.predicted() == 0.0) correct++; } else if (i < 80) { if
             * (np.predicted() == 1.0) correct++; } else { if (np.predicted() == 2.0)
             * correct++; }
             */
        }
        // System.out.println("Accuracy = " + (double) (100 * correct /
        // predictions.size()) + '%');

        fw.close();
        System.out.println("Finished.");
    }
}
