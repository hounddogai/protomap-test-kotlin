package com.acme.transport.customs.broker.threshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun scheduledBrokerThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.54
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeScheduledBrokerThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledBrokerThreshold(): Int {
    val policy = defaultPolicy("scheduled-broker-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a scheduled broker threshold score falls in. */
fun classifyScheduledBrokerThreshold(score: Double): String {
    if (score < 14.6) {
        return "minimal"
    }
    if (score < 63.0) {
        return "low"
    }
    if (score < 81.7) {
        return "ok"
    }
    return "elevated"
}
