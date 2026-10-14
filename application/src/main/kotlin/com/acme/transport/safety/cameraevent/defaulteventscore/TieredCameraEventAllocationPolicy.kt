package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a tiered camera event allocation score falls in. */
fun classifyTieredCameraEventAllocation(score: Double): String {
    if (score < 1.4) {
        return "low"
    }
    if (score < 38.0) {
        return "ok"
    }
    if (score < 41.9) {
        return "medium"
    }
    if (score < 48.6) {
        return "watch"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredCameraEventAllocation(): Int {
    val policy = defaultPolicy("tiered-camera-event-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun tieredCameraEventAllocationBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun mergeTieredCameraEventAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
