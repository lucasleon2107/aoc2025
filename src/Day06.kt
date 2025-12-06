fun main() {
    fun part1(input: List<String>): Long {
        return solveWorksheet(input)
    }

    fun part2(input: List<String>): Long {
        return solveWorksheetColumns(input)
    }

    val testInput = readInput("Day06_test")
    check(part1(testInput) == 4277556L)
    check(part2(testInput) == 3263827L)

    val input = readInput("Day06")
    part1(input).println()
    part2(input).println()
}


data class Problem(
    val numbers: List<Long>,
    val op: Char
)

fun solveWorksheet(lines: List<String>): Long {
    if (lines.isEmpty()) return 0L

    val width = lines.maxOf { it.length }
    val grid = lines.map { it.padEnd(width, ' ') }

    val lastRowIndex = grid.lastIndex
    val opRow = grid[lastRowIndex]

    val problemRanges = findProblemRanges(grid)

    val problems = problemRanges.map { range ->
        val numbers = mutableListOf<Long>()

        for (rowIndex in 0 until lastRowIndex) {
            val slice = grid[rowIndex].substring(range).trim()
            if (slice.isNotEmpty()) {
                numbers += slice.toLong()
            }
        }

        val opSlice = opRow.substring(range).trim()
        val op = opSlice.firstOrNull()
            ?: error("No operator found for problem in columns $range")

        Problem(numbers, op)
    }

    return problems.sumOf { problem ->
        when (problem.op) {
            '+' -> problem.numbers.sum()
            '*' -> problem.numbers.fold(1L) { acc, n -> acc * n }
            else -> error("Unexpected operator: ${problem.op}")
        }
    }
}

fun solveWorksheetColumns(lines: List<String>): Long {
    if (lines.isEmpty()) return 0L

    val width = lines.maxOf { it.length }
    val grid = lines.map { it.padEnd(width, ' ') }

    val lastRowIndex = grid.lastIndex
    val opRow = grid[lastRowIndex]

    val problemRanges = findProblemRanges(grid)

    val problems = problemRanges.map { range ->
        val numbers = mutableListOf<Long>()

        for (c in range) {
            val digits = StringBuilder()

            for (rowIndex in 0 until lastRowIndex) {
                val ch = grid[rowIndex][c]
                if (ch.isDigit()) {
                    digits.append(ch)
                }
            }

            if (digits.isNotEmpty()) {
                numbers += digits.toString().toLong()
            }
        }

        val opSlice = opRow.substring(range).trim()
        val op = opSlice.firstOrNull()
            ?: error("No operator found for problem in columns $range")

        Problem(numbers, op)
    }

    return problems.sumOf { problem ->
        when (problem.op) {
            '+' -> problem.numbers.sum()
            '*' -> problem.numbers.fold(1L) { acc, n -> acc * n }
            else -> error("Unexpected operator: ${problem.op}")
        }
    }
}

fun findProblemRanges(grid: List<String>): List<IntRange> {
    val width = grid[0].length
    val ranges = mutableListOf<IntRange>()

    var inProblem = false
    var startCol = 0

    for (c in 0 until width) {
        val isSeparator = grid.all { row -> row[c] == ' ' }

        if (!isSeparator && !inProblem) {
            inProblem = true
            startCol = c
        } else if (isSeparator && inProblem) {
            inProblem = false
            ranges += (startCol..(c - 1))
        }
    }

    if (inProblem) {
        ranges += (startCol..(width - 1))
    }

    return ranges
}