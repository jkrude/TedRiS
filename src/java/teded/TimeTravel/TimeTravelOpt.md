### Optimisations for generalised Time-Travel-Riddle

#### General properties

let $k$ be the number of vertices in $G$ and $C$ the size of the searched circle

- The fully connected graph with $k$ nodes has $k$ over $2$ edges ($|E| =$ ${x^2 -x}\over 2$)
- Thereby there are $2^{|E|}$ possible graphs where every edge is either *red* or *blue*

#### First naive approach

- Test all $2^{|E|}$ possibilities
- $k \cdot (c -1)$ worst case approximation for circle finding
- represent edges and nodes by objects
- Store the graph as set of nodes, where every node has a list of edges
- test every graph for circles with colour blue and red

| Circle Size C | Time (ms) | Iterations | Solution      | Max Size of Search tree |
| ------------- | --------- | ---------- | ------------- | ----------------------- |
| 1             | -         | -          | -             | -                       |
| 2             | 0.14      | 100        | 2             | 2                       |
| 3             | 101.67    | 1 000      | 6             | 32.7685                 |
| 4             | 114.56    | 100        | 6             | 32.7685                 |
| 5             | -         | -          | Out of Memory | 268.435.456             |

#### Second approach

| Circle Size C | Time (ms) | Iterations | Solution      | Max Size of Search tree |
| ------------- | --------- | ---------- | ------------- | ----------------------- |
| 1             | -         | -          | -             | -                       |
| 2             | 0.014     | 1 000      | 2             | 3                       |
| 3             | 15.25     | 1 000      | 6             | 38 613                  |
| 4             | 27.82     | 100        | 6             | 40 752                  |
| 5             | -         | -          | Out of Memory | 268.435.456             |

Adding a choosing strategy to reduces the number of tried coloring-options

| Circle Size C | Time (ms) | Iterations | Solution      | Max Size of Search tree |
| ------------- | --------- | ---------- | ------------- | ----------------------- |
| 3             | 14.33     | 1 000      | 6             | 23 594                  |
| 4             | 16.589    | 1 000      | 6             | 23 719                  |
| 5             | -         | -          | Out of Memory | 268.435.456             |

#### Third Approach

- Changing `SearchTree` to `EnumeratingSearch` in order to fix `OutOfMemory`-Exception.
  - All the saved `BranchOptions` with mapped and `toBeMapped` took to much space

- Using

```java
  asBString="0".repeat(this.toBeMapped.size()-asBString.length())+asBString;
        // instead of 
        `String.format(,asBString).replace(" ","0");`
// bring huge performance increase(75% over all → 19% over all)
```

- using one array for all Search-Results was 82% more efficient | Circle Size C | Time (ms) | Iterations | Solution |
  Max Size of Search tree | | ------------- | --------- | ---------- | -------- | ----------------------- | | 3 | 6.469|
  1 000 | 6 | 20 184 | | 4 | 12.818 | 1 000 | 6 | 21 580 | | 5 | | 1 | > 8 | > 68 000 000 000 |

- Search-Space for $k=8$: 16 974 025
  - over 100 It took 20 590ms until $k=8$ was discarded
- No solution up to `state = 17 677 804 469` took over 3h
- Coloring graph takes really long (38%)



#### Cutting down the numbers

- Let $k$ be the number of nodes

- Let $v$ be the number of edges = $k^2 - k \over 2$

- If all graphs with $k$ nodes and $x$ edges have a cycle of length $c$
  - then all graphs with $x+1$ edges have a cycle of length $c$
  - therefore for all graphs with $k - x\geq$ nodes the inverse has a cycle

- For 18,19 and 20 of 36 edges there is an instance which has no circle$=5$

  - All Graphs with $9$ nodes and $21$ edges have a cycle of length $5$

    → This eliminates 56 994 458 000 possible coloring-options ($\geq 22$ edges)

#### Parallel

- partition $1\dots2^{|V|}$ into `numOfThreads` parts
- Each thread tests only graphs that correspond to encoding of $n \in$ partition
  - report to callback if instance without cycle was found
  - if that was the case callback will close all threads
- This is repeated for each $k$
- **Problem**: unbalanced load → the more edges the more instances
  - partitions should be distributed by resulting graph instances / number of edges

| Iterations | Description                | Number of Graphs | P=1: Time (ms)  | P=16: Time      | Speedup |
| ---------- | -------------------------- | ---------------- | --------------- | --------------- | ------- |
| 10         | c=5, k=9, step $\leq 10^7$ | $10^7$           | 14 510          | 2 671 (it =100) | 5,43    |
| 10         | step $\leq 10^8 * 4$       | 400 000 000      | 425 344 (it =3) | 75 198          | 5,65    |

#### Improving finding cycles

- Use only *one* array for finding the cycle
  - (instead of new copied array list for every added element)
- use `inverted` parameter instead of creating an inverted new matrix

| Iterations | Description          | Number of Graphs | P=1: Time (ms)  | P=16: Time           | Speedup |
| ---------- | -------------------- | ---------------- | --------------- | :------------------- | ------- |
| 1          | step $\leq 10^8 * 4$ | 400 000 000      | 425 344 (it =3) | 33 681 (41391 it=10) | 5,65    |

- profiling is completely dominated by `hasNoCirlce::contains` (42%) and `colorGraph` (42%)