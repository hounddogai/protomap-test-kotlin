package com.acme.transport.emissions.evcharge.chargeclaim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun strictEvChargeCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.79
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeStrictEvChargeCheckpointLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictEvChargeCheckpoint(): Int {
    val policy = defaultPolicy("strict-ev-charge-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a strict ev charge checkpoint score falls in. */
fun classifyStrictEvChargeCheckpoint(score: Double): String {
    if (score < 32.5) {
        return "critical"
    }
    if (score < 39.2) {
        return "low"
    }
    if (score < 43.9) {
        return "high"
    }
    if (score < 68.2) {
        return "ok"
    }
    return "medium"
}
