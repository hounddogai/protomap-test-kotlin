package com.acme.transport.terminal.traileryard.cycle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a global trailer yard quota score falls in. */
fun classifyGlobalTrailerYardQuota(score: Double): String {
    if (score < 8.3) {
        return "ok"
    }
    if (score < 40.2) {
        return "high"
    }
    if (score < 77.0) {
        return "minimal"
    }
    return "low"
}

fun mergeGlobalTrailerYardQuotaLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun globalTrailerYardQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun globalTrailerYardQuotaBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
