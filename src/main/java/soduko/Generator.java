package soduko;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Generator {
    static boolean pringLog = false;

    public static void main(String args[]) {
//        SMatrix sMatrix = new SMatrix();
//        sMatrix.getBlock(7).get(3,2).setValue(9);
//        sMatrix.getBlock(6).get(3,3).setValue(9);
//        sMatrix.print();

//        SMatrix sMatrix = generate();
//        if(null != sMatrix) {
//            sMatrix.print();
//        }

        //calculate the precision,currently around 54%
//        int times = 10000;
//        int counter = 0;
//        int i = 0;
//        while (i++ < times) {
//            if (generate() != null) {
//                counter++;
//            }
//        }
//        System.out.println("Tried " + times + " times and generate " + counter + " matrix." +
//                " Precision:" + (double) counter / (double) times);

        SMatrix sMatrix = getSMatrix();
        sMatrix.print();
    }

    public static SMatrix getSMatrix(){
        SMatrix matrix;
        int counter = 1;
        while(null == (matrix = generate())){
            counter++;
        }
        System.out.println("Tried "+counter+" times!");
        return matrix;
    }

    public static SMatrix generate() {
        SMatrix sMatrix = new SMatrix();

        for (int i = 1; i <= 9; i++) {
            List<Set<Integer>> usedIsets = IntStream.rangeClosed(1, 3)
                    .mapToObj(HashSet<Integer>::new)
                    .collect(Collectors.toList());
            List<Set<Integer>> usedJsets = IntStream.rangeClosed(1, 3)
                    .mapToObj(HashSet<Integer>::new)
                    .collect(Collectors.toList());
            Set<Integer> filled = new HashSet<>();

            for (int b = 1; b <= 9; b++) {
                if (filled.contains(b)) {
                    continue;
                }

                if (!fillSingle(sMatrix, i, usedIsets, usedJsets, filled)) {
                    if (pringLog) {
                        System.err.println("Can not fill the number " + i + " in Block:" + b + ", because single/conflict problem!");
                        sMatrix.print();
                    }
                    return null;
                }
                if (filled.contains(b)) {
                    continue;
                }
//                System.out.println("Block:" + b + "-- " + usedI + usedJ);
                SMatrix.Block block = sMatrix.getBlock(b);

                List<SMatrix.Cell> cells = getPossibleCells(block, b, usedIsets, usedJsets);
                if (cells.isEmpty()) {
                    if (pringLog) {
                        System.err.println("Could not find possible possition for number:" + i + " in Block:" + b);
                        sMatrix.print();
                    }
                    return null;
                }
                Collections.shuffle(cells);
                for (SMatrix.Cell cell : cells) {
                    if (check(sMatrix, i, b, cell, usedIsets, usedJsets, filled)) {
                        cell.setValue(i);
                        int[] indexes = getFilledIndex(b);
                        Set<Integer> usedI = usedIsets.get(indexes[0]);
                        Set<Integer> usedJ = usedJsets.get(indexes[1]);
                        usedI.add(cell.getI());
                        usedJ.add(cell.getJ());
                        filled.add(b);
                        break;
                    }
                }
                if (!filled.contains(b)) {
                    if (pringLog) {
                        System.err.println("Filled to fill number: " + i + " in Block:" + b);
                        sMatrix.print();
                    }
                    return null;
                }
            }
        }

        return sMatrix;
    }

    private static boolean check(SMatrix sMatrix, int number,
                                 int b, SMatrix.Cell cell,
                                 List<Set<Integer>> usedIsets, List<Set<Integer>> usedJsets,
                                 Set<Integer> filled) {
        int[] indexes = getFilledIndex(b);
        List<Set<Integer>> newUsedISets = usedIsets.stream().map(HashSet::new).collect(Collectors.toList());
        List<Set<Integer>> newUsedJSets = usedJsets.stream().map(HashSet::new).collect(Collectors.toList());
        newUsedISets.get(indexes[0]).add(cell.getI());
        newUsedJSets.get(indexes[1]).add(cell.getJ());
        for (int i = 1; i <= 9; i++) {
            if (filled.contains(i) || i == b) {
                continue;
            }
            List<SMatrix.Cell> cells = getPossibleCells(sMatrix.getBlock(i), i, newUsedISets, newUsedJSets);
            if (cells.isEmpty()) {
                return false;
            }
        }
        SMatrix matrix2 = sMatrix.clone();
        matrix2.getBlock(b).get(cell.i, cell.j).setValue(number);
//        System.out.println("originial+clone");
//        sMatrix.print();
//        matrix2.print();
        Set<Integer> filled2 = new HashSet<>(filled);
        filled2.add(b);
        if (!fillSingle(matrix2, number, newUsedISets, newUsedJSets, filled2)) {
            return false;
        }
        return true;
    }

    private static boolean fillSingle(SMatrix sMatrix, int number,
                                      List<Set<Integer>> usedIsets, List<Set<Integer>> usedJsets,
                                      Set<Integer> filled) {
        boolean hasSingle = true;
        while (hasSingle) {
            hasSingle = false;
            for (int b = 1; b <= 9; b++) {
                if (filled.contains(b)) {
                    continue;
                }
                SMatrix.Block block = sMatrix.getBlock(b);
                List<SMatrix.Cell> cells = getPossibleCells(block, b, usedIsets, usedJsets);
                if (cells.size() == 1) {
                    hasSingle = true;
                    SMatrix.Cell cell = cells.get(0);
                    if (!check(sMatrix, number, b, cell, usedIsets, usedJsets, filled)) {
//                        System.err.println("Can not fill the number " + number + " in Block: " + b + " because single/conflict problem!");
                        return false;
                    }

                    int[] indexes = getFilledIndex(b);
                    Set<Integer> usedI = usedIsets.get(indexes[0]);
                    Set<Integer> usedJ = usedJsets.get(indexes[1]);
                    cell.setValue(number);
                    usedI.add(cell.getI());
                    usedJ.add(cell.getJ());
                    filled.add(b);
                }
            }
        }
        return true;
    }

    private static int[] getFilledIndex(final int b) {
        return new int[]{(b - 1) / 3, b % 3 == 0 ? 2 : b % 3 - 1};
    }

    private static List<SMatrix.Cell> getPossibleCells(final SMatrix.Block block, final int b,
                                                       final List<Set<Integer>> usedIsets,
                                                       final List<Set<Integer>> usedJsets) {
        int[] indexes = getFilledIndex(b);
        Set<Integer> usedI = usedIsets.get(indexes[0]);
        Set<Integer> usedJ = usedJsets.get(indexes[1]);
        return block.getCells().stream().filter(c -> c.value == 0)
                .filter(c -> !usedI.contains(c.getI()) && !usedJ.contains(c.getJ()))
                .collect(Collectors.toList());
    }
}
