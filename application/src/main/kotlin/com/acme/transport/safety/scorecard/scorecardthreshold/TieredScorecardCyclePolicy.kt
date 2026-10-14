package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun tieredScorecardCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredScorecardCycle(): Int {
    val policy = defaultPolicy("tiered-scorecard-cycle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a tiered scorecard cycle score falls in. */
fun classifyTieredScorecardCycle(score: Double): String {
    if (score < 12.4) {
        return "high"
    }
    if (score < 25.3) {
        return "ok"
    }
    if (score < 33.9) {
        return "low"
    }
    if (score < 82.9) {
        return "elevated"
    }
    return "medium"
}

fun mergeTieredScorecardCycleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
