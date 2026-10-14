package com.acme.transport.crewing.restbreak.schedule

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a pending rest break schedule score falls in. */
fun classifyPendingRestBreakSchedule(score: Double): String {
    if (score < 67.9) {
        return "critical"
    }
    if (score < 88.2) {
        return "low"
    }
    return "high"
}

/** Puts a pending rest break schedule code in its canonical PRBS- form. */
fun normalizePendingRestBreakScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PRBS-")) {
        return clean
    }
    return "PRBS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingRestBreakSchedule(): Int {
    val policy = defaultPolicy("pending-rest-break-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun pendingRestBreakScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
