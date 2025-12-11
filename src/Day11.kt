fun main() {
    fun part1(input: List<String>): Long {
        val graph = parseGraph(input)
        return countPaths("you", "out", graph)
    }

    fun part2(input: List<String>): Long {
        val graph = parseGraph(input)
        return countPathsVisitingBoth("svr", "out", graph, "dac", "fft")
    }

    val testInput1 = readInput("Day11_test1")
    check(part1(testInput1) == 5L)

    val testInput2 = readInput("Day11_test2")
    check(part2(testInput2) == 2L)

    val input = readInput("Day11")
    part1(input).println()
    part2(input).println()
}

fun parseGraph(input: List<String>): Map<String, List<String>> {
    val graph = mutableMapOf<String, MutableList<String>>()

    for (line in input) {
        if (line.isBlank()) continue

        val (from, rest) = line.split(":", limit = 2)
        val targets = rest.trim()
            .takeIf { it.isNotEmpty() }
            ?.split(" ")
            ?: emptyList()

        graph.computeIfAbsent(from.trim()) { mutableListOf() }
            .addAll(targets.map { it.trim() })
    }

    return graph
}

fun countPaths(
    start: String,
    end: String,
    graph: Map<String, List<String>>
): Long {
    val memo = mutableMapOf<String, Long>()

    fun dfs(node: String): Long {
        if (node == end) return 1L
        memo[node]?.let { return it }

        val neighbors = graph[node] ?: emptyList()
        var total = 0L
        for (next in neighbors) {
            total += dfs(next)
        }
        memo[node] = total
        return total
    }

    return dfs(start)
}

fun countPathsVisitingBoth(
    start: String,
    end: String,
    graph: Map<String, List<String>>,
    a: String,
    b: String
): Long {
    data class State(val node: String, val seenA: Boolean, val seenB: Boolean)
    val memo = mutableMapOf<State, Long>()

    fun dfs(node: String, seenA: Boolean, seenB: Boolean): Long {
        val newA = seenA || node == a
        val newB = seenB || node == b

        if (node == end) {
            return if (newA && newB) 1L else 0L
        }

        val state = State(node, newA, newB)
        memo[state]?.let { return it }

        var total = 0L
        for (next in graph[node].orEmpty()) {
            total += dfs(next, newA, newB)
        }

        memo[state] = total
        return total
    }

    return dfs(start, false, false)
}