package com.acme.transport.lastmile.drop.recorditem

import com.acme.transport.kit.numbers.roundTo

fun dropTargetBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a drop target score falls in. */
fun classifyDropTarget(score: Double): String {
    if (score < 5.5) {
        return "high"
    }
    if (score < 31.3) {
        return "elevated"
    }
    if (score < 69.0) {
        return "low"
    }
    return "medium"
}

fun dropTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
