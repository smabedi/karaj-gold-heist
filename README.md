# Karaj Gold Heist — $O(N^2)$ Subrectangle Sum $[k, 2k]$

[![License: MIT](https://img.shields.io/badge/License-MIT-amber.svg)](LICENSE)
[![Language: Java 17+](https://img.shields.io/badge/Java-17%2B-orange.svg)](src/Main.java)
[![Visualizer: Interactive HTML](https://img.shields.io/badge/Visualizer-Interactive%20Demo-emerald.svg)](https://smabedi.github.io/karaj-gold-heist/)
[![Problem Statement: Persian PDF](https://img.shields.io/badge/Problem%20PDF-Persian%20(Quera)-blue.svg)](karaj-problem-statement.pdf)

> **Quick Links:** [Live Interactive Visualizer](https://smabedi.github.io/karaj-gold-heist/) · [Java Solution (`Main.java`)](src/Main.java) · [Original Problem Statement (Persian PDF)](karaj-problem-statement.pdf) · [Sample & Edge Test Cases](test_cases.txt)

An $O(N^2)$ time and $O(N^2)$ space deterministic solution—paired with a zero-dependency, step-by-step interactive web visualizer—for the **Karaj Gold Heist** algorithmic problem (*Data Structures & Algorithms — Project 1, Problem 2*).

---

## Table of Contents

- [Problem Statement](#problem-statement)
- [Interactive Visualizer](#interactive-visualizer)
- [Algorithm & Mathematical Proof](#algorithm--mathematical-proof)
- [Complexity Comparison](#complexity-comparison)
- [Repository Structure](#repository-structure)
- [Quick Start](#quick-start)
- [Keyboard Shortcuts](#keyboard-shortcuts)

---

## Problem Statement

Beneath the city of Karaj lies an $N \times N$ subterranean grid where cell $(i, j)$ contains $A_{i,j}$ units of gold ($0 \le A_{i,j} \le 10^9$). Given a threshold $k$ ($1 \le k \le 10^9$), find any axis-aligned subrectangle defined by top-left corner $(x_1, y_1)$ and bottom-right corner $(x_2, y_2)$ ($1 \le x_1 \le x_2 \le N$, $1 \le y_1 \le y_2 \le N$) whose total gold sum $S$ satisfies:

$$k \le S = \sum_{i=x_1}^{x_2} \sum_{j=y_1}^{y_2} A_{i,j} \le 2k$$

* **Output:** Print four space-separated 1-indexed integers `x1 y1 x2 y2` if any valid subrectangle exists, or `NIST` otherwise.
* **Constraints:** $1 \le N \le 2000$, $1 \le k \le 10^9$, $0 \le A_{i,j} \le 10^9$ (`1.0 s` time limit, `256 MB` memory limit).

---

## Interactive Visualizer

The standalone visualizer (`index.html`) traces every operation of `HistogramConverter()` in `Main.java` with synchronized telemetry:

* **4-Stage Synchronized Execution:** Step forward or backward through **Stage 0** (2D Prefix Sums), **Stage 1** ($1 \times 1$ Scan), **Stage 2** ($1 \times W$ Sliding Window), and **Stage 3** (Monotonic Stack Histogram & Row Peeling).
* **Live Code & Telemetry Sync:** Highlights the active lines in `Main.java` alongside live variable watches, mathematical invariants, a $[0, k, 2k]$ target sum gauge, and an $O(1)$ inclusion-exclusion corner inspector (`+total`, `-top`, `-left`, `+topLeft`).
* **Stage 3 Histogram & Stack Inspector:** Renders vertical bar heights, aligned `height[y]`, `leftBound[y]`, and `rightBound[y]` arrays, and bottom-up monotonic stack state transitions.
* **Interactive Sandbox & Verification:**
  * Click any grid cell to edit values inline (`Tab` / `Shift+Tab` to move across cells).
  * Import or copy raw competitive-programming `stdin` (`N k` + matrix) via the **Import / Copy Stdin** modal.
  * Toggle **O(N^4) Verify** to exhaustively enumerate all valid subrectangles in $[k, 2k]$ and preview them on the grid.
  * Export self-contained `.html` snapshots preserving your custom grid, $k$, and current step.

---

## Algorithm & Mathematical Proof

With $N \le 2000$, enumerating all $\binom{N+1}{2}^2 = O(N^4)$ subrectangles times out immediately. We reduce the search to $O(N^2)$ via four consecutive stages where each stage establishes a structural invariant for the next:

### Stage 0 — 2D Prefix Sum Precomputation ($O(N^2)$)
We build `preCachedGridSum[N][N]` using a horizontal pass followed by a vertical pass so any subrectangle sum is queried in $O(1)$ time via inclusion-exclusion:

$$\text{gridSum}(x_1, y_1, x_2, y_2) = P[x_2][y_2] - P[x_1 - 1][y_2] - P[x_2][y_1 - 1] + P[x_1 - 1][y_1 - 1]$$

### Stage 1 — Single-Cell Scan ($O(N^2)$)
Scan every $1 \times 1$ cell $(x, y)$. If $k \le A_{x,y} \le 2k$, return $(x, y, x, y)$ immediately.
* **Post-Stage 1 Invariant:** Every cell in the grid is now either **usable** ($A_{x,y} < k$) or a **forbidden barrier** ($A_{x,y} > 2k$, since $A_{i,j} \ge 0$).

### Stage 2 — Single-Row Sliding Window ($O(N^2)$)
For each row $x$, maintain a two-pointer window $[ \text{left}, \text{right} ]$ and reset whenever $A_{x, \text{right}} > 2k$. Because every usable cell satisfies $A_{x,y} < k$, adding one cell to a window whose sum is $< k$ produces a new sum $< 2k$—it can never overshoot $[k, 2k]$.
* **Post-Stage 2 Invariant:** Every contiguous horizontal segment of $< k$ cells in any row has total sum strictly $< k$.

### Stage 3 — 2D Histogram, Monotonic Stack & Row Peeling ($O(N^2)$)
Treat cells with $A_{x,y} < k$ as `1` and cells with $A_{x,y} > 2k$ as `0` (obstacles):
1. For each bottom row $x$, update vertical column heights `height[y]` in $O(N)$.
2. Run two $O(N)$ monotonically increasing stack passes to compute `leftBound[y]` and `rightBound[y]`, identifying the unique maximal-width rectangle $[x_1, y_1] \to [x, y_2]$ of height `height[y]`.
3. Evaluate its sum $S$ in $O(1)$ using `gridSum`. If $S \ge k$:
   * While $S > 2k$, decrement `x2--` (peeling off the bottom row of width $y_2 - y_1 + 1$).
   * **Why Row Peeling Never Overshoots:** By the **Post-Stage 2 Invariant**, every horizontal row segment of $< k$ cells has sum strictly $< k$. Subtracting $< k$ from a sum $> 2k$ can never jump below $k$, guaranteeing termination inside $[k, 2k]$ in at most $N$ decrements.

If all $N$ rows finish without finding a valid region, no valid subrectangle exists and the program outputs `NIST`.

---

## Complexity Comparison

| Approach | Time Complexity | Space Complexity | Status for $N = 2000$ ($1\text{ s}$ Limit) |
| :--- | :---: | :---: | :--- |
| Brute Force (All Subrectangles) | $O(N^4)$ | $O(N^2)$ | TLE ($\approx 4 \times 10^{12}$ ops) |
| 2D Prefix Sum + 1D Sliding Window | $O(N^3)$ | $O(N^2)$ | TLE ($\approx 4 \times 10^9$ ops) |
| **4-Stage Histogram Reduction (`Main.java`)** | **$O(N^2)$** | **$O(N^2)$** | **Accepted ($\approx 4 \times 10^6$ ops)** |

---

## Repository Structure

```text
karaj-gold-heist/
├── index.html                        # Self-contained interactive algorithm visualizer
├── src/
│   └── Main.java                     # O(N²) Java implementation (4-stage reduction)
├── karaj-problem-statement.pdf       # Official problem statement (Persian)
├── test_cases.txt                    # Sample inputs and edge-case test suite
├── LICENSE.txt                       # MIT License
└── README.md                         # Problem breakdown, proof & documentation
```

---

## Quick Start

### 1. Run the Interactive Visualizer
Open `index.html` directly in any modern web browser—no build step, bundler, or local server required.

### 2. Compile & Run `Main.java`
```bash
javac -d out src/Main.java
java -cp out Main << 'EOF'
3 10
1 2 3
4 5 6
7 8 9
EOF
```
Expected output:
```text
1 1 2 2
```

---

## Keyboard Shortcuts

| Key | Action |
| :--- | :--- |
| `Space` | Play / Pause step-by-step execution |
| `ArrowRight` | Advance one algorithmic step |
| `ArrowLeft` | Step backward one algorithmic step |
| `Tab` / `Shift + Tab` | Move to next / previous cell while editing the grid inline |
| `Enter` / `Escape` | Commit or cancel inline cell edit / close modal |
