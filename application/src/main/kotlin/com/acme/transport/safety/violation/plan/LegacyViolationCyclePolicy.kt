package com.acme.transport.safety.violation.plan

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun legacyViolationCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a legacy violation cycle score falls in. */
fun classifyLegacyViolationCycle(score: Double): String {
    if (score < 11.4) {
        return "minimal"
    }
    if (score < 24.9) {
        return "ok"
    }
    if (score < 68.4) {
        return "critical"
    }
    if (score < 90.2) {
        return "elevated"
    }
    return "high"
}

fun mergeLegacyViolationCycleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
