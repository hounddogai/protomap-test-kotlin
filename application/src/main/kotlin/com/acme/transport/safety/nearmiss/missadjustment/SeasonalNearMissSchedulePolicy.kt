package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a seasonal near miss schedule score falls in. */
fun classifySeasonalNearMissSchedule(score: Double): String {
    if (score < 4.7) {
        return "high"
    }
    if (score < 67.0) {
        return "elevated"
    }
    if (score < 76.0) {
        return "minimal"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalNearMissSchedule(): Int {
    val policy = defaultPolicy("seasonal-near-miss-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeSeasonalNearMissScheduleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
