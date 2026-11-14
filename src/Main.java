import java.io.IOException;
import java.io.InputStream;

public class Main {
    static int N;
    static long k;
    static int[][] grid;
    static long[][] preCachedGridSum;

    static class FastScanner {
        private final InputStream stream;
        private final byte[] buf = new byte[8192];
        private int head, tail;

        public FastScanner(InputStream stream) {
            this.stream = stream;
        }

        private int read() throws IOException {
            if (head >= tail) {
                head = 0;
                tail = stream.read(buf, 0, buf.length);
                if (tail <= 0) return -1;
            }
            return buf[head++];
        }

        public int nextInt() throws IOException {
            int c = read();
            while (c <= 32) {
                if (c == -1) return -1;
                c = read();
            }
            int res = 0;
            while (c > 32) {
                res = res * 10 + c - '0';
                c = read();
            }
            return res;
        }
    }

    public static void main(String[] args) throws IOException {
        FastScanner in = new FastScanner(System.in);

        N = in.nextInt();
        k = in.nextInt();

        grid = new int[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                grid[i][j] = in.nextInt();
            }
        }

        preCachedGridSum = new long[N][N];
        for (int i = 0; i < N; i++) {
            preCachedGridSum[i][0] = grid[i][0];
            for (int j = 1; j < N; j++) {
                preCachedGridSum[i][j] = preCachedGridSum[i][j - 1] + grid[i][j];
            }
        }
        for (int j = 0; j < N; j++) {
            for (int i = 1; i < N; i++) {
                preCachedGridSum[i][j] = preCachedGridSum[i - 1][j] + preCachedGridSum[i][j];
            }
        }

        Region region = HistogramConverter();
        if (region != null) {
            System.out.println(region);
        } else {
            System.out.println("NIST");
        }
    }

    record Region(int x1, int y1, int x2, int y2) {
        @Override
        public String toString() {
            return (x1 + 1) + " " + (y1 + 1) + " " + (x2 + 1) + " " + (y2 + 1);
        }
    }

    static long gridSum(Region region) {
        int x1 = region.x1, y1 = region.y1, x2 = region.x2, y2 = region.y2;
        long total = preCachedGridSum[x2][y2];
        long top = (x1 > 0) ? preCachedGridSum[x1 - 1][y2] : 0;
        long left = (y1 > 0) ? preCachedGridSum[x2][y1 - 1] : 0;
        long topLeft = (x1 > 0 && y1 > 0) ? preCachedGridSum[x1 - 1][y1 - 1] : 0;
        return total - top - left + topLeft;
    }

    static Region bruteForce() {
        for (int x1 = 0; x1 < N; x1++) {
            for (int y1 = 0; y1 < N; y1++) {
                for (int x2 = x1; x2 < N; x2++) {
                    for (int y2 = y1; y2 < N; y2++) {
                        long sum = gridSum(new Region(x1, y1, x2, y2));
                        if (k <= sum && sum <= 2 * k) {
                            return new Region(x1, y1, x2, y2);
                        }
                    }
                }
            }
        }

        return null;
    }

    static Region HistogramConverter() {
        // Find single-cell valid answers
        for (int x = 0; x < N; x++) {
            for (int y = 0; y < N; y++) {
                int cellValue = grid[x][y];
                if (k <= cellValue && cellValue <= 2 * k) {
                    return new Region(x, y, x, y);
                }
            }
        }

        // Find single-row valid subrectangles using a sliding window
        for (int x = 0; x < N; x++) {
            int left = 0;
            long sum = 0;
            for (int right = 0; right < N; right++) {
                if (grid[x][right] > 2 * k) {
                    left = right + 1;
                    sum = 0;
                } else {
                    sum += grid[x][right];
                    while (sum > 2 * k && left <= right) {
                        sum -= grid[x][left];
                        left++;
                    }
                    if (k <= sum && sum <= 2 * k) {
                        return new Region(x, left, x, right);
                    }
                }
            }
        }

        // Find maximal subrectangles using histogram conversion
        int[] height = new int[N];
        int[] leftBound = new int[N];
        int[] rightBound = new int[N];
        int[] stack = new int[N];
        int top;

        for (int x = 0; x < N; x++) {
            for (int y = 0; y < N; y++) {
                height[y] = grid[x][y] < k ? height[y] + 1 : 0;
            }

            // Nearest smaller bar to the left for each column
            top = -1;
            for (int y = 0; y < N; y++) {
                while (top >= 0 && height[stack[top]] >= height[y]) {
                    top--;
                }
                leftBound[y] = (top == -1) ? 0 : stack[top] + 1;
                stack[++top] = y;
            }

            // Nearest smaller bar to the right for each column
            top = -1;
            for (int y = N - 1; y >= 0; y--) {
                while (top >= 0 && height[stack[top]] >= height[y]) {
                    top--;
                }
                rightBound[y] = (top == -1) ? N - 1 : stack[top] - 1;
                stack[++top] = y;
            }

            // Evaluate each maximal rectangle and peel rows off if sum > 2 * k
            for (int y = 0; y < N; y++) {
                if (height[y] == 0) continue;

                int x1 = x - height[y] + 1;
                int y1 = leftBound[y];
                int x2 = x;
                int y2 = rightBound[y];

                long sum = gridSum(new Region(x1, y1, x2, y2));
                if (sum >= k) {
                    while (sum > 2 * k) {
                        x2--;
                        sum = gridSum(new Region(x1, y1, x2, y2));
                    }
                    return new Region(x1, y1, x2, y2);
                }
            }
        }

        return null;
    }
}
