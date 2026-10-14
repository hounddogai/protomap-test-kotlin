package com.acme.transport.freightpay.audit.auditrule

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun lenientAuditSegmentBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun lenientAuditSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.49
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
