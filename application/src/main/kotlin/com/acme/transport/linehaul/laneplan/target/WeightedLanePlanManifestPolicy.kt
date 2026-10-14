package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun weightedLanePlanManifestBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedLanePlanManifest(): Int {
    val policy = defaultPolicy("weighted-lane-plan-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun weightedLanePlanManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.19
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
