fun main() {
    fun part1(input: List<String>): Int {
        return countSplits(input)
    }

    fun part2(input: List<String>): Long {
        return countTimelines(input)
    }

    val testInput = readInput("Day07_test")
    check(part1(testInput) == 21)
    check(part2(testInput) == 40L)

    val input = readInput("Day07")
    part1(input).println()
    part2(input).println()
}


fun countSplits(grid: List<String>): Int {
    val height = grid.size
    if (height == 0) return 0
    val width = grid[0].length

    var startRow = -1
    var startCol = -1
    outer@ for (r in grid.indices) {
        val c = grid[r].indexOf('S')
        if (c != -1) {
            startRow = r
            startCol = c
            break@outer
        }
    }
    require(startRow != -1) { "No S found in grid" }

    var splits = 0

    var beams: Set<Int> = setOf(startCol)

    for (row in (startRow + 1) until height) {
        val nextBeams = mutableSetOf<Int>()

        for (col in beams) {
            if (grid[row][col] == '^') {
                splits++

                if (col - 1 >= 0) {
                    nextBeams.add(col - 1)
                }
                if (col + 1 < width) {
                    nextBeams.add(col + 1)
                }
            } else {
                nextBeams.add(col)
            }
        }

        beams = nextBeams
        if (beams.isEmpty()) break
    }

    return splits
}

fun countTimelines(grid: List<String>): Long {
    val height = grid.size
    if (height == 0) return 0
    val width = grid[0].length

    var startRow = -1
    var startCol = -1
    outer@ for (r in grid.indices) {
        val c = grid[r].indexOf('S')
        if (c != -1) {
            startRow = r
            startCol = c
            break@outer
        }
    }
    require(startRow != -1) { "No S found in grid" }

    var counts = LongArray(width)
    counts[startCol] = 1L

    for (row in (startRow + 1) until height) {
        val next = LongArray(width)

        for (col in 0 until width) {
            val ways = counts[col]
            if (ways == 0L) continue

            when (grid[row][col]) {
                '^' -> {
                    if (col - 1 >= 0) next[col - 1] += ways
                    if (col + 1 < width) next[col + 1] += ways
                }
                else -> {
                    next[col] += ways
                }
            }
        }

        counts = next
    }

    return counts.sum()
}