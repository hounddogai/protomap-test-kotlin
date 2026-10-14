package com.acme.transport.linehaul.capacity.capacitymapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeCapacityForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a capacity forecast score falls in. */
fun classifyCapacityForecast(score: Double): String {
    if (score < 15.3) {
        return "high"
    }
    if (score < 27.8) {
        return "medium"
    }
    if (score < 53.8) {
        return "watch"
    }
    if (score < 77.1) {
        return "minimal"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCapacityForecast(): Int {
    val policy = defaultPolicy("capacity-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun capacityForecastBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
