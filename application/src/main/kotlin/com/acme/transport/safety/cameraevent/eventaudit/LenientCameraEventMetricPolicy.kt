package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeLenientCameraEventMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientCameraEventMetric(): Int {
    val policy = defaultPolicy("lenient-camera-event-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun lenientCameraEventMetricBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a lenient camera event metric score falls in. */
fun classifyLenientCameraEventMetric(score: Double): String {
    if (score < 15.4) {
        return "high"
    }
    if (score < 45.7) {
        return "minimal"
    }
    return "medium"
}
