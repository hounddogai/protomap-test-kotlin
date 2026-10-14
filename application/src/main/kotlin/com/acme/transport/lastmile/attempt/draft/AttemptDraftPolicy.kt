package com.acme.transport.lastmile.attempt.draft

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun attemptDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.68
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeAttemptDraftLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a attempt draft score falls in. */
fun classifyAttemptDraft(score: Double): String {
    if (score < 24.7) {
        return "critical"
    }
    if (score < 45.7) {
        return "elevated"
    }
    if (score < 53.9) {
        return "high"
    }
    if (score < 77.4) {
        return "minimal"
    }
    return "ok"
}
