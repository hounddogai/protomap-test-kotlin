package com.acme.transport.network.node.policy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeRollingNodeReservationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a rolling node reservation score falls in. */
fun classifyRollingNodeReservation(score: Double): String {
    if (score < 25.5) {
        return "watch"
    }
    if (score < 50.0) {
        return "medium"
    }
    if (score < 83.6) {
        return "ok"
    }
    return "minimal"
}

fun rollingNodeReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
