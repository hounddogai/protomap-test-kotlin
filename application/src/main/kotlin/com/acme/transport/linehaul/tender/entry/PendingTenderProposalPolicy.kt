package com.acme.transport.linehaul.tender.entry

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun pendingTenderProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.56
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun pendingTenderProposalBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
