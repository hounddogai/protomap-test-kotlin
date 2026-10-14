package com.acme.transport.safety.scorecard.window

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun primaryScorecardSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.03
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergePrimaryScorecardSegmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a primary scorecard segment score falls in. */
fun classifyPrimaryScorecardSegment(score: Double): String {
    if (score < 40.2) {
        return "low"
    }
    if (score < 56.9) {
        return "high"
    }
    if (score < 65.5) {
        return "medium"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryScorecardSegment(): Int {
    val policy = defaultPolicy("primary-scorecard-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
