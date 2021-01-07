package soduko;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SMatrix {
    List<Block> blocks = new ArrayList<>(9);

    public SMatrix() {
        for (int i = 0; i < 9; i++) {
            blocks.add(new Block());
        }
    }

    @Override
    public SMatrix clone(){
        SMatrix another = new SMatrix();
        another.blocks.clear();
        blocks.stream().map(Block::clone).forEach(another.blocks::add);
        return another;
    }

    public Block getBlock(final int i) {
        if (i < 1 || i > 9) {
            System.err.println("Wrong block index: "+i);
            System.err.println("please give correct block index [1,9]");
            return null;
        }
        return blocks.get(i-1);
    }

    public void print() {
        System.out.println(getPrintLine());
        for (int b = 0; b < 3; b++) {
            List<Block> subblocks = blocks.subList(b * 3, b * 3 + 3);
            for (int i = 1; i <= 3; i++) {
                System.out.print("| ");
                for(Block block: subblocks) {
                    for (int j = 1; j <= 3; j++) {
                        System.out.print(block.get(i,j).getValue());
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
        public Block clone(){
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

        public Cell(@NonNull final Cell cell){
            this.i  = cell.i;
            this.j = cell.j;
            this.value = cell.value;
        }
    }
}
