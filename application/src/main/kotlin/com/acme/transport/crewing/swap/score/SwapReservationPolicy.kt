package com.acme.transport.crewing.swap.score

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeSwapReservationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun swapReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a swap reservation score falls in. */
fun classifySwapReservation(score: Double): String {
    if (score < 15.5) {
        return "high"
    }
    if (score < 59.6) {
        return "ok"
    }
    if (score < 93.6) {
        return "medium"
    }
    return "elevated"
}
