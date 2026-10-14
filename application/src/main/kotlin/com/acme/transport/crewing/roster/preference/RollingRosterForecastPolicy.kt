package com.acme.transport.crewing.roster.preference

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a rolling roster forecast code in its canonical RRF- form. */
fun normalizeRollingRosterForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RRF-")) {
        return clean
    }
    return "RRF-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingRosterForecast(): Int {
    val policy = defaultPolicy("rolling-roster-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
