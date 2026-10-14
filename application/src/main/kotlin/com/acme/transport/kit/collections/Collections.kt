package com.acme.transport.kit.collections

// Generic helpers over lists and maps.

/** Returns the items keep accepts, in order. */
fun <T> filter(items: MutableList<T>, keep: (T) -> Boolean): MutableList<T> {
    val out = mutableListOf<T>()
    for (item in items) {
        if (keep(item)) {
            out.add(item)
        }
    }
    return out
}

fun <T> countBy(items: MutableList<T>, key: (T) -> String): MutableMap<String, Int> {
    val counts = mutableMapOf<String, Int>()
    for (item in items) {
        counts[key(item)] = (counts[key(item)] ?: 0) + 1
    }
    return counts
}

/** Groups items by key, keeping each group in input order. */
fun <T> groupBy(items: MutableList<T>, key: (T) -> String): MutableMap<String, MutableList<T>> {
    val groups = mutableMapOf<String, MutableList<T>>()
    for (item in items) {
        groups.getOrPut(key(item)) { mutableListOf<T>() }.add(item)
    }
    return groups
}

fun <T> first(items: MutableList<T>, pred: (T) -> Boolean): T? {
    for (item in items) {
        if (pred(item)) {
            return item
        }
    }
    return null
}

fun <T> anyMatch(items: MutableList<T>, pred: (T) -> Boolean): Boolean {
    for (item in items) {
        if (pred(item)) {
            return true
        }
    }
    return false
}

fun <T> allMatch(items: MutableList<T>, pred: (T) -> Boolean): Boolean {
    for (item in items) {
        if (!pred(item)) {
            return false
        }
    }
    return true
}

fun <T> sumBy(items: MutableList<T>, measure: (T) -> Double): Double {
    var total = 0.0
    for (item in items) {
        total += measure(item)
    }
    return total
}

/** Splits items into batches of at most size, the last one shorter. */
fun <T> chunk(items: MutableList<T>, size: Int): MutableList<MutableList<T>> {
    val chunks = mutableListOf<MutableList<T>>()
    if (size <= 0) {
        return chunks
    }
    var current = mutableListOf<T>()
    for (item in items) {
        current.add(item)
        if (current.size == size) {
            chunks.add(current)
            current = mutableListOf<T>()
        }
    }
    if (current.size > 0) {
        chunks.add(current)
    }
    return chunks
}

/** Returns a copy of items ordered by score, highest first. The sort is stable. */
fun <T> sortedBy(items: MutableList<T>, score: (T) -> Double): MutableList<T> {
    val sorted = items.toMutableList()
    for (i in 0 until sorted.size) {
        var j = i
        while (j > 0 && score(sorted[j - 1]) < score(sorted[j])) {
            val swap: T = sorted[j]
            sorted[j] = sorted[j - 1]
            sorted[j - 1] = swap
            j = j - 1
        }
    }
    return sorted
}

/** Drops repeated values, keeping the first of each. */
fun uniqueStrings(values: MutableList<String>): MutableList<String> {
    val seen = mutableMapOf<String, Boolean>()
    val out = mutableListOf<String>()
    for (value in values) {
        if (seen.containsKey(value)) {
            continue
        }
        seen[value] = true
        out.add(value)
    }
    return out
}

fun <T> take(items: MutableList<T>, count: Int): MutableList<T> {
    val out = mutableListOf<T>()
    for (item in items) {
        if (out.size >= count) {
            break
        }
        out.add(item)
    }
    return out
}
