package com.acme.transport.emissions.evcharge.chargeclaim

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun evChargeReviewBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun evChargeReviewWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.37
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
