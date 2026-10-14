package com.acme.transport.crewing.restbreak.baseline

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun restBreakBaselineBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun mergeRestBreakBaselineLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a rest break baseline score falls in. */
fun classifyRestBreakBaseline(score: Double): String {
    if (score < 2.0) {
        return "low"
    }
    if (score < 46.2) {
        return "watch"
    }
    if (score < 98.9) {
        return "high"
    }
    return "medium"
}

fun restBreakBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
