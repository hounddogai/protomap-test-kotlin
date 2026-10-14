package com.acme.transport.crewing.availability.primaryavailabilitytier

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeManualAvailabilityDraftLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun manualAvailabilityDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualAvailabilityDraft(): Int {
    val policy = defaultPolicy("manual-availability-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a manual availability draft score falls in. */
fun classifyManualAvailabilityDraft(score: Double): String {
    if (score < 81.9) {
        return "ok"
    }
    if (score < 83.5) {
        return "low"
    }
    return "high"
}
