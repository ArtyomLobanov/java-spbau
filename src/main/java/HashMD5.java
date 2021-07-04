import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class HashMD5 {

    /**
     * Run single-thread calculating of MD5-hash of given file
     *
     * @param file target file
     * @return MD5-hash
     * @throws NoSuchAlgorithmException if md5-algorithm was not found
     * @throws FileNotFoundException    if some file was not found
     * @throws IOException if its occured during file reading
     */
    private static byte[] hashFile(@NotNull File file) throws NoSuchAlgorithmException, IOException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        FileInputStream input = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int readBytes;
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        do {
            readBytes = input.read(buffer);
            if (readBytes != -1) {
                md5.update(buffer, 0, readBytes);
            }
        } while (readBytes != -1);
        return md5.digest();
    }

    /**
     * Run single-thread calculating of MD5-hash of given directory
     *
     * @param file target directory
     * @return MD5-hash
     * @throws NoSuchAlgorithmException if md5-algorithm was not found
     * @throws FileNotFoundException    if some file was not found
     * @throws FilesNotRetrievedException    if fail to get inner files
     * @throws IOException if its occured during file reading
     */
    private static byte[] hashDirectory(File file) throws IOException, NoSuchAlgorithmException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("Directory was expected, but file was found");
        }
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(file.getName().getBytes());
        File[] files = file.listFiles();
        if (files == null) {
            throw new FilesNotRetrievedException("At directory: " + file.getAbsolutePath());
        }
        for (File f : files) {
            md5.update(hash(f));
        }
        return md5.digest();
    }

    /**
     * Run single-thread calculating of MD5-hash of given file
     *
     * @param file target file
     * @return MD5-hash
     * @throws NoSuchAlgorithmException if md5-algorithm was not found
     * @throws FileNotFoundException    if some file was not found
     * @throws FilesNotRetrievedException    if fail to get inner files
     * @throws IOException if its occured during file reading
     */
    public static byte[] hash(File file) throws IOException, NoSuchAlgorithmException {
        return file.isDirectory() ? hashDirectory(file) : hashFile(file);
    }

    /**
     * Run multi-thread calculating of MD5-hash
     * of given file using ForkJoinPool
     *
     * @param file target file
     * @return MD5-hash
     * @throws NoSuchAlgorithmException if md5-algorithm was not found
     * @throws FileNotFoundException    if some file was not found
     * @throws FilesNotRetrievedException    if fail to get inner files
     * @throws IOException if its occured during file reading
     */
    public static byte[] hashParallel(File file) throws IOException, NoSuchAlgorithmException {
        ForkJoinPool pool = new ForkJoinPool();
        byte[] result;
        try {
            result = pool.invoke(new ParallelHashCalculator(file));
        } catch (ForkJoinTaskException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else if (e.getCause() instanceof NoSuchAlgorithmException) {
                throw (NoSuchAlgorithmException) e.getCause();
            }
            throw e; // unknown error
        }
        return result;
    }

    /**
     * Special class which implement RecursiveTask to
     * run multi-thread calculations
     * Calculate MD5-hash of file or directory given in constructor
     */
    private static class ParallelHashCalculator extends RecursiveTask<byte[]> {

        private final File targetFile;

        private ParallelHashCalculator(File targetFile) {
            this.targetFile = targetFile;
        }

        @Override
        protected byte[] compute() {
            if (!targetFile.isDirectory()) {
                try {
                    return HashMD5.hashFile(targetFile);
                } catch (Exception e) {
                    throw new ForkJoinTaskException(e);
                }
            }
            MessageDigest md5 = null;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new ForkJoinTaskException(e);
            }
            md5.update(targetFile.getName().getBytes());
            File[] files = targetFile.listFiles();
            if (files == null) {
                throw new ForkJoinTaskException(new IOException("Cant retrieve files"));
            }
            ArrayList<ParallelHashCalculator> calculators = new ArrayList<>();
            for (File file : files) {
                ParallelHashCalculator calculator = new ParallelHashCalculator(file);
                calculator.fork();
                calculators.add(calculator);
            }
            for (ParallelHashCalculator calculator : calculators) {
                md5.update(calculator.join());
            }
            return md5.digest();
        }
    }

    /**
     * Exception, thrown if file.listFiles() returned null
     */
    public static class FilesNotRetrievedException extends IOException {
        private FilesNotRetrievedException(String message) {
            super(message);
        }
    }

    /**
     * Uncheckable exception to wrap exception which occur
     * during calculation in ForkJoinPool
     */
    private static class ForkJoinTaskException extends RuntimeException {
        private ForkJoinTaskException(Throwable cause) {
            super(cause);
        }
    }
}
