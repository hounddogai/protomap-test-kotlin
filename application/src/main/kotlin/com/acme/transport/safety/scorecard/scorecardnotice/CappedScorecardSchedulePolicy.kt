package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun cappedScorecardScheduleBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedScorecardSchedule(): Int {
    val policy = defaultPolicy("capped-scorecard-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a capped scorecard schedule code in its canonical CSS- form. */
fun normalizeCappedScorecardScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CSS-")) {
        return clean
    }
    return "CSS-${clean.replace(" ", "-")}"
}

/** Names the band a capped scorecard schedule score falls in. */
fun classifyCappedScorecardSchedule(score: Double): String {
    if (score < 19.9) {
        return "watch"
    }
    if (score < 30.2) {
        return "critical"
    }
    return "ok"
}
