package com.acme.transport.network.transittime.request

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun transitTimeScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Names the band a transit time score score falls in. */
fun classifyTransitTimeScore(score: Double): String {
    if (score < 10.7) {
        return "low"
    }
    if (score < 14.6) {
        return "medium"
    }
    if (score < 92.0) {
        return "watch"
    }
    return "high"
}

fun mergeTransitTimeScoreLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTransitTimeScore(): Int {
    val policy = defaultPolicy("transit-time-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
