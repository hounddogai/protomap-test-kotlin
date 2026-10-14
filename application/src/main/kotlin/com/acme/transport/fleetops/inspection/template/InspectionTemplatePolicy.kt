package com.acme.transport.fleetops.inspection.template

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun inspectionTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.43
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun inspectionTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
