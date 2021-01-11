package soduko;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
public class SMatrix {
    List<Block> blocks = new ArrayList<>(9);

    public SMatrix() {
        for (int i = 0; i < 9; i++) {
            blocks.add(new Block());
        }
    }

    @Override
    public SMatrix clone() {
        SMatrix another = new SMatrix();
        another.blocks.clear();
        blocks.stream().map(Block::clone).forEach(another.blocks::add);
        return another;
    }

    public Block getBlock(final int i) {
        if (i < 1 || i > 9) {
            System.err.println("Wrong block index: " + i);
            System.err.println("please give correct block index [1,9]");
            return null;
        }
        return blocks.get(i - 1);
    }

    public List<Cell> getColumn(final int i) {
        if (i < 1 || i > 9) {
            System.err.println("Wrong column index: " + i);
            System.err.println("please give correct column index [1,9]");
            return null;
        }
        List<Cell> results = new ArrayList<>();
        int c = i % 3;
        if (c == 0) {
            c = 3;
        }
        int b = (i - 1) / 3 + 1;
        for (; b <= 9; b += 3) {
            for (int r = 1; r <= 3; r++) {
                results.add(getBlock(b).get(r, c));
            }
        }
        return results;
    }

    public List<Cell> getRow(final int i) {
        if (i < 1 || i > 9) {
            System.err.println("Wrong row index: " + i);
            System.err.println("please give correct row index [1,9]");
            return null;
        }
        List<Cell> results = new ArrayList<>();

        int r = i % 3;
        if (r == 0) {
            r = 3;
        }
        int b = (i - 1) / 3 * 3+ 1;
        int end = b+2;
        for(;b<=end;b++){
            Block block = getBlock(b);
            for(int c=1;c<=3;c++){
                results.add(block.get(r,c));
            }
        }
        return results;
    }

    public void print() {
        System.out.println(getPrintLine());
        for (int b = 0; b < 3; b++) {
            List<Block> subblocks = blocks.subList(b * 3, b * 3 + 3);
            for (int i = 1; i <= 3; i++) {
                System.out.print("| ");
                for (Block block : subblocks) {
                    for (int j = 1; j <= 3; j++) {
                        System.out.print(block.get(i, j).getValue());
                        System.out.print(" ");
                    }
                    System.out.print("| ");
                }
                System.out.println();
            }
            System.out.println(getPrintLine());
        }
    }

    private String getPrintLine() {
        return IntStream.rangeClosed(1, 25).mapToObj(i -> "-").collect(Collectors.joining());
    }

    @Data
    static class Block {
        List<Cell> cells = new ArrayList<>(9);

        public Block() {
            for (int i = 1; i <= 3; i++) {
                for (int j = 1; j <= 3; j++) {
                    cells.add(new Cell(i, j));
                }
            }
        }

        @Override
        public Block clone() {
            Block another = new Block();
            another.cells.clear();
            this.getCells().stream().map(Cell::new).forEach(another.getCells()::add);
            return another;
        }

        public Cell get(final int i, final int j) {
            if (i > 3 || i < 1 || j > 3 || j < 1) {
                System.err.println("Wrong Cell index: " + i + ", " + j);
                System.err.println("pleas give correct cell i/j index: 1, 2, 3");
                return null;
            }
            return cells.get((i - 1) * 3 + j - 1);
        }
    }

    @Data
    @RequiredArgsConstructor
    static class Cell {
        final int i;
        final int j;
        int value = 0;
        Set<Integer> possibleNumbers = new HashSet<>();

        public Cell(@NonNull final Cell cell) {
            this.i = cell.i;
            this.j = cell.j;
            this.value = cell.value;
        }
    }
}
