package com.acme.transport.lastmile.attempt.tier

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun globalAttemptRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.58
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeGlobalAttemptRecordLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a global attempt record score falls in. */
fun classifyGlobalAttemptRecord(score: Double): String {
    if (score < 12.2) {
        return "elevated"
    }
    if (score < 25.9) {
        return "high"
    }
    if (score < 48.1) {
        return "ok"
    }
    if (score < 49.5) {
        return "minimal"
    }
    return "low"
}
