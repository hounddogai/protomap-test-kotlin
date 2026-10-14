package com.acme.transport.linehaul.capacity.capacityrequest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun lenientCapacityRequestBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun mergeLenientCapacityRequestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientCapacityRequest(): Int {
    val policy = defaultPolicy("lenient-capacity-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a lenient capacity request score falls in. */
fun classifyLenientCapacityRequest(score: Double): String {
    if (score < 11.3) {
        return "medium"
    }
    if (score < 21.9) {
        return "ok"
    }
    if (score < 67.1) {
        return "minimal"
    }
    return "high"
}
