fun main() {
    fun part1(input: List<String>): Int {
        var joltSum = 0
        input.forEach { bank ->
            val batteriesArray = bank.map { it.digitToInt() }.toIntArray()
            joltSum += maxTwoDigitNumber(batteriesArray)
        }
        return joltSum
    }

    fun part2(input: List<String>): Long {
        var joltSum = 0L
        input.forEach { bank ->
            val batteriesArray = bank.map { it.digitToInt() }.toIntArray()
            joltSum += maxNumberKeep12(batteriesArray).toLong()
        }
        return joltSum
    }

    val testInput = readInput("Day03_test")
    check(part1(testInput) == 357)
    check(part2(testInput) == 3121910778619)

    val input = readInput("Day03")
    part1(input).println()
    part2(input).println()
}

fun maxTwoDigitNumber(nums: IntArray): Int {
    if (nums.size < 2) throw IllegalArgumentException("Need at least two digits")

    var maxLeft = nums[0]
    var best = Int.MIN_VALUE

    for (i in 1 until nums.size) {
        val d = nums[i]

        val candidate = maxLeft * 10 + d
        if (candidate > best) {
            best = candidate
        }

        if (d > maxLeft) {
            maxLeft = d
        }
    }

    return best
}

fun maxNumberKeep12(nums: IntArray): String {
    require(nums.size >= 12) { "Need at least 12 digits" }

    var toRemove = nums.size - 12
    val stack = ArrayDeque<Int>()

    for (d in nums) {
        while (toRemove > 0 && stack.isNotEmpty() && stack.last() < d) {
            stack.removeLast()
            toRemove--
        }
        stack.addLast(d)
    }

    repeat(toRemove) {
        if (stack.isNotEmpty()) stack.removeLast()
    }

    val result = stack.take(12)

    return result.joinToString("") { it.toString() }
}