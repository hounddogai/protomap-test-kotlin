package com.acme.transport.network.zone.digest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a seasonal zone review score falls in. */
fun classifySeasonalZoneReview(score: Double): String {
    if (score < 15.0) {
        return "elevated"
    }
    if (score < 28.4) {
        return "ok"
    }
    if (score < 43.3) {
        return "medium"
    }
    if (score < 65.3) {
        return "minimal"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalZoneReview(): Int {
    val policy = defaultPolicy("seasonal-zone-review")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun seasonalZoneReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun mergeSeasonalZoneReviewLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
