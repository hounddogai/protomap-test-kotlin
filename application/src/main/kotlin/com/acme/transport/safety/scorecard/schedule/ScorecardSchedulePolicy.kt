package com.acme.transport.safety.scorecard.schedule

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScorecardSchedule(): Int {
    val policy = defaultPolicy("scorecard-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a scorecard schedule code in its canonical SS- form. */
fun normalizeScorecardScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SS-")) {
        return clean
    }
    return "SS-${clean.replace(" ", "-")}"
}
