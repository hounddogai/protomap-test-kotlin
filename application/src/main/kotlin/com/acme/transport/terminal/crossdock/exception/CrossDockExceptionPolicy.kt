package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a cross dock exception score falls in. */
fun classifyCrossDockException(score: Double): String {
    if (score < 2.2) {
        return "high"
    }
    if (score < 63.1) {
        return "minimal"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCrossDockException(): Int {
    val policy = defaultPolicy("cross-dock-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun crossDockExceptionBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun crossDockExceptionWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.81
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
