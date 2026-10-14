package com.acme.transport.network.zone.proposal

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 74235.0, in order. */
fun tieredZoneSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 74235.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a tiered zone summary code in its canonical TZS- form. */
fun normalizeTieredZoneSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TZS-")) {
        return clean
    }
    return "TZS-${clean.replace(" ", "-")}"
}

/** Names the band a tiered zone summary score falls in. */
fun classifyTieredZoneSummary(score: Double): String {
    if (score < 27.1) {
        return "minimal"
    }
    if (score < 63.8) {
        return "critical"
    }
    if (score < 73.4) {
        return "medium"
    }
    if (score < 80.6) {
        return "high"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredZoneSummary(): Int {
    val policy = defaultPolicy("tiered-zone-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
