package com.acme.transport.customs.broker.tier

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeGlobalBrokerRequestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun globalBrokerRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalBrokerRequest(): Int {
    val policy = defaultPolicy("global-broker-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a global broker request score falls in. */
fun classifyGlobalBrokerRequest(score: Double): String {
    if (score < 9.5) {
        return "high"
    }
    if (score < 29.7) {
        return "medium"
    }
    if (score < 41.7) {
        return "minimal"
    }
    return "critical"
}
