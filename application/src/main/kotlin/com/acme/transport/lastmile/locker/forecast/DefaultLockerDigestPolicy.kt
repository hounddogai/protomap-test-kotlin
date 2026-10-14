package com.acme.transport.lastmile.locker.forecast

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a default locker digest score falls in. */
fun classifyDefaultLockerDigest(score: Double): String {
    if (score < 54.7) {
        return "watch"
    }
    if (score < 57.7) {
        return "ok"
    }
    return "low"
}

fun defaultLockerDigestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.15
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
