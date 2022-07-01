import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;


public class ThunderDecryptPic {
    public static final String J_EXT = "j";
    public static final String P_EXT = "p";
    public static final byte[] PNG_HEAD = {-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82};
    public static final byte[] PNG_END = {0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};
    public static final byte[] JPG_HEAD = {-1, -40};
    public static final byte[] JPG_END = {-1, -39};
    static byte[] fileData;
    static String out_dir = "E:/Thunder/images/";
    static String source_dir = "E:/Thunder/thunder/assets/CloverAssets/";
    static File out_dir_file = new File(out_dir);
    static File dir_file = new File(source_dir);
    static Path source_dir_path = Paths.get(source_dir);


    public static void main(String[] args) throws Exception {
        if (!out_dir_file.exists()) {
            out_dir_file.mkdir();
        }
        findFile(dir_file);

    }


    private static void findFile(File dir) throws Exception {
        try {
            File[] files = dir.listFiles();
            assert files != null;
            for (File temp : files) {
                if (temp.isDirectory()) {
                    findFile(temp);
                } else if (temp.getName().endsWith(".p")) {
                    FileInputStream fileInputStream = new FileInputStream(temp);
                    Path output_dir_path = Paths.get(temp.getPath());
                    Path temp_dir_path = source_dir_path.relativize(output_dir_path);
                    fileData = decryptPic(fileInputStream, P_EXT);
                    File temp_file = new File(out_dir_file.getPath() + "\\" + temp_dir_path + "ng");
                    if (!temp_file.exists()) {
                        temp_file.mkdirs();
                    }
                    ByteToFile(fileData, out_dir_file.getPath() + "\\" + temp_dir_path + "ng");
                } else if (temp.getName().endsWith(".j")) {
                    FileInputStream fileInputStream = new FileInputStream(temp);
                    fileData = decryptPic(fileInputStream, J_EXT);
                    Path output_dir_path = Paths.get(temp.getPath());
                    Path temp_dir_path = source_dir_path.relativize(output_dir_path);
                    File temp_file = new File(out_dir_file.getPath() + "\\" + temp_dir_path + "pg");
                    if (!temp_file.exists()) {
                        temp_file.mkdirs();
                    }
                    ByteToFile(fileData, out_dir_file.getPath() + "\\" + temp_dir_path + "pg");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void ByteToFile(byte[] bytes, String outPath) {
        File file = new File(outPath);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try (byteArrayInputStream) {
            BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);
            if (file.getName().endsWith(".png")) {
                ImageIO.write(bufferedImage, "png", file);
            } else if (file.getName().endsWith(".jpg")) {
                ImageIO.write(bufferedImage, "jpg", file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("decryptPic:" + file.getName());
        }
    }

    private static byte[] decryptPic(InputStream in, String inExt) throws Exception {
        byte[] headData;
        byte[] endData;
        if (in == null || inExt == null || inExt.length() == 0) {
            return null;
        }
        if (inExt.compareToIgnoreCase(J_EXT) == 0) {
            headData = JPG_HEAD;
            endData = JPG_END;
        } else {
            headData = PNG_HEAD;
            endData = PNG_END;
        }
        DataInputStream din = new DataInputStream(in);
        int len = din.readInt();
        int chunkSize = din.readShort();
        byte[] pngData = new byte[headData.length + len + endData.length];
        System.arraycopy(headData, 0, pngData, 0, headData.length);
        System.arraycopy(endData, 0, pngData, pngData.length - endData.length, endData.length);
        int blockNum = len / chunkSize;
        int oddSize = len % chunkSize;
        int off = headData.length + len;
        if (oddSize != 0) {
            off -= oddSize;
            din.read(pngData, off, oddSize);
        }
        for (int i = blockNum - 1; i >= 0; i--) {
            off -= chunkSize;
            din.read(pngData, off, chunkSize);
        }
        din.close();
        return pngData;
    }
}
