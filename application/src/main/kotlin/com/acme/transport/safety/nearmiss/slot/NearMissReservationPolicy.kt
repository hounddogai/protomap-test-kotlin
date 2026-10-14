package com.acme.transport.safety.nearmiss.slot

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun nearMissReservationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.97
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun nearMissReservationBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
