package com.acme.transport.customs.broker.bundle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a capped broker variance score falls in. */
fun classifyCappedBrokerVariance(score: Double): String {
    if (score < 13.1) {
        return "low"
    }
    if (score < 17.4) {
        return "elevated"
    }
    if (score < 27.6) {
        return "critical"
    }
    return "watch"
}

fun cappedBrokerVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedBrokerVariance(): Int {
    val policy = defaultPolicy("capped-broker-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeCappedBrokerVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
