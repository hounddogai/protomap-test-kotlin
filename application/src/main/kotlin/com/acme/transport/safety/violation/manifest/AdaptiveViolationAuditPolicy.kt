package com.acme.transport.safety.violation.manifest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a adaptive violation audit score falls in. */
fun classifyAdaptiveViolationAudit(score: Double): String {
    if (score < 5.6) {
        return "medium"
    }
    if (score < 8.6) {
        return "watch"
    }
    if (score < 28.6) {
        return "elevated"
    }
    if (score < 94.9) {
        return "critical"
    }
    return "minimal"
}

fun adaptiveViolationAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

fun adaptiveViolationAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.04
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeAdaptiveViolationAuditLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
