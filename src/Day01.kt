import kotlin.math.abs

fun main() {
    fun part1(input: List<String>): Int {
        var zeroCount = 0
        val dial = 100
        var dialStart = 50

        input.forEach { rotation ->
            val direction = rotation.first()
            val amount = rotation.substring(1).toInt()
            val steps = if (direction == 'L') -amount else amount

            dialStart = dial.moveFrom(dialStart, steps)

            if(dialStart == 0) zeroCount++
        }

        return zeroCount
    }

    fun part2(input: List<String>): Int {
        val dial = 100
        var dialStart = 50
        var zeroCount = 0

        input.forEach { rotation ->
            val direction = rotation.first()
            val amount = rotation.substring(1).toInt()
            val steps = if (direction == 'L') -amount else amount

            zeroCount += dial.zeroPasses(dialStart, steps)

            dialStart = dial.moveFrom(dialStart, steps)
        }

        return zeroCount
    }

    val testInput = readInput("Day01_test")
    check(part1(testInput) == 3)
    check(part2(testInput) == 6)

    val input = readInput("Day01")
    part1(input).println()
    part2(input).println()
}

fun Int.circular(index: Int) = (index % this + this) % this

fun Int.moveFrom(start: Int, steps: Int): Int = circular(start + steps)

fun Int.zeroPasses(start: Int, steps: Int): Int {
    if (steps == 0) return 0

    val n = this
    val total = abs(steps)
    val fullLaps = total / n
    val partial = total % n

    var passes = fullLaps
    if (partial == 0) return passes

    val s = circular(start)
    val dir = if (steps > 0) 1 else -1

    var distToZero = if (dir > 0) {
        (n - s) % n
    } else {
        s % n
    }

    if (distToZero == 0) distToZero = n

    if (partial >= distToZero) passes++

    return passes
}