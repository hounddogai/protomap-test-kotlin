package com.acme.transport.terminal.crossdock.dockestimate

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingCrossDockScore(): Int {
    val policy = defaultPolicy("rolling-cross-dock-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun rollingCrossDockScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.51
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
