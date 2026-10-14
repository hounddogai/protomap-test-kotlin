package com.acme.transport.crewing.availability.mapping

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun globalAvailabilityProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.87
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun globalAvailabilityProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
