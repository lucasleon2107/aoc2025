fun main() {
    fun part1(input: List<String>, kConnections: Int): Long {
        return buildLargestCircuitsProduct(input, kConnections)
    }

    fun part2(input: List<String>): Long {
        return findFinalConnectionXProduct(input)
    }

    val testInput = readInput("Day08_test")
    check(part1(testInput, 10) == 40L)
    check(part2(testInput) == 25272L)

    val input = readInput("Day08")
    part1(input, 1000).println()
    part2(input).println()
}

data class Point(val x: Long, val y: Long, val z: Long)
data class Edge(val a: Int, val b: Int, val dist2: Long)

fun buildLargestCircuitsProduct(input: List<String>, kConnections: Int): Long {
    val points = input
        .filter { it.isNotBlank() }
        .map { line ->
            val (x, y, z) = line.split(",")
            Point(x.trim().toLong(), y.trim().toLong(), z.trim().toLong())
        }

    val n = points.size
    if (n == 0) return 0

    // Build all pairwise edges with squared distance
    val edges = ArrayList<Edge>()
    for (i in 0 until n) {
        for (j in i + 1 until n) {
            val p1 = points[i]
            val p2 = points[j]
            val dx = p1.x - p2.x
            val dy = p1.y - p2.y
            val dz = p1.z - p2.z
            val dist2 = dx * dx + dy * dy + dz * dz
            edges += Edge(i, j, dist2)
        }
    }

    edges.sortBy { it.dist2 }

    val dsu = DSU(n)

    var used = 0
    for (e in edges) {
        if (used == kConnections) break
        dsu.union(e.a, e.b)
        used++
    }

    val sizeByRoot = HashMap<Int, Int>()
    for (i in 0 until n) {
        val r = dsu.find(i)
        sizeByRoot[r] = (sizeByRoot[r] ?: 0) + 1
    }

    val sizesDesc = sizeByRoot.values.sortedDescending()
    if (sizesDesc.isEmpty()) return 0

    val top3 = sizesDesc.take(3)
    return top3.fold(1L) { acc, s -> acc * s }
}

class DSU(n: Int) {
    private val parent = IntArray(n) { it }
    private val rank = IntArray(n)

    fun find(x: Int): Int {
        var v = x
        while (parent[v] != v) {
            parent[v] = parent[parent[v]] // path compression
            v = parent[v]
        }
        return v
    }

    fun union(x: Int, y: Int) {
        var rx = find(x)
        var ry = find(y)
        if (rx == ry) return

        if (rank[rx] < rank[ry]) {
            val tmp = rx
            rx = ry
            ry = tmp
        }
        parent[ry] = rx
        if (rank[rx] == rank[ry]) rank[rx]++
    }
}

fun findFinalConnectionXProduct(input: List<String>): Long {
    val points = input
        .filter { it.isNotBlank() }
        .map { line ->
            val (x, y, z) = line.split(",")
            Point(x.trim().toLong(), y.trim().toLong(), z.trim().toLong())
        }

    val n = points.size
    if (n <= 1) return 0

    val edges = ArrayList<Edge>()
    for (i in 0 until n) {
        for (j in i + 1 until n) {
            val p1 = points[i]
            val p2 = points[j]
            val dx = p1.x - p2.x
            val dy = p1.y - p2.y
            val dz = p1.z - p2.z
            val dist2 = dx * dx + dy * dy + dz * dz
            edges += Edge(i, j, dist2)
        }
    }

    edges.sortBy { it.dist2 }

    val dsu = DSU(n)
    var components = n

    for (e in edges) {
        val ra = dsu.find(e.a)
        val rb = dsu.find(e.b)
        if (ra != rb) {
            dsu.union(ra, rb)
            components--

            if (components == 1) {
                val p1 = points[e.a]
                val p2 = points[e.b]
                return p1.x * p2.x
            }
        }
    }

    return 0
}