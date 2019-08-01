import java.io.*;
import java.util.*;
/*
import com.hankcs.hanlp.*;
import com.hankcs.hanlp.seg.*;
import com.hankcs.hanlp.seg.NShort.*;
import com.hankcs.hanlp.seg.common.Term;
*/
import weka.classifiers.*;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.*;
import weka.classifiers.evaluation.*;
import weka.classifiers.rules.*;
import weka.classifiers.trees.*;
import weka.core.*;

public class Try {
    public Try(){super();}

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

    static void createInstances() throws Exception {
        String filename = "TrainingSet_all.txt";
        BufferedReader datafile = new BufferedReader(new FileReader(filename));
        String str = null, sdict = null;

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

        BufferedWriter extractfile = new BufferedWriter(new FileWriter("./extractarff.txt"));
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
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (hm_ch2id.containsKey(c)) {
                        int tmpid = hm_ch2id.get(c);
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

    public static void main(String args[]) throws Exception {
        hm_ch2id = new HashMap<Character, Integer>();
        hm_id2ch = new HashMap<Integer, Character>();

        createInstances();
        WekaTest.main(args);
    }
}