package com.acme.transport.crewing.roster.regionalrostersummary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a legacy roster schedule score falls in. */
fun classifyLegacyRosterSchedule(score: Double): String {
    if (score < 8.9) {
        return "watch"
    }
    if (score < 27.4) {
        return "elevated"
    }
    if (score < 42.9) {
        return "minimal"
    }
    return "ok"
}

fun legacyRosterScheduleBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun mergeLegacyRosterScheduleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyRosterSchedule(): Int {
    val policy = defaultPolicy("legacy-roster-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
