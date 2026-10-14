package com.acme.transport.terminal.sortation.sortationsnapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeRollingSortationClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a rolling sortation claim score falls in. */
fun classifyRollingSortationClaim(score: Double): String {
    if (score < 29.4) {
        return "critical"
    }
    if (score < 75.8) {
        return "high"
    }
    if (score < 84.7) {
        return "elevated"
    }
    if (score < 84.7) {
        return "low"
    }
    return "ok"
}

fun rollingSortationClaimBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun rollingSortationClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.99
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
