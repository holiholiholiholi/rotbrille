package soduko;

import lombok.NonNull;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.List;

public final class SMatrixReader {
    public static void main(String args[]) throws IOException{
        SMatrix sMatrix = SMatrixReader.read("soduko/extream1.txt");
        if(null != sMatrix){
            sMatrix.print();
        }
    }

    public static SMatrix read(@NonNull final String file) throws IOException {
        List<String> lines = IOUtils.readLines(utils.IOUtils.openStream(file), "utf8");
        SMatrix matrix = new SMatrix();

        int row = 0;
        for (String l : lines) {
            l = l.trim();
            if (l.length() < 9) {
                System.err.println("Error in line:" + row + "! not enough numbers!");
                return null;
            }
            for (int col = 0; col < 9; col++) {
                int[] indexArray = getIndex(row, col);
                try {
                    int v = Integer.parseInt(l.charAt(col) + "");
                    matrix.getBlock(indexArray[0]).get(indexArray[1], indexArray[2]).setValue(v);
                } catch (Exception e) {
                    System.err.println("Can not convert character: " + l.charAt(col) + ", line:" + row + ", column:" + col);
                    return null;
                }
            }
            row++;
            if (row >= 9) {
                break;
            }
        }
        if (row < 9) {
            System.err.println("Error! Only " + row + " lines! (at least 9)");
            return null;
        }
        return matrix;
    }

    private static int[] getIndex(final int r, final int c) {
        int i = r % 3 + 1;
        int j = c % 3 + 1;
        int b = r / 3 * 3 + 1 + c / 3;
        return new int[]{b, i, j};
    }
}
