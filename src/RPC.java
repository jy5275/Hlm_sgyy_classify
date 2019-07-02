import java.io.*;
import java.util.*;
import com.hankcs.hanlp.*;
import com.hankcs.hanlp.seg.*;
import com.hankcs.hanlp.seg.NShort.*;
import com.hankcs.hanlp.seg.common.Term;

import weka.classifiers.*;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.*;
import weka.classifiers.evaluation.*;
import weka.classifiers.rules.*;
import weka.classifiers.trees.*;
import weka.core.*;

import java.net.*;

class RPCReceiver implements Runnable {
    ServerSocket socket6789;
    boolean stop = false;
    Socket client = null;
    Honglm lock;

    public RPCReceiver(Honglm l_) throws IOException {
        socket6789 = new ServerSocket(6789);
        socket6789.setSoTimeout(8000);
        lock = l_;
    }

    public void terminate() {
        stop = true;
    }

    public void answer(Hashtable<String, Object> msg) throws Exception {
        if (msg.get("result") == null) { // Call test method, let's response!
            String callingmethod = (String) (msg.get("method"));
            if (callingmethod == null) {
                System.out.println("Received method is null!");
                return;
            } else if (!callingmethod.equals("test")) {
                System.out.println("Unknown method!");
                return;
            } else { // OK!
                RPC.sendsubmit(msg, lock);
            }
        } else {
            System.out.println("----------- Received message -----------");
            System.out.println(msg);
            System.out.println("-------------- END --------------\n");
        }
    }

    public void run() {
        int timeoutn = 0;
        Hashtable<String, Object> readone;
        while (timeoutn < 3) {
            try {
                client = socket6789.accept();
                // System.out.println("LocalPort:" + client.getLocalPort());
                // System.out.println("LocalSocketAddress:" + client.getLocalSocketAddress());
                timeoutn = 0;
            } catch (Exception e) {
                e.printStackTrace();
                timeoutn++;
                continue;
            }

            try {
                ObjectInputStream in = new ObjectInputStream(client.getInputStream());
                readone = (Hashtable<String, Object>) in.readObject();
                answer(readone);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            RPC.sendgetscore();
            client.close();
            socket6789.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

public class RPC {

    public static void sendinit() throws Exception {
        Socket client = new Socket("222.29.98.46", 9876);
        ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
        Hashtable<String, Object> msgtosend = new Hashtable<String, Object>();
        msgtosend.put("method", "initTest");
        msgtosend.put("param1", "1600013239");
        msgtosend.put("param2", "UTF-8");
        msgtosend.put("id", "1600013239-" + System.currentTimeMillis());
        out.writeObject(msgtosend);
        out.flush();
        out.close();
        client.close();
    }

    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list(); // 递归删除目录中的子目录/文件
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    System.out.println("Delete file fail!");
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    public static void sendgetscore() throws Exception {
        Socket client = new Socket("222.29.98.46", 9876);
        ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
        Hashtable<String, Object> msgtosend = new Hashtable<String, Object>();
        msgtosend.put("method", "getScore");
        msgtosend.put("param1", "1600013239");
        msgtosend.put("id", "1600013239_" + System.currentTimeMillis());
        out.writeObject(msgtosend);
        out.flush();
        out.close();
        client.close();
    }

    public static void sendclear() throws Exception {
        Socket client = new Socket("222.29.98.46", 9876);
        ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
        Hashtable<String, Object> msgtosend = new Hashtable<String, Object>();
        msgtosend.put("method", "clearScore");
        msgtosend.put("param1", "1600013239");
        msgtosend.put("id", "1600013239_" + System.currentTimeMillis());
        out.writeObject(msgtosend);
        out.flush();
        out.close();
        client.close();
    }

    public static void sendsubmit(Hashtable<String, Object> question, Honglm lock) throws Exception {
        List<String> classifyit = (List<String>) question.get("param4");
        List<String> train0 = (List<String>) question.get("param1");
        List<String> train1 = (List<String>) question.get("param2");
        List<String> train2 = (List<String>) question.get("param3");
        List<Integer> ans = new ArrayList<Integer>();

        // 创建训练集文件
        String tdirname = "./" + (String) question.get("id") + "/";
        String dirname0 = tdirname + "dir0" + "/";
        String dirname1 = tdirname + "dir1" + "/";
        String dirname2 = tdirname + "dir2" + "/";
        String testdirname = tdirname + "testdir" + "/";
        String resultname = tdirname + "results.txt";
        File tdir = new File(tdirname);
        File dir0 = new File(dirname0);
        File dir1 = new File(dirname1);
        File dir2 = new File(dirname2);
        File testdir = new File(testdirname);
        tdir.mkdir();
        dir0.mkdir();
        dir1.mkdir();
        dir2.mkdir();
        testdir.mkdir();
        BufferedWriter bw;
        BufferedWriter trainsetw = new BufferedWriter(new FileWriter(tdirname + "TrainingSet.txt"));
        BufferedWriter testsetw = new BufferedWriter(new FileWriter(tdirname + "TestingSetDir.txt"));
        for (int i = 0; i < train0.size(); i++) {
            bw = new BufferedWriter(new FileWriter(dirname0 + i + ".txt"));
            bw.write(train0.get(i));
            bw.close();
            trainsetw.write("0|" + dirname0 + i + ".txt\n");
        }
        for (int i = 0; i < train1.size(); i++) {
            bw = new BufferedWriter(new FileWriter(dirname1 + i + ".txt"));
            bw.write(train1.get(i));
            bw.close();
            trainsetw.write("1|" + dirname1 + i + ".txt\n");
        }
        for (int i = 0; i < train2.size(); i++) {
            bw = new BufferedWriter(new FileWriter(dirname2 + i + ".txt"));
            bw.write(train2.get(i));
            bw.close();
            trainsetw.write("2|" + dirname2 + i + ".txt\n");
        }
        for (int i = 0; i < classifyit.size(); i++) {
            bw = new BufferedWriter(new FileWriter(testdirname + i + ".txt"));
            bw.write(classifyit.get(i));
            bw.close();
            testsetw.write(testdirname + i + ".txt\n");
        }
        trainsetw.close();
        testsetw.close();

        String[] arg = { tdirname };
        lock.main(arg);

        BufferedReader br = new BufferedReader(new FileReader(resultname));
        String tempString;
        while ((tempString = br.readLine()) != null) {
            int num = Integer.parseInt(tempString.substring(0, 1));
            // System.out.println(num);
            ans.add(num);
        }
        br.close();

        // 创建 TrainingSet.txt 和 TestingSetDir.txt

        String id = "1600013239_" + System.currentTimeMillis();
        Hashtable<String, Object> msg = new Hashtable<String, Object>();
        msg.put("method", "submit");
        msg.put("param1", "1600013239");
        msg.put("param2", question.get("id"));
        msg.put("param3", ans);
        msg.put("id", id);

        Socket client = new Socket("222.29.98.46", 9876);
        ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
        out.writeObject(msg);
        out.flush();
        out.close();
        client.close();
        deleteDir(tdir);
    }

    public static void main(String[] args) throws Exception {
        Honglm hlm = new Honglm();
        RPCReceiver receiver = new RPCReceiver(hlm);
        Thread rthread = new Thread(receiver);
        rthread.start();
        // sendclear();
        sendinit();
        sendgetscore();
        rthread.join();
    }
}