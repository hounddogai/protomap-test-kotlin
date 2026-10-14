package com.acme.transport.lastmile.attempt.attemptrequest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a lenient attempt request score falls in. */
fun classifyLenientAttemptRequest(score: Double): String {
    if (score < 30.2) {
        return "critical"
    }
    if (score < 34.3) {
        return "ok"
    }
    if (score < 78.8) {
        return "minimal"
    }
    return "watch"
}

fun lenientAttemptRequestBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientAttemptRequest(): Int {
    val policy = defaultPolicy("lenient-attempt-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeLenientAttemptRequestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
