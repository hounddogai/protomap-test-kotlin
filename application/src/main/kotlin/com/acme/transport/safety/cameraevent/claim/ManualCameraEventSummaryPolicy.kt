package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun manualCameraEventSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a manual camera event summary score falls in. */
fun classifyManualCameraEventSummary(score: Double): String {
    if (score < 2.0) {
        return "minimal"
    }
    if (score < 15.6) {
        return "medium"
    }
    if (score < 43.8) {
        return "low"
    }
    if (score < 90.3) {
        return "elevated"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualCameraEventSummary(): Int {
    val policy = defaultPolicy("manual-camera-event-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeManualCameraEventSummaryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
