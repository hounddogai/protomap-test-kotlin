package com.acme.transport.lastmile.locker.cycle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeLockerSummaryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun lockerSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.5
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a locker summary score falls in. */
fun classifyLockerSummary(score: Double): String {
    if (score < 37.9) {
        return "high"
    }
    if (score < 47.0) {
        return "minimal"
    }
    if (score < 82.6) {
        return "medium"
    }
    if (score < 86.9) {
        return "ok"
    }
    return "elevated"
}

fun lockerSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
