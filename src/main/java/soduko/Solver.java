package soduko;

import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Solver {
    public static boolean log = true;

    public static void main(String args[]) throws Exception{
//        SMatrix matrix = getMatrixEasy();
//        SMatrix matrix = getMatrixNormal();
        SMatrix matrix = SMatrixReader.read("soduko/extream1.txt");
        matrix.print();

        //first step, initial possible set
        initialCondition(matrix);

        checkSingleBlock(matrix);

        checkBlockSetInRow(matrix);
        checkBlockSetInColumn(matrix);

        checkSingleBlock(matrix);

        matrix.print();

    }

    private static void initialCondition(@NonNull final SMatrix matrix) {
        for (SMatrix.Block block : matrix.getBlocks()) {
            for (SMatrix.Cell cell : block.getCells()) {
                if (cell.getValue() == 0) {
                    cell.getPossibleNumbers().addAll(IntStream.rangeClosed(1, 9).boxed().collect(Collectors.toSet()));
                }
            }
        }

        for (int b = 1; b <= 9; b++) {
            SMatrix.Block block = matrix.getBlock(b);
            for (SMatrix.Cell cell : block.getCells()) {
                int v = cell.getValue();
                if (v == 0) {
                    continue;
                }
                filterBlock(block, v);
                filterRowColumn(matrix, b, cell);
            }
        }
    }

    static boolean checkBlockSetInRow(@NonNull final SMatrix matrix) {
        boolean update = false;
        for (Integer startb : List.of(1, 4, 7)) {
            List<SMatrix.Block> blocks = List.of(matrix.getBlock(startb), matrix.getBlock(startb + 1)
                    , matrix.getBlock(startb + 2));
            Set<Integer> values = getAllNumbets();
            values.removeAll(blocks.stream().flatMap(b -> b.getCells().stream())
                    .map(SMatrix.Cell::getValue)
                    .filter(v -> v != 0)
                    .collect(Collectors.toSet()));

            for (Integer value : values) {
                List<Set<Integer>> rowSets =
                        blocks.stream().map(b -> b.getCells().stream()
                                .filter(c -> c.getValue() == 0 && c.getPossibleNumbers().contains(value))
                                .map(SMatrix.Cell::getI).collect(Collectors.toSet()))
                                .collect(Collectors.toList());
                int[] array = checkSamePair(rowSets);
                if (array == null) {
                    continue;
                }
                Set<Integer> rows = rowSets.get(array[0]);
                int third = IntStream.range(0, 3).filter(i -> i != array[0] && i != array[1]).findFirst().orElse(-1);
                Set<Integer> otherRows = rowSets.get(third);
                if (hasIntersection(rows, otherRows)) {
                    update = true;
                    for (int row : rows) {
                        if (log) {
                            System.out.println("update for block sets(row):" + startb + " and number:" + value);
                        }
                        blocks.get(third).getCells().stream().filter(c -> c.getValue() == 0 && c.getI() == row)
                                .forEach(c -> c.getPossibleNumbers().remove(value));
                    }
                }
            }
        }
        return update;
    }

    static boolean checkBlockSetInColumn(@NonNull final SMatrix matrix) {
        boolean update = false;
        for (Integer startb : List.of(1, 2, 3)) {
            List<SMatrix.Block> blocks = List.of(matrix.getBlock(startb), matrix.getBlock(startb + 3)
                    , matrix.getBlock(startb + 6));

            Set<Integer> values = getAllNumbets();
            values.removeAll(blocks.stream().flatMap(b -> b.getCells().stream())
                    .map(SMatrix.Cell::getValue)
                    .filter(v -> v != 0)
                    .collect(Collectors.toSet()));
//            System.out.println(startb+":"+values);

            for (Integer value : values) {
                List<Set<Integer>> columnSets =
                        blocks.stream().map(b -> b.getCells().stream()
                                .filter(c -> c.getValue() == 0 && c.getPossibleNumbers().contains(value))
                                .map(SMatrix.Cell::getJ).collect(Collectors.toSet()))
                                .collect(Collectors.toList());
                int[] array = checkSamePair(columnSets);
                if (array == null) {
                    continue;
                }
                Set<Integer> columns = columnSets.get(array[0]);
                int third = IntStream.range(0, 3).filter(i -> i != array[0] && i != array[1]).findFirst().orElse(-1);
                Set<Integer> otherColumns = columnSets.get(third);
                if (hasIntersection(columns, otherColumns)) {
                    update = true;
                    for (int col : columns) {
                        if (log) {
                            System.out.println("update for block sets(column):" + startb + " and number:" + value);
                        }
                        blocks.get(third).getCells().stream().filter(c -> c.getValue() == 0 && c.getJ() == col)
                                .forEach(c -> c.getPossibleNumbers().remove(value));
                    }
                }
            }
        }
        return update;
    }

    private static <T> int[] checkSamePair(List<? extends Collection<T>> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            Collection<T> c1 = list.get(i);
            if (c1.size() > 2) {
                continue;
            }
            for (int j = i + 1; j < list.size(); j++) {
                Collection<T> c2 = list.get(j);
                if (c1.equals(c2)) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }


    static void checkSingleBlock(@NonNull final SMatrix matrix) {
        boolean update = true;
        while (update) {
            update = false;
            for (int b = 1; b <= 9; b++) {
                SMatrix.Block block = matrix.getBlock(b);

                //cell has only one possibility
                for (SMatrix.Cell c : block.getCells()) {
                    if (c.getValue() == 0 && c.getPossibleNumbers().size() == 1) {
                        update = true;
                        int v = c.getPossibleNumbers().iterator().next();
                        c.setValue(v);
                        c.getPossibleNumbers().clear();
                        filterRowColumn(matrix, b, c);
                        if (log) {
                            System.out.println("=========1. Fill Block:" + b + " with " + v + "=========");
                            matrix.print();
                        }
                    }
                }

                if (checkCellsWithSameP(matrix, block, b)) {
                    update = true;
                }

                Set<Integer> numbers = getAllNumbets();
                numbers.removeAll(block.getCells().stream().map(SMatrix.Cell::getValue)
                        .filter(value -> value != 0).collect(Collectors.toSet()));
                for (Integer v : numbers) {
//                    System.out.println("B:"+b+", V:"+v);
                    List<SMatrix.Cell> cells = block.getCells().stream()
                            .filter(c -> c.getValue() == 0 && c.getPossibleNumbers().contains(v)).collect(Collectors.toList());
                    if (cells.isEmpty()) {
                        System.err.println("Can not fill block:" + b + " with " + v);
                        matrix.print();
                        return;
                    }
                    if (cells.size() == 1) {
                        SMatrix.Cell cell = cells.get(0);
                        cell.setValue(v);
                        cell.getPossibleNumbers().clear();
                        filterRowColumn(matrix, b, cell);
                        update = true;
                        if (log) {
                            System.out.println("=========2. Fill Block:" + b + " with " + v + "=========");
                            matrix.print();
                        }
                        continue;
                    }

                    if (checkCellsOneLine(matrix, b, v, cells)) {
                        update = true;
                    }
                }
            }
            if (!update) {
                break;
            }
        }
    }

    private static boolean checkCellsWithSameP(final SMatrix matrix, final SMatrix.Block block, final int b) {
        //same cells has the same possibility
        Set<Set<Integer>> visited = new HashSet<>();
        boolean goOne = true;
        boolean update = false;
        while (goOne) {
            goOne = false;
            Map<Set<Integer>, List<SMatrix.Cell>> map = new HashMap<>();
            block.getCells().stream().filter(c -> c.value == 0)
                    .forEach(c -> map.computeIfAbsent(c.getPossibleNumbers(), k -> new ArrayList<>()).add(c));
            List<SMatrix.Cell> scells = map.entrySet().stream()
                    .filter(entry -> entry.getKey().size() == entry.getValue().size() && !visited.contains(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .findFirst().orElse(null);
            if (null != scells) {
                goOne = true;
                Set<Integer> pnumbers = scells.get(0).getPossibleNumbers();
                visited.add(pnumbers);
                Set<SMatrix.Cell> otherCells = block.getCells().stream().filter(c -> c.value == 0 && !scells.contains(c) &&
                        hasIntersection(pnumbers, c.getPossibleNumbers())).collect(Collectors.toSet());
                if (!otherCells.isEmpty()) {
                    otherCells.forEach(c -> c.getPossibleNumbers().removeAll(pnumbers));
                    update = true;
                }
                for (Integer v : pnumbers) {
                    if (checkCellsOneLine(matrix, b, v, scells)) {
                        update = true;
                    }
                }
            }
        }
        return update;
    }

    private static <T> boolean hasIntersection(Collection<T> set1, Collection<T> set2) {
        Set<T> set = new HashSet<>(set1);
        set.retainAll(set2);
        return !set.isEmpty();
    }

    private static boolean checkCellsOneLine(final SMatrix matrix, final int b, final int v, final List<SMatrix.Cell> cells) {
        boolean update = false;
        List<Integer> rows = cells.stream().map(SMatrix.Cell::getI).distinct().collect(Collectors.toList());
        if (rows.size() == 1) {
            int row = getIndex(b, cells.get(0))[0];
            if (filterRow(matrix, row, v, cells)) {
                update = true;
            }
        }

        List<Integer> columns = cells.stream().map(SMatrix.Cell::getJ).distinct().collect(Collectors.toList());
        if (columns.size() == 1) {
            int col = getIndex(b, cells.get(0))[1];
            if (filterColumn(matrix, col, v, cells)) {
                update = true;
            }
        }
        return update;
    }

    private static Set<Integer> getAllNumbets() {
        return IntStream.rangeClosed(1, 9).boxed().collect(Collectors.toSet());
    }

    private static void filterBlock(final SMatrix.Block block, final int v) {
        block.getCells().stream().filter(c -> c.getValue() == 0).forEach(c -> c.getPossibleNumbers().remove(v));
    }

    private static void filterRowColumn(final SMatrix matrix, final int block, final SMatrix.Cell cell) {
        int[] index = getIndex(block, cell);
        filterRow(matrix, index[0], cell.getValue());
        filterColumn(matrix, index[1], cell.getValue());
    }

    private static void filterRow(final SMatrix matrix, final int row, final int v) {
        matrix.getRow(row).stream().filter(c -> c.getValue() == 0).forEach(c -> c.getPossibleNumbers().remove(v));
    }

    private static boolean filterRow(final SMatrix matrix, final int row, final int v, final Collection<SMatrix.Cell> excludes) {
        Set<SMatrix.Cell> cells = matrix.getRow(row).stream()
                .filter(c -> c.getValue() == 0 && !excludes.contains(c) && c.getPossibleNumbers().contains(v))
                .collect(Collectors.toSet());
        if (!cells.isEmpty()) {
            cells.forEach(c -> c.getPossibleNumbers().remove(v));
            return true;
        }
        return false;
    }

    private static void filterColumn(final SMatrix matrix, final int col, final int v) {
        matrix.getColumn(col).stream().filter(c -> c.getValue() == 0).forEach(c -> c.getPossibleNumbers().remove(v));
    }

    private static boolean filterColumn(final SMatrix matrix, final int col, final int v, final Collection<SMatrix.Cell> excludes) {
        Set<SMatrix.Cell> cells = matrix.getColumn(col).stream()
                .filter(c -> c.getValue() == 0 && !excludes.contains(c) && c.getPossibleNumbers().contains(v))
                .collect(Collectors.toSet());
        if (cells.isEmpty()) {
            return false;
        }
        cells.forEach(c -> c.getPossibleNumbers().remove(v));
        return true;
    }

    private static int[] getIndex(final int b, final SMatrix.Cell cell) {
        int i = (b - 1) / 3 * 3 + cell.getI();
        int j = (b - 1) % 3 * 3 + cell.getJ();
        return new int[]{i, j};
    }

    //easy
    static SMatrix getMatrixEasy() {
        SMatrix matrix = new SMatrix();

        SMatrix.Block b1 = matrix.getBlock(1);
        b1.get(1, 2).setValue(8);
        b1.get(1, 3).setValue(3);

        SMatrix.Block b3 = matrix.getBlock(3);
        b3.get(1, 1).setValue(9);
        b3.get(2, 2).setValue(1);
        b3.get(2, 3).setValue(5);
        b3.get(3, 1).setValue(3);

        SMatrix.Block b4 = matrix.getBlock(4);
        b4.get(1, 1).setValue(8);
        b4.get(1, 2).setValue(2);
        b4.get(3, 2).setValue(4);

        SMatrix.Block b5 = matrix.getBlock(5);
        b5.get(1, 2).setValue(6);
        b5.get(2, 1).setValue(8);
        b5.get(3, 1).setValue(7);
        b5.get(3, 2).setValue(3);

        SMatrix.Block b6 = matrix.getBlock(6);
        b6.get(2, 3).setValue(4);

        SMatrix.Block b7 = matrix.getBlock(7);
        b7.get(1, 2).setValue(7);
        b7.get(2, 3).setValue(5);
        b7.get(3, 2).setValue(6);

        SMatrix.Block b8 = matrix.getBlock(8);
        b8.get(1, 1).setValue(4);
        b8.get(2, 2).setValue(8);
        b8.get(3, 3).setValue(9);

        SMatrix.Block b9 = matrix.getBlock(9);
        b9.get(1, 1).setValue(8);
        b9.get(1, 3).setValue(9);
        b9.get(2, 3).setValue(2);
        b9.get(3, 2).setValue(7);
        return matrix;
    }

    static SMatrix getMatrixNormal() {
        SMatrix matrix = new SMatrix();

        SMatrix.Block b1 = matrix.getBlock(1);
        b1.get(1, 1).setValue(5);
        b1.get(1, 2).setValue(9);
        b1.get(1, 3).setValue(7);
        b1.get(2, 2).setValue(6);

        SMatrix.Block b2 = matrix.getBlock(2);
        b2.get(1, 1).setValue(6);
        b2.get(3, 2).setValue(1);
        b2.get(3, 3).setValue(5);

        SMatrix.Block b3 = matrix.getBlock(3);
        b3.get(3, 3).setValue(7);

        SMatrix.Block b4 = matrix.getBlock(4);
        b4.get(2, 2).setValue(4);
        b4.get(3, 2).setValue(3);

        SMatrix.Block b5 = matrix.getBlock(5);
        b5.get(2, 3).setValue(9);
        b5.get(3, 1).setValue(1);


        SMatrix.Block b6 = matrix.getBlock(6);
        b6.get(1, 3).setValue(4);
        b6.get(2, 3).setValue(8);
        b6.get(3, 2).setValue(5);

        SMatrix.Block b7 = matrix.getBlock(7);
        b7.get(1, 1).setValue(9);
        b7.get(1, 3).setValue(3);
        b7.get(3, 1).setValue(2);
        b7.get(3, 2).setValue(5);

        SMatrix.Block b8 = matrix.getBlock(8);
        b8.get(1, 2).setValue(6);
        b8.get(2, 1).setValue(7);
        b8.get(2, 2).setValue(5);
        b8.get(2, 3).setValue(8);

        SMatrix.Block b9 = matrix.getBlock(9);
        b9.get(1, 3).setValue(1);
        b9.get(2, 3).setValue(2);
        b9.get(3, 1).setValue(7);
        return matrix;
    }
}
