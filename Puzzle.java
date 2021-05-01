import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Puzzle {

    public int size; //row and column length
    public char[][] matrix; //store letters
    public char[][] transpose; //transpose of matrix
    public char[][] reverse_m; //matrix with reversed rows
    private final ArrayList<ArrayList<Character>> r_down_diagonals; //down diagonal of matrix
    private final ArrayList<ArrayList<Character>> up_diagonals; //up diagonals of matrix

    /**
     * Read and store puzzle text file into matrix, transpose,
     * and reverse_m. Find the diagonals of the reverse_m and matrix.
     */
    public Puzzle(String fn) throws FileNotFoundException {
        Scanner scan = new Scanner(new File(fn));
        size = Integer.parseInt(scan.nextLine());
        matrix = new char[size][size];
        transpose = new char[size][size];
        reverse_m = new char[size][size];
        r_down_diagonals = new ArrayList<>();
        up_diagonals = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String line = scan.nextLine();
            String reverse = new StringBuilder(line).reverse().toString();
            for (int j = 0; j < line.length(); j++) {
                matrix[i][j] = line.charAt(j);
                transpose[j][i] = line.charAt(j);
                reverse_m[i][j] = reverse.charAt(j);
            }
        }
        get_diagonals(reverse_m, true);
        get_diagonals(matrix, false);
    }

    /**
     * Helper function to find the up diagonals. If the type
     * is true, we are dealing with a reverse_m matrix and adding diagonal
     * values to r_down_diagonals. Since reverse_m is the reverse row of the
     * original matrix, finding the up diagonals will of reverse_m will give
     * us the down diagonals of the original matrix.
     */
    private void get_diagonals(char[][] matrix, boolean type) {
        for (int i = 0; i < size*2 - 1; i++) {
            ArrayList<Character> diagonal = new ArrayList<>();
            for (int j = 0; j <= i; j++) {
                if (i - j < size && j < size) {
                    diagonal.add(matrix[i - j][j]);
                }
            }
            if (type) {
                r_down_diagonals.add(diagonal);
            } else {
                up_diagonals.add(diagonal);
            }
        }
    }

    /**
     * Search the puzzle for the given word and return
     * {a, b, x, y} where (a, b) is the starting point
     * and (x, y) is the ending point. Return null if
     * there is no word in the puzzle.
     */
    public int[] search(String word) {
        String reverse = new StringBuilder(word).reverse().toString();
        for (int i = 0; i < size; i++) {
            // horizontal word
            int b = boyer_moore(word, matrix[i]);
            if (b != -1) {
                return new int[] {i, b, i, b + word.length() - 1};
            }
            // horizontal reverse
            b = boyer_moore(reverse, matrix[i]);
            if (b != -1) {
                return new int[] {i, b + word.length() - 1, i, b};
            }
            // vertical word
            b = boyer_moore(word, transpose[i]);
            if (b != -1) {
                return new int[] {b, i, b + word.length() - 1, i};
            }
            // vertical reverse
            b = boyer_moore(reverse, transpose[i]);
            if (b != -1) {
                return new int[] {b + word.length() - 1, i, b, i};
            }
        }
        int count = 0;
        boolean subtract = false;
        // diagonal up
        for (ArrayList<Character> up_diagonal : up_diagonals) {
            if (count == size) {
                subtract = true;
                count = 1;
            }
            char[] line = arrayList_to_char(up_diagonal);
            int b = boyer_moore(word, line);
            if (b != -1) {
                if (subtract) {
                    return new int[] {size - 1 - b, count + b, size - 1 - b - word.length() + 1, count + b + word.length() - 1};
                }
                return new int[] {count - b, b, count - b - word.length() + 1, b + word.length() - 1};
            }
            count++;
        }
        // diagonal down
        count = 0;
        subtract = false;
        for (ArrayList<Character> down_diagonal : r_down_diagonals) {
            if (count >= size -1) {
                subtract = true;
            }
            char[] line = arrayList_to_char(down_diagonal);
            int b = boyer_moore(reverse, line);
            if (b != -1) {
                int x1 = count - b;
                int y1 = size - 1 - b;
                if (subtract) {
                    return new int[] {y1 - word.length() + 1, x1 - word.length() + 1, y1, x1};
                }
                return new int[] {x1 - word.length() + 1, y1 - word.length() + 1, x1, y1};
            }
            if (subtract) {
                count--;
            } else {
                count++;
            }
        }
        // diagonal down reverse
        count = 0;
        subtract = false;
        for (ArrayList<Character> down_diagonal : r_down_diagonals) {
            if (count >= size -1) {
                subtract = true;
            }
            char[] line = arrayList_to_char(down_diagonal);
            int b = boyer_moore(word, line);
            if (b != -1) {
                int x1 = count - b;
                int y1 = size - 1 - b;
                if (subtract) {
                    return new int[] {y1, x1, y1 - word.length() + 1, x1 - word.length() + 1};
                }
                return new int[] {x1, y1, x1 - word.length() + 1, y1 - word.length() + 1};
            }
            if (subtract) {
                count--;
            } else {
                count++;
            }
        }
        // diagonal up reverse
        count = 0;
        subtract = false;
        for (ArrayList<Character> up_diagonal : up_diagonals) {
            if (count == size) {
                subtract = true;
                count = 1;
            }
            char[] line = arrayList_to_char(up_diagonal);
            int b = boyer_moore(reverse, line);
            if (b != -1) {
                if (subtract) {
                    return new int[] {size - 1 - b - word.length() + 1, count + b + word.length() - 1, size - 1 - b, count + b};
                }
                return new int[] {count - b - word.length() + 1, b + word.length() - 1, count - b, b};
            }
            count++;
        }
        return null;
    }

    /**
     * Helper function to convert an char ArrayList to char[],
     */
    private char[] arrayList_to_char(ArrayList<Character> diagonal) {
        char[] line = new char[diagonal.size()];
        for (int i = 0; i < diagonal.size(); i++) {
            line[i] = diagonal.get(i);
        }
        return line;
    }

    /**
     * Helper function for Boyer Moore algorithm to find the last
     * occurrence of a letter in pattern. All other letters in the
     * alphabet will be given values of -1.
     */
    private HashMap<Character, Integer> last_occurrence(char[] text, String pattern) {
        HashMap<Character, Integer> lo = new HashMap<>();
        for (char c : text) {
            lo.put(c, -1);
        }
        for (int i = 0; i < pattern.length(); i++) {
            lo.put(pattern.charAt(i), i);
        }
        return lo;
    }

    /**
     * Boyer Moore algorithm to find a word in a line and
     * return its starting index within the line. Return -1
     * if there is no word found in the line.
     */
    private int boyer_moore(String word, char[] line) {
        HashMap<Character, Integer> lo = last_occurrence(line, word);
        int n = line.length;
        int m = word.length();
        int i = m - 1;
        int j = m - 1;
        while (i <= n - 1) {
            if (line[i] == word.charAt(j)) {
                if (j == 0) {
                    return i;
                } else {
                    i -= 1;
                    j -= 1;
                }
            } else {
                int l = lo.get(line[i]);
                i = i + m - Math.min(j, 1 + l);
                j = m - 1;
            }
        }
        return -1;
    }

    public static void main(String[] args) throws FileNotFoundException {
        Puzzle puzzle_1 = new Puzzle("puzzle1_test.txt");
        Puzzle puzzle_2 = new Puzzle("puzzle2_test.txt");
        Puzzle puzzle_3 = new Puzzle("puzzle3_test.txt");
        System.out.println(Arrays.toString(puzzle_1.search("RED")));
        System.out.println(Arrays.toString(puzzle_2.search("APPLE")));
        System.out.println(Arrays.toString(puzzle_3.search("ALBANY")));
    }

}