package com.acme.transport.crewing.roster.exception

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun scheduledRosterCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

fun mergeScheduledRosterCycleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun scheduledRosterCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.84
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
