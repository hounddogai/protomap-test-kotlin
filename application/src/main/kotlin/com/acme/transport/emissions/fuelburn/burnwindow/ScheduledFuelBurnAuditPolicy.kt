package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a scheduled fuel burn audit score falls in. */
fun classifyScheduledFuelBurnAudit(score: Double): String {
    if (score < 40.9) {
        return "low"
    }
    if (score < 92.7) {
        return "elevated"
    }
    if (score < 94.1) {
        return "high"
    }
    return "critical"
}

fun scheduledFuelBurnAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun mergeScheduledFuelBurnAuditLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
