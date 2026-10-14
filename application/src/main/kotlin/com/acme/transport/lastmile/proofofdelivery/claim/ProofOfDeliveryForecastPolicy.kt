package com.acme.transport.lastmile.proofofdelivery.claim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeProofOfDeliveryForecast(): Int {
    val policy = defaultPolicy("proof-of-delivery-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun proofOfDeliveryForecastBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun mergeProofOfDeliveryForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun proofOfDeliveryForecastWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.19
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
