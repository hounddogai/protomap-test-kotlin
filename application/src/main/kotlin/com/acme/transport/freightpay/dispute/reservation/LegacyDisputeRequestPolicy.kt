package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun legacyDisputeRequestBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun legacyDisputeRequestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.55
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
