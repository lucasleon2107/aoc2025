fun main() {
    fun part1(input: List<String>): Long {
        val ranges = input.first().split(",")
        var invalidSum: Long = 0

        ranges.forEach { range ->
            val splitRange = range.split("-")
            val start = splitRange.first().toLong()
            val end = splitRange.last().toLong()

            invalidSum += findRepeated(start, end).sum()
        }

        return invalidSum
    }

    fun part2(input: List<String>): Long {
        val ranges = input.first().split(",")
        var invalidSum: Long = 0

        ranges.forEach { range ->
            val splitRange = range.split("-")
            val start = splitRange.first().toLong()
            val end = splitRange.last().toLong()

            invalidSum += numbersWithRepeatedPattern(start, end).sum()
        }

        return invalidSum
    }

    val testInput = readInput("Day02_test")
    check(part1(testInput) == 1227775554L)
    check(part2(testInput) == 4174379265L)

    val input = readInput("Day02")
    part1(input).println()
    part2(input).println()
}

fun hasFullRepeatedBlock(n: Long): Boolean {
    val s = n.toString()
    val len = s.length

    if (len % 2 != 0) return false

    val half = len / 2
    val left = s.take(half)
    val right = s.substring(half)

    return left == right
}

fun findRepeated(start: Long, end: Long): List<Long> = (start..end).filter { hasFullRepeatedBlock(it) }

fun isMadeOfRepeatedBlock(n: Long): Boolean {
    val s = n.toString()
    val len = s.length

    for (blockSize in 1..len / 2) {

        if (len % blockSize != 0) continue

        val block = s.take(blockSize)
        val times = len / blockSize

        if (times >= 2 && block.repeat(times) == s) {
            return true
        }
    }

    return false
}

fun numbersWithRepeatedPattern(start: Long, end: Long): List<Long> = (start..end).filter {
    isMadeOfRepeatedBlock(it)
}
