package com.acme.transport.lastmile.proofofdelivery.budget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun proofOfDeliverySnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.07
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun proofOfDeliverySnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
