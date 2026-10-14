package com.acme.transport.terminal.dwell.schedule

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledDwellSchedule(): Int {
    val policy = defaultPolicy("scheduled-dwell-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun scheduledDwellScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Puts a scheduled dwell schedule code in its canonical SDS- form. */
fun normalizeScheduledDwellScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDS-")) {
        return clean
    }
    return "SDS-${clean.replace(" ", "-")}"
}

fun scheduledDwellScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.08
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
