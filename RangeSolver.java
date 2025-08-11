import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
    private BufferedWriter writer;
    private String outputFileName;

    public static Range[] read(String fname) {
        Range ranges[] = new Range[1_000_000];
        try (BufferedReader reader = new BufferedReader(new FileReader(fname))) {
            String text = reader.readLine();
            int line = 0;
            while (text != null) {
                String r[] = text.split(" ");
                int start = Integer.parseInt(r[0]);
                int end = Integer.parseInt(r[1]);
                ranges[line] = new Range(start, end);
                text = reader.readLine();
                line++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ranges;
    }

    public void openForWriting(String fname) throws IOException {
        this.outputFileName = fname;
        this.writer = new BufferedWriter(new FileWriter(fname));
    }

    public void writeRange(Range range) throws IOException {
        if (writer == null) {
            throw new IllegalStateException("File not opened for writing. Call openForWriting() first.");
        }

        writer.write(String.format("%d %d %d %d",
                range.start, range.end, range.hp, range.hp_count));
        writer.newLine();
    }

    public void writeLine(String line) throws IOException {
        if (writer == null) {
            throw new IllegalStateException("File not opened for writing. Call openForWriting() first.");
        }
        writer.write(line);
        writer.newLine();
    }

    public void flush() throws IOException {
        if (writer != null) {
            writer.flush();
        }
    }

    public void close() throws IOException {
        if (writer != null) {
            writer.flush();
            writer.close();
            writer = null;
        }
    }

    public static void writeAll(String fname, Range[] ranges) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fname))) {
            for (Range range : ranges) {
                if (range == null)
                    break;
                writer.write(String.format("%d %d %d %d",
                        range.start, range.end, range.hp, range.hp_count));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Problem3XP1 {
    static int CACHE_NUMBERS[] = new int[1_000_000];

    public static int count(int n) {
        if (CACHE_NUMBERS[n] != 0)
            return CACHE_NUMBERS[n];

        int originalN = n;
        int c = 0;
        while (n > 1) {
            if (n < CACHE_NUMBERS.length && CACHE_NUMBERS[n] != 0) {
                c += CACHE_NUMBERS[n];
                break;
            }
            n = n % 2 == 0 ? n / 2 : n * 3 + 1;
            c++;
        }
        CACHE_NUMBERS[originalN] = c;
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
    static public final Range[] CACHED_RANGES = new Range[5000];

    public static void main(String[] args) {
        try {
            long start = System.nanoTime();
            Range r[] = FileManager.read("output.txt");
            long end = System.nanoTime();
            long duration = (end - start) / 1_000_000l;
            System.out.println("dur: " + duration + " ms");

            FileManager fm = new FileManager();
            fm.openForWriting("results.txt");
            start = System.nanoTime();
            RangeSolver.solve(r, fm);
            end = System.nanoTime();
            duration = (end - start) / 1_000_000l;
            System.out.println("dur: " + duration + " ms");
            fm.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void solve(Range[] ranges, FileManager fm) throws IOException {
        for (Range range : ranges) {
            if (range == null) {
                continue;
            }

            int startIndex = RangeSolver.getCachedRangeIndex(range.start),
                    endIndex = RangeSolver.getCachedRangeIndex(range.end),
                    segmentsCount = endIndex - startIndex + 1;

            if (segmentsCount == 1) {
                Range cachedRange = RangeSolver.getOrComputeCachedRange(startIndex);

                if (RangeSolver.isHighestPointInRange(range, cachedRange.hp)) {
                    range.hp = cachedRange.hp;
                    range.hp_count = cachedRange.hp_count;
                } else {
                    int[] result = calculateRange(range);
                    range.hp = result[0];
                    range.hp_count = result[1];
                }
            } else if (segmentsCount == 2) {
                Range startRange = RangeSolver.getOrComputeCachedRange(startIndex);
                Range endRange = RangeSolver.getOrComputeCachedRange(endIndex);
                if (RangeSolver.isHighestPointInRange(range, startRange.hp)
                        && RangeSolver.isHighestPointInRange(range, endRange.hp)) {
                    if (startRange.hp > endRange.hp) {
                        range.hp = startRange.hp;
                        range.hp_count = startRange.hp_count;
                    } else {
                        range.hp = endRange.hp;
                        range.hp_count = endRange.hp_count;
                    }
                } else if (RangeSolver.isHighestPointInRange(range, startRange.hp)
                        && !RangeSolver.isHighestPointInRange(range, endRange.hp)) {
                    Range dummyRange = new Range(endRange.start, range.end);
                    int[] result = calculateRange(dummyRange);
                    if (startRange.hp > result[0]) {
                        range.hp = startRange.hp;
                        range.hp_count = startRange.hp_count;
                    } else {
                        range.hp = result[0];
                        range.hp_count = result[1];
                    }
                } else if (!RangeSolver.isHighestPointInRange(range, startRange.hp)
                        && RangeSolver.isHighestPointInRange(range, endRange.hp)) {
                    Range dummyRange = new Range(range.start, startRange.end);
                    int[] result = calculateRange(dummyRange);
                    if (result[0] > endRange.hp) {
                        range.hp = result[0];
                        range.hp_count = result[1];
                    } else {
                        range.hp = endRange.hp;
                        range.hp_count = endRange.hp_count;
                    }
                }
            } else {
                int highestPoint = 0;
                int highestPointCount = 0;
                Range cachedRange;
                for (int i = startIndex + 1; i < endIndex; i++) {
                    cachedRange = getOrComputeCachedRange(i);
                    if (cachedRange.hp > highestPoint) {
                        highestPoint = cachedRange.hp;
                        highestPointCount = cachedRange.hp_count;
                    }
                }

                Range startRange = RangeSolver.getOrComputeCachedRange(startIndex);
                Range endRange = RangeSolver.getOrComputeCachedRange(endIndex);

                if (RangeSolver.isHighestPointInRange(range, startRange.hp)
                        && RangeSolver.isHighestPointInRange(range, endRange.hp)) {
                    if (startRange.hp > endRange.hp) {
                        if (startRange.hp > highestPoint) {
                            highestPoint = startRange.hp;
                            highestPointCount = startRange.hp_count;
                        }
                    } else {
                        if (endRange.hp > highestPoint) {
                            highestPoint = endRange.hp;
                            highestPointCount = endRange.hp_count;
                        }
                    }
                } else if (RangeSolver.isHighestPointInRange(range, startRange.hp)
                        && !RangeSolver.isHighestPointInRange(range, endRange.hp)) {
                    Range dummyRange = new Range(endRange.start, range.end);
                    int[] result = calculateRange(dummyRange);
                    if (startRange.hp > result[0]) {
                        if (startRange.hp > highestPoint) {
                            highestPoint = startRange.hp;
                            highestPointCount = startRange.hp_count;
                        }
                    } else {
                        if (result[0] > highestPoint) {
                            highestPoint = result[0];
                            highestPointCount = result[1];
                        }
                    }
                } else if (!RangeSolver.isHighestPointInRange(range, startRange.hp)
                        && RangeSolver.isHighestPointInRange(range, endRange.hp)) {
                    Range dummyRange = new Range(range.start, startRange.end);
                    int[] result = calculateRange(dummyRange);
                    if (result[0] > endRange.hp) {
                        if (result[0] > highestPoint) {
                            highestPoint = result[0];
                            highestPointCount = result[1];
                        }
                    } else {
                        if (endRange.hp > highestPoint) {
                            highestPoint = endRange.hp;
                            highestPointCount = endRange.hp_count;
                        }
                    }
                }
                range.hp = highestPoint;
                range.hp_count = highestPointCount;
            }

            fm.writeRange(range);
        }
    }

    public static int[] calculateRange(Range range) {

        int highestPoint = range.start;
        int highestPointCount = Problem3XP1.count(range.start);

        for (int i = range.start + 1; i <= range.end; i++) {
            int count = Problem3XP1.count(i);
            if (count > highestPointCount) {
                highestPointCount = count;
                highestPoint = i;
            }
        }

        return new int[] { highestPoint, highestPointCount };
    }

    public static int getCachedRangeIndex(int n) {
        return (n - 1) / 200;
    }

    public static boolean isHighestPointInRange(Range range, int highestPoint) {
        return highestPoint >= range.start && highestPoint <= range.end;
    }

    private static Range getOrComputeCachedRange(int index) {
        if (CACHED_RANGES[index] == null) {
            int start = index * 200 + 1;
            Range r = new Range(start, start + 199);
            int[] hpData = calculateRange(r);
            r.hp = hpData[0];
            r.hp_count = hpData[1];
            CACHED_RANGES[index] = r;
        }
        return CACHED_RANGES[index];
    }
}
