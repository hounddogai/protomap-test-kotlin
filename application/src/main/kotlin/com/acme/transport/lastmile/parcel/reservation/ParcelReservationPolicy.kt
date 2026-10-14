package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun parcelReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Names the band a parcel reservation score falls in. */
fun classifyParcelReservation(score: Double): String {
    if (score < 11.2) {
        return "low"
    }
    if (score < 23.2) {
        return "ok"
    }
    if (score < 30.7) {
        return "minimal"
    }
    if (score < 75.3) {
        return "elevated"
    }
    return "high"
}

fun parcelReservationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.34
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun parcelReservationBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
