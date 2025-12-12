private data class ShapeOrientation(val w: Int, val h: Int, val cells: IntArray)
private data class Shape(val area: Int, val orientations: List<ShapeOrientation>)

private class Bitset(private val words: LongArray) {
    fun intersects(other: Bitset): Boolean {
        for (i in words.indices) if ((words[i] and other.words[i]) != 0L) return true
        return false
    }

    fun orInPlace(other: Bitset) {
        for (i in words.indices) words[i] = words[i] or other.words[i]
    }

    fun andNotInPlace(other: Bitset) {
        for (i in words.indices) words[i] = words[i] and other.words[i].inv()
    }

    fun popcount(): Int {
        var c = 0
        for (w in words) c += java.lang.Long.bitCount(w)
        return c
    }

    companion object {
        fun empty(cellCount: Int): Bitset = Bitset(LongArray((cellCount + 63) ushr 6))
        fun fromBits(cellCount: Int, bits: IntArray): Bitset {
            val b = empty(cellCount)
            for (idx in bits) {
                val wi = idx ushr 6
                val bi = idx and 63
                b.words[wi] = b.words[wi] or (1L shl bi)
            }
            return b
        }
    }
}

private fun parseInput(lines: List<String>): Pair<List<Shape>, List<Region>> {
    val shapes = mutableListOf<Shape>()
    val regions = mutableListOf<Region>()

    var i = 0

    // Shapes: "id:" then grid lines until blank. Repeat.
    while (i < lines.size) {
        val line = lines[i].trimEnd()
        if (line.isBlank()) {
            i++; continue
        }
        if (!line.endsWith(":")) break

        i++ // skip "id:"
        val gridLines = mutableListOf<String>()
        while (i < lines.size && lines[i].isNotBlank()) {
            gridLines += lines[i].trimEnd()
            i++
        }
        while (i < lines.size && lines[i].isBlank()) i++

        val h = gridLines.size
        val w = gridLines.maxOf { it.length }
        val pts = mutableListOf<Pair<Int, Int>>()
        for (y in 0 until h) {
            val row = gridLines[y].padEnd(w, '.')
            for (x in 0 until w) if (row[x] == '#') pts += x to y
        }

        val orientations = buildOrientations(pts)
        shapes += Shape(area = pts.size, orientations = orientations)
    }

    // Regions: "WxH: c0 c1 c2 ..."
    while (i < lines.size) {
        val raw = lines[i].trim()
        i++
        if (raw.isBlank()) continue
        val parts = raw.split(":")
        val wh = parts[0].trim()
        val (W, H) = wh.split("x").map { it.trim().toInt() }
        val counts = parts.getOrNull(1)
            ?.trim()
            ?.split(Regex("\\s+"))
            ?.filter { it.isNotEmpty() }
            ?.map { it.toInt() }
            ?: emptyList()

        regions += Region(W, H, counts.toIntArray())
    }

    return shapes to regions
}

private fun buildOrientations(points: List<Pair<Int, Int>>): List<ShapeOrientation> {
    fun rot(p: Pair<Int, Int>, r: Int): Pair<Int, Int> = when (r) {
        0 -> p
        1 -> (-p.second) to p.first
        2 -> (-p.first) to (-p.second)
        else -> p.second to (-p.first)
    }

    val seen = HashSet<String>()
    val out = mutableListOf<ShapeOrientation>()

    for (flip in listOf(false, true)) {
        for (r in 0..3) {
            val tr = points.map { (x, y) ->
                val fx = if (flip) -x else x
                rot(fx to y, r)
            }

            val minX = tr.minOf { it.first }
            val minY = tr.minOf { it.second }
            val norm = tr.map { (x, y) -> (x - minX) to (y - minY) }

            val w = norm.maxOf { it.first } + 1
            val h = norm.maxOf { it.second } + 1
            val cells = norm.map { (x, y) -> y * w + x }.sorted()

            val key = "$w,$h:" + cells.joinToString(",")
            if (seen.add(key)) out += ShapeOrientation(w, h, cells.toIntArray())
        }
    }
    return out
}

private data class Region(val w: Int, val h: Int, val counts: IntArray)

private fun generatePlacements(shape: Shape, W: Int, H: Int): List<Bitset> {
    val placements = ArrayList<Bitset>()
    val cellCount = W * H

    for (ori in shape.orientations) {
        if (ori.w > W || ori.h > H) continue
        for (y0 in 0..(H - ori.h)) {
            for (x0 in 0..(W - ori.w)) {
                val bits = IntArray(ori.cells.size)
                for (k in ori.cells.indices) {
                    val c = ori.cells[k]
                    val ox = c % ori.w
                    val oy = c / ori.w
                    bits[k] = (y0 + oy) * W + (x0 + ox)
                }
                placements += Bitset.fromBits(cellCount, bits)
            }
        }
    }
    return placements
}

private fun canFitRegion(shapes: List<Shape>, region: Region): Boolean {
    val W = region.w
    val H = region.h
    val n = shapes.size

    val need = IntArray(n) { idx -> region.counts.getOrElse(idx) { 0 } }
    val totalArea = (0 until n).sumOf { need[it] * shapes[it].area }
    if (totalArea > W * H) return false

    val placementsByShape = Array(n) { generatePlacements(shapes[it], W, H) }
    for (s in 0 until n) if (need[s] > 0 && placementsByShape[s].isEmpty()) return false

    val lastIndex = IntArray(n) { 0 }
    val occ = Bitset.empty(W * H)

    fun remainingArea(): Int = (0 until n).sumOf { need[it] * shapes[it].area }

    fun dfs(): Boolean {
        if (need.all { it == 0 }) return true

        val free = W * H - occ.popcount()
        if (remainingArea() > free) return false

        var bestShape = -1
        var bestCandidates: IntArray? = null
        var bestCount = Int.MAX_VALUE

        for (s in 0 until n) {
            if (need[s] <= 0) continue
            val list = placementsByShape[s]
            val start = lastIndex[s]
            if (start >= list.size) return false

            val tmp = IntArray(list.size - start)
            var c = 0
            for (pi in start until list.size) {
                if (!occ.intersects(list[pi])) {
                    tmp[c++] = pi
                    if (c >= bestCount) break
                }
            }
            if (c == 0) return false
            if (c < bestCount) {
                bestCount = c
                bestShape = s
                bestCandidates = tmp.copyOf(c)
                if (bestCount == 1) break
            }
        }

        val s = bestShape
        val cands = bestCandidates ?: return false
        val list = placementsByShape[s]

        val savedNeed = need[s]
        val savedLast = lastIndex[s]

        for (pi in cands) {
            need[s] = savedNeed - 1
            lastIndex[s] = pi + 1

            occ.orInPlace(list[pi])
            if (dfs()) return true
            occ.andNotInPlace(list[pi])

            need[s] = savedNeed
            lastIndex[s] = savedLast
        }

        return false
    }

    return dfs()
}

private fun solve(input: List<String>): Int {
    val (shapes, regions) = parseInput(input)
    var ok = 0
    for (r in regions) if (canFitRegion(shapes, r)) ok++
    return ok
}

fun main() {
    fun part1(input: List<String>): Int = solve(input)

    // The prompt only defines one question (count regions that can fit),
    // so part2 is the same unless your AoC variant adds a second part.
    fun part2(input: List<String>): Int = solve(input)

    val testInput = readInput("Day12_test")
    check(part1(testInput) == 2)

    val input = readInput("Day12")
    part1(input).println()
    part2(input).println()
}
