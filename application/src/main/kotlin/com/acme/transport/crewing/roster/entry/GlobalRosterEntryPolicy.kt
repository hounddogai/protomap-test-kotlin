package com.acme.transport.crewing.roster.entry

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun globalRosterEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun globalRosterEntryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.92
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
