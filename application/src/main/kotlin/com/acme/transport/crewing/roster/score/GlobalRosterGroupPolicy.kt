package com.acme.transport.crewing.roster.score

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun globalRosterGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.81
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalRosterGroup(): Int {
    val policy = defaultPolicy("global-roster-group")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeGlobalRosterGroupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a global roster group score falls in. */
fun classifyGlobalRosterGroup(score: Double): String {
    if (score < 4.2) {
        return "high"
    }
    if (score < 13.3) {
        return "ok"
    }
    if (score < 51.2) {
        return "medium"
    }
    if (score < 93.9) {
        return "critical"
    }
    return "minimal"
}
