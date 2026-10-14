package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun globalSortationMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

fun globalSortationMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.56
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
