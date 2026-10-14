package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.kit.numbers.roundTo

/** Names the band a cross dock cycle score falls in. */
fun classifyCrossDockCycle(score: Double): String {
    if (score < 45.1) {
        return "high"
    }
    if (score < 53.6) {
        return "medium"
    }
    if (score < 88.6) {
        return "low"
    }
    return "watch"
}

fun crossDockCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun crossDockCycleBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
