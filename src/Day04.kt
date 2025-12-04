fun main() {
    fun part1(input: List<String>): Int {
        return countAccessibleRolls(input)
    }

    fun part2(input: List<String>): Int {
        return totalRemovableRolls(input)
    }

    val testInput = readInput("Day04_test")
    check(part1(testInput) == 13)
    check(part2(testInput) == 43)

    val input = readInput("Day04")
    part1(input).println()
    part2(input).println()
}

fun countAccessibleRolls(lines: List<String>): Int {
    if (lines.isEmpty()) return 0

    val height = lines.size
    val width = lines[0].length
    val grid = lines.map { it.toCharArray() }

    val directions = listOf(
        -1 to -1, -1 to 0, -1 to 1,
        0 to -1,          0 to 1,
        1 to -1,  1 to 0, 1 to 1
    )

    var accessibleCount = 0

    for (r in 0 until height) {
        for (c in 0 until width) {
            if (grid[r][c] != '@') continue

            var neighborRolls = 0
            for ((dr, dc) in directions) {
                val nr = r + dr
                val nc = c + dc
                if (nr in 0 until height && nc in 0 until width && grid[nr][nc] == '@') {
                    neighborRolls++
                }
            }

            if (neighborRolls < 4) {
                accessibleCount++
            }
        }
    }

    return accessibleCount
}

fun totalRemovableRolls(lines: List<String>): Int {
    if (lines.isEmpty()) return 0

    val height = lines.size
    val width = lines[0].length
    val grid = Array(height) { r -> lines[r].toCharArray() }

    val directions = listOf(
        -1 to -1, -1 to 0, -1 to 1,
        0 to -1,          0 to 1,
        1 to -1,  1 to 0, 1 to 1
    )

    var totalRemoved = 0

    while (true) {
        val toRemove = mutableListOf<Pair<Int, Int>>()

        for (r in 0 until height) {
            for (c in 0 until width) {
                if (grid[r][c] != '@') continue

                var neighbors = 0
                for ((dr, dc) in directions) {
                    val nr = r + dr
                    val nc = c + dc
                    if (nr in 0 until height && nc in 0 until width && grid[nr][nc] == '@') {
                        neighbors++
                    }
                }

                if (neighbors < 4) {
                    toRemove += r to c
                }
            }
        }

        if (toRemove.isEmpty()) break

        for ((r, c) in toRemove) {
            grid[r][c] = '.'
        }

        totalRemoved += toRemove.size
    }

    return totalRemoved
}