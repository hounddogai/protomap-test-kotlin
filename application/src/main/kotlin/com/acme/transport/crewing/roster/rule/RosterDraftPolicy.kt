package com.acme.transport.crewing.roster.rule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a roster draft score falls in. */
fun classifyRosterDraft(score: Double): String {
    if (score < 12.4) {
        return "medium"
    }
    if (score < 39.0) {
        return "watch"
    }
    if (score < 54.7) {
        return "ok"
    }
    if (score < 70.4) {
        return "critical"
    }
    return "low"
}

fun mergeRosterDraftLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRosterDraft(): Int {
    val policy = defaultPolicy("roster-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun rosterDraftBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
