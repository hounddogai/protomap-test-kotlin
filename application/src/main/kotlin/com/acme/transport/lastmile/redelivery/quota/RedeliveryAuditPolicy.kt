package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun redeliveryAuditBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun redeliveryAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.17
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
