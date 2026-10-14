package com.acme.transport.fleetops.tire.segment

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a seasonal tire budget score falls in. */
fun classifySeasonalTireBudget(score: Double): String {
    if (score < 51.6) {
        return "low"
    }
    if (score < 78.8) {
        return "watch"
    }
    if (score < 82.5) {
        return "critical"
    }
    return "elevated"
}

/** Puts a seasonal tire budget code in its canonical STB- form. */
fun normalizeSeasonalTireBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STB-")) {
        return clean
    }
    return "STB-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalTireBudget(): Int {
    val policy = defaultPolicy("seasonal-tire-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
