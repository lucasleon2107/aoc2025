import com.google.ortools.Loader
import com.google.ortools.sat.CpModel
import com.google.ortools.sat.CpSolver
import com.google.ortools.sat.CpSolverStatus
import com.google.ortools.sat.LinearExpr

data class Machine(
    val numLights: Int,
    val targetMask: Int,
    val buttonMasks: List<Int>,
    val buttonIndices: List<IntArray>,
    val jolts: IntArray
)

fun parseMachines(input: List<String>): List<Machine> {
    val machines = mutableListOf<Machine>()

    val indicatorRegex = Regex("""\[(.+)]""")
    val buttonRegex = Regex("""\(([^)]*)\)""")
    val joltsRegex = Regex("""\{([^}]*)}""")

    for (raw in input) {
        val line = raw.trim()
        if (line.isEmpty()) continue

        val indicatorMatch = indicatorRegex.find(line)
            ?: error("No indicator pattern found in line: $line")

        val pattern = indicatorMatch.groupValues[1]
        val numLights = pattern.length

        var targetMask = 0
        pattern.forEachIndexed { idx, ch ->
            if (ch == '#') {
                targetMask = targetMask or (1 shl idx)
            }
        }

        val buttonMasks = mutableListOf<Int>()
        val buttonIndices = mutableListOf<IntArray>()

        for (match in buttonRegex.findAll(line)) {
            val inner = match.groupValues[1]
            if (inner.isBlank()) {
                buttonMasks += 0
                buttonIndices += IntArray(0)
            } else {
                val idxList = inner.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { it.toInt() }

                val idxArray = idxList.toIntArray()
                var mask = 0
                for (idx in idxArray) {
                    mask = mask or (1 shl idx)
                }
                buttonMasks += mask
                buttonIndices += idxArray
            }
        }

        val joltsMatch = joltsRegex.find(line)
            ?: error("No joltage requirements found in line: $line")

        val joltsInner = joltsMatch.groupValues[1]
        val jolts = joltsInner.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { it.toInt() }
            .toIntArray()

        machines += Machine(
            numLights = numLights,
            targetMask = targetMask,
            buttonMasks = buttonMasks,
            buttonIndices = buttonIndices,
            jolts = jolts
        )
    }

    return machines
}

fun minPressesForMachineLights(machine: Machine): Int {
    val (numLights, targetMask, buttonMasks) = machine

    val startState = 0
    val maxState = 1 shl numLights

    val dist = IntArray(maxState) { -1 }
    val queue: ArrayDeque<Int> = ArrayDeque()

    dist[startState] = 0
    queue.add(startState)

    while (queue.isNotEmpty()) {
        val state = queue.removeFirst()
        val currentDist = dist[state]

        if (state == targetMask) {
            return currentDist
        }

        val nextDist = currentDist + 1
        for (buttonMask in buttonMasks) {
            val nextState = state xor buttonMask
            if (dist[nextState] == -1) {
                dist[nextState] = nextDist
                queue.add(nextState)
            }
        }
    }

    error("No solution found for lights")
}

fun minPressesForMachineJolts(machine: Machine): Int {
    val targets = machine.jolts.toList()
    if (targets.isEmpty()) return 0

    val validButtons = machine.buttonIndices
        .filter { it.isNotEmpty() }
        .map { button ->
            button.filter { it in targets.indices }
                .distinct()
                .sorted()
                .toIntArray()
        }
        .filter { it.isNotEmpty() }
        .distinctBy { it.joinToString(",") }

    if (validButtons.isEmpty()) {
        return if (targets.all { it == 0 }) 0 else error("No buttons")
    }

    for (i in targets.indices) {
        if (targets[i] != 0 && validButtons.none { i in it }) {
            error("Counter $i has target ${targets[i]} but no buttons affect it")
        }
    }

    Loader.loadNativeLibraries()

    val model = CpModel()

    val buttonVars = validButtons.map { button ->
        val maxPresses = button.minOf { index -> targets[index] }
        model.newIntVar(0, maxPresses.toLong(), "button")
    }

    for (i in targets.indices) {
        val affectingButtons = validButtons.indices.mapNotNull { j ->
            if (i in validButtons[j]) buttonVars[j] else null
        }

        if (affectingButtons.isEmpty()) {
            if (targets[i] != 0) error("Counter $i unreachable")
        } else {
            model.addEquality(
                LinearExpr.sum(affectingButtons.toTypedArray()),
                targets[i].toLong()
            )
        }
    }

    model.minimize(LinearExpr.sum(buttonVars.toTypedArray()))

    val solver = CpSolver()
    solver.parameters.numSearchWorkers = Runtime.getRuntime().availableProcessors()
    val status = solver.solve(model)

    return when (status) {
        CpSolverStatus.OPTIMAL, CpSolverStatus.FEASIBLE -> solver.objectiveValue().toInt()
        else -> error("No solution found: $status")
    }
}

fun part1(input: List<String>): Int {
    val machines = parseMachines(input)
    return machines.sumOf { minPressesForMachineLights(it) }
}

fun part2(input: List<String>): Int {
    val machines = parseMachines(input)
    return machines.sumOf { minPressesForMachineJolts(it) }
}

fun main() {
    val testInput = readInput("Day10_test")
    check(part1(testInput) == 7)
    check(part2(testInput) == 33)

    val input = readInput("Day10")
    part1(input).println()
    part2(input).println()
}