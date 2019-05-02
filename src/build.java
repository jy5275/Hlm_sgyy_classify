
import java.io.*;
import java.util.*;

public class build {
    public static void main(String args[]) throws Exception {

        FileWriter fw = new FileWriter("./sgyytrain.txt");
        for (int i = 1; i <= 12; i++) {
            for (int j = 1; j <= 10; j++) {
                StringBuilder path = new StringBuilder("./data_utf8/sgyy/sgyy");
                if (i < 10)
                    path.append("0");
                path.append(i);
                path.append('_');
                path.append(j);
                String realpath = "2|" + path + ".txt\n";
                fw.write(realpath);
            }
        }
        fw.close();
    }
}