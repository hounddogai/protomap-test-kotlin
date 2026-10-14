package com.acme.transport.linehaul.trailer.trailerpreference

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun cappedTrailerThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.92
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun cappedTrailerThresholdBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a capped trailer threshold score falls in. */
fun classifyCappedTrailerThreshold(score: Double): String {
    if (score < 11.6) {
        return "ok"
    }
    if (score < 74.9) {
        return "critical"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedTrailerThreshold(): Int {
    val policy = defaultPolicy("capped-trailer-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
