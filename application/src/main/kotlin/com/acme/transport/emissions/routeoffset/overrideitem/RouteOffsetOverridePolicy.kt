package com.acme.transport.emissions.routeoffset.overrideitem

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a route offset override score falls in. */
fun classifyRouteOffsetOverride(score: Double): String {
    if (score < 50.3) {
        return "elevated"
    }
    if (score < 70.0) {
        return "low"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRouteOffsetOverride(): Int {
    val policy = defaultPolicy("route-offset-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun routeOffsetOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.33
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
