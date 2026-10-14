package com.acme.transport.lastmile.locker.forecast

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLockerForecast(): Int {
    val policy = defaultPolicy("locker-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a locker forecast code in its canonical LF- form. */
fun normalizeLockerForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LF-")) {
        return clean
    }
    return "LF-${clean.replace(" ", "-")}"
}

/** Names the band a locker forecast score falls in. */
fun classifyLockerForecast(score: Double): String {
    if (score < 76.4) {
        return "minimal"
    }
    if (score < 96.6) {
        return "elevated"
    }
    return "high"
}
