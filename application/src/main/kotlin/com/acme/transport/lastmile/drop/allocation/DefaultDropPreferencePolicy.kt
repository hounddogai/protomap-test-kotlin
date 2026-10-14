package com.acme.transport.lastmile.drop.allocation

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun defaultDropPreferenceBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultDropPreference(): Int {
    val policy = defaultPolicy("default-drop-preference")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a default drop preference score falls in. */
fun classifyDefaultDropPreference(score: Double): String {
    if (score < 44.2) {
        return "high"
    }
    if (score < 46.5) {
        return "ok"
    }
    if (score < 49.2) {
        return "low"
    }
    if (score < 82.2) {
        return "elevated"
    }
    return "minimal"
}

fun defaultDropPreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.58
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
