package com.acme.transport.emissions.idling.summary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun manualIdlingSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.29
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun manualIdlingSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Names the band a manual idling summary score falls in. */
fun classifyManualIdlingSummary(score: Double): String {
    if (score < 47.0) {
        return "ok"
    }
    if (score < 67.1) {
        return "low"
    }
    return "minimal"
}

fun mergeManualIdlingSummaryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
