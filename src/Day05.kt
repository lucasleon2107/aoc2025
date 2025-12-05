fun main() {
    fun part1(input: List<String>): Int {
        var freshCount = 0

        val blankIndex = input.indexOf("")
        val ranges = input.subList(0, blankIndex)
            .map {
                val (start, end) = it.split("-")
                start.toLong()..end.toLong()
            }

        val ingredients = input.subList(blankIndex + 1, input.size).map { it.toLong() }

        for (ingredient in ingredients) {
            for (range in ranges) {
                if(ingredient in range) {
                    freshCount++
                    break
                }
            }
        }

        return freshCount
    }

    fun part2(input: List<String>): Long {
        val blankIndex = input.indexOf("")

        val ranges = input.take(blankIndex)
            .map {
                val (start, end) = it.split("-")
                start.toLong()..end.toLong()
            }

        return countFreshIds(ranges)
    }

    val testInput = readInput("Day05_test")
    check(part1(testInput) == 3)
    check(part2(testInput) == 14L)

    val input = readInput("Day05")
    part1(input).println()
    part2(input).println()
}

fun mergedRanges(ranges: List<LongRange>): List<LongRange> {
    if (ranges.isEmpty()) return emptyList()

    val sorted = ranges.sortedBy { it.first }
    val merged = mutableListOf<LongRange>()

    var currentStart = sorted[0].first
    var currentEnd = sorted[0].last

    for (range in sorted.drop(1)) {
        if (range.first <= currentEnd + 1) {
            if (range.last > currentEnd) {
                currentEnd = range.last
            }
        } else {
            merged += currentStart..currentEnd
            currentStart = range.first
            currentEnd = range.last
        }
    }

    merged += currentStart..currentEnd
    return merged
}

fun countFreshIds(ranges: List<LongRange>): Long {
    val merged = mergedRanges(ranges)
    return merged.sumOf { it.last - it.first + 1 }
}
