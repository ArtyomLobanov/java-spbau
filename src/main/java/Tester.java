import java.io.File;
import java.util.Arrays;

public class Tester {
    public static void main(String[] args) throws Exception {
//        String path = args[0];
        String path = "C:\\workspace";

        long timeStart = System.currentTimeMillis();
        System.out.println("Run single thread calculating");
        System.out.println("Result: " + Arrays.toString(HashMD5.hash(new File(path))));
        System.out.println("Time: " + (System.currentTimeMillis() - timeStart) + " mills");

        System.out.println("Run multi thread calculating");
        System.out.println("Result: " + Arrays.toString(HashMD5.hashParallel(new File(path))));
        System.out.println("Time: " + (System.currentTimeMillis() - timeStart) + " mills");
    }
}
