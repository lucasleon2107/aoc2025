import kotlin.math.abs

fun main() {
    fun part1(input: List<String>): Long {
        return largestRedRectangleArea(input)
    }

    fun part2(input: List<String>): Long {
        return solveEnclosedRectangles(input)
    }

    val testInput = readInput("Day09_test")
    check(part1(testInput) == 50L)
    check(part2(testInput) == 24L)

    val input = readInput("Day09")
    part1(input).println()
    part2(input).println()
}

data class RedPoint(val x: Int, val y: Int)

fun largestRedRectangleArea(input: List<String>): Long {
    val points = input
        .filter { it.isNotBlank() }
        .map { line ->
            val (xStr, yStr) = line.split(",")
            RedPoint(xStr.toInt(), yStr.toInt())
        }

    var maxArea = 0L

    for (i in points.indices) {
        val p1 = points[i]
        for (j in i + 1 until points.size) {
            val p2 = points[j]

            val dx = abs(p1.x - p2.x) + 1
            val dy = abs(p1.y - p2.y) + 1

            if (dx <= 1 || dy <= 1) continue

            val area = dx.toLong() * dy.toLong()
            if (area > maxArea) {
                maxArea = area
            }
        }
    }

    return maxArea
}

data class CornerTile(val x: Int, val y: Int, val xi: Int, val yi: Int)

fun solveEnclosedRectangles(input: List<String>): Long {
    val rawPoints = input
        .filter { it.isNotBlank() }
        .map { line ->
            val (xs, ys) = line.split(",")
            xs.toInt() to ys.toInt()
        }

    val n = rawPoints.size
    require(n >= 2)

    val xsSorted = rawPoints.map { it.first }.distinct().sorted()
    val ysSorted = rawPoints.map { it.second }.distinct().sorted()

    val compX = mutableListOf<Int>()
    compX.add(xsSorted.first() - 1)

    for (i in xsSorted.indices) {
        val v = xsSorted[i]
        compX.add(v)
        if (i + 1 < xsSorted.size) {
            val next = xsSorted[i + 1]
            if (next > v + 1) {
                compX.add(v + 1)
            }
        }
    }
    compX.add(xsSorted.last() + 1)

    val compY = mutableListOf<Int>()
    compY.add(ysSorted.first() - 1)

    for (i in ysSorted.indices) {
        val v = ysSorted[i]
        compY.add(v)
        if (i + 1 < ysSorted.size) {
            val next = ysSorted[i + 1]
            if (next > v + 1) {
                compY.add(v + 1)
            }
        }
    }
    compY.add(ysSorted.last() + 1)

    val w = compX.size
    val h = compY.size

    val xIndex = compX.withIndex().associate { it.value to it.index }
    val yIndex = compY.withIndex().associate { it.value to it.index }

    val grid = Array(h) { CharArray(w) { '.' } }

    val points = rawPoints.map { (x, y) ->
        val xi = xIndex[x]!!
        val yi = yIndex[y]!!
        grid[yi][xi] = '#'
        CornerTile(x, y, xi, yi)
    }

    for (i in 0 until n) {
        val a = points[i]
        val b = points[(i + 1) % n]

        if (a.xi == b.xi) {
            val x = a.xi
            val y1 = minOf(a.yi, b.yi)
            val y2 = maxOf(a.yi, b.yi)
            for (y in y1 + 1 until y2) {
                if (grid[y][x] == '.') grid[y][x] = 'X'
            }
        } else if (a.yi == b.yi) {
            val y = a.yi
            val x1 = minOf(a.xi, b.xi)
            val x2 = maxOf(a.xi, b.xi)
            for (x in x1 + 1 until x2) {
                if (grid[y][x] == '.') grid[y][x] = 'X'
            }
        } else {
            error("Non-orthogonal segment between $a and $b")
        }
    }

    val outside = Array(h) { BooleanArray(w) }
    val q: ArrayDeque<Pair<Int, Int>> = ArrayDeque()
    outside[0][0] = true
    q.addLast(0 to 0)

    val dirs = arrayOf(
        1 to 0,
        -1 to 0,
        0 to 1,
        0 to -1
    )

    while (q.isNotEmpty()) {
        val (cx, cy) = q.removeFirst()
        for ((dx, dy) in dirs) {
            val nx = cx + dx
            val ny = cy + dy
            if (nx !in 0 until w || ny !in 0 until h) continue
            if (outside[ny][nx]) continue
            if (grid[ny][nx] != '.') continue
            outside[ny][nx] = true
            q.addLast(nx to ny)
        }
    }

    for (y in 0 until h) {
        for (x in 0 until w) {
            if (!outside[y][x] && grid[y][x] == '.') {
                grid[y][x] = 'X'
            }
        }
    }

    val pref = Array(h + 1) { IntArray(w + 1) }
    for (y in 0 until h) {
        var rowSum = 0
        for (x in 0 until w) {
            val v = if (grid[y][x] == '#' || grid[y][x] == 'X') 1 else 0
            rowSum += v
            pref[y + 1][x + 1] = pref[y][x + 1] + rowSum
        }
    }

    fun countColored(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        return pref[y2 + 1][x2 + 1] -
                pref[y1][x2 + 1] -
                pref[y2 + 1][x1] +
                pref[y1][x1]
    }

    var maxArea = 0L

    for (i in 0 until n) {
        val p1 = points[i]
        for (j in i + 1 until n) {
            val p2 = points[j]

            if (p1.xi == p2.xi || p1.yi == p2.yi) continue

            val left = minOf(p1.xi, p2.xi)
            val right = maxOf(p1.xi, p2.xi)
            val top = minOf(p1.yi, p2.yi)
            val bottom = maxOf(p1.yi, p2.yi)

            val cellsWide = right - left + 1
            val cellsHigh = bottom - top + 1
            val cellsCount = cellsWide * cellsHigh

            val widthTiles = abs(p1.x - p2.x).toLong() + 1
            val heightTiles = abs(p1.y - p2.y).toLong() + 1
            val realArea = widthTiles * heightTiles
            if (realArea <= maxArea) continue

            val coloredCount = countColored(left, top, right, bottom)
            if (coloredCount == cellsCount) {
                maxArea = realArea
            }
        }
    }

    return maxArea
}