package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a scheduled roster schedule score falls in. */
fun classifyScheduledRosterSchedule(score: Double): String {
    if (score < 22.1) {
        return "watch"
    }
    if (score < 24.0) {
        return "high"
    }
    if (score < 86.9) {
        return "critical"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledRosterSchedule(): Int {
    val policy = defaultPolicy("scheduled-roster-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a scheduled roster schedule code in its canonical SRS- form. */
fun normalizeScheduledRosterScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRS-")) {
        return clean
    }
    return "SRS-${clean.replace(" ", "-")}"
}

fun scheduledRosterScheduleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
