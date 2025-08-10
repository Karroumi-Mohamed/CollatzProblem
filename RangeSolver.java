import java.io.BufferedReader;
import java.io.FileReader;

class Range {
    public int start;
    public int end;
    public int hp;
    public int hp_count;
    public Range(int start, int end) {
        this.start = start;
        this.end = end;
    }
}

class FileManager {
    public static Range[] read(String fname) {
        Range ranges[] = new Range[1_000_000];

        try (BufferedReader reader = new BufferedReader(new FileReader(fname))) {
            String text = reader.readLine();
            int line = 0;
            while (text != null) {
                String r[] = text.split(" ");

                int start = Integer.parseInt(r[0]);
                int end = Integer.parseInt(r[1]);
                ranges[line]= new Range(start, end);

                text = reader.readLine();
                line++;
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ranges;
    }
}

class Problem3XP1{
    static int CACHE_NUMBERS[] = new int[1_000_000];
    public static int count(int n){
        if (CACHE_NUMBERS[n] != 0) {
           return CACHE_NUMBERS[n];
        }
        int c = 0;
        while (n > 1) {
            n = n % 2 == 0 ? n / 2 : n * 3 + 1;
            c++;
        }
        CACHE_NUMBERS[n] = c;
        return c;
    }
    public static float scanCacheLevel() {
        int empty = 0;
        for (int i = 0; i < CACHE_NUMBERS.length; i++) {
            if (CACHE_NUMBERS[i] == 0) {
                empty++;
            }
        }
        return (float) empty / CACHE_NUMBERS.length;
    }
}

public class RangeSolver {
    public static void main(String[] args) {
        long start = System.nanoTime();
        Range r[] = FileManager.read("output.txt");
        long end = System.nanoTime();
        long duration = (end - start) / 1_000_000l;
        System.out.println("dur: "+ duration + " ms");

        start = System.nanoTime();
        RangeSolver.solve(r);
        end = System.nanoTime();
        duration = (end - start) / 1_000_000l;
        System.out.println("dur: "+ duration + " ms");
    }
    public static void solve(Range[] ranges){
        int rangeCount = 1;
        for (Range range : ranges) {
            int numberWithMostSteps = 0;
            int highestStepCount = 0;
           for (int i = range.start; i <= range.end; i++) {
                int currentHp = Problem3XP1.count(i);
                if (currentHp >= numberWithMostSteps) {
                    numberWithMostSteps = i;
                    highestStepCount = currentHp;
                }
            }

            range.hp = numberWithMostSteps;
            range.hp_count = highestStepCount;
            rangeCount++;
        }
        System.out.println("Cash emptyb in %: " + Problem3XP1.scanCacheLevel() * 100 + "%");
    }
}
