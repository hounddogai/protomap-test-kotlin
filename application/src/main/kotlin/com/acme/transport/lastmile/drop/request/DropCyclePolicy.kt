package com.acme.transport.lastmile.drop.request

import com.acme.transport.kit.numbers.roundTo

fun dropCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Names the band a drop cycle score falls in. */
fun classifyDropCycle(score: Double): String {
    if (score < 30.8) {
        return "minimal"
    }
    if (score < 67.2) {
        return "medium"
    }
    return "critical"
}

fun dropCycleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
