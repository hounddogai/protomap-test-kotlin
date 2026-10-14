package com.acme.transport.linehaul.relay.relaymapping

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun primaryRelayRequestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.89
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun primaryRelayRequestBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
