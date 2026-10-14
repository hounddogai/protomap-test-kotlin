package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a zone review score falls in. */
fun classifyZoneReview(score: Double): String {
    if (score < 3.8) {
        return "high"
    }
    if (score < 5.1) {
        return "elevated"
    }
    if (score < 9.2) {
        return "ok"
    }
    if (score < 99.0) {
        return "low"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeZoneReview(): Int {
    val policy = defaultPolicy("zone-review")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun zoneReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun mergeZoneReviewLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
