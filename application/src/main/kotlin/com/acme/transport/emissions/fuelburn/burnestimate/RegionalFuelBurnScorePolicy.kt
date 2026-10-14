package com.acme.transport.emissions.fuelburn.burnestimate

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalFuelBurnScore(): Int {
    val policy = defaultPolicy("regional-fuel-burn-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 17773.0, in order. */
fun regionalFuelBurnScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 17773.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a regional fuel burn score code in its canonical RFBS- form. */
fun normalizeRegionalFuelBurnScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RFBS-")) {
        return clean
    }
    return "RFBS-${clean.replace(" ", "-")}"
}

/** Names the band a regional fuel burn score score falls in. */
fun classifyRegionalFuelBurnScore(score: Double): String {
    if (score < 27.6) {
        return "high"
    }
    if (score < 33.5) {
        return "watch"
    }
    if (score < 36.8) {
        return "medium"
    }
    if (score < 80.6) {
        return "low"
    }
    return "ok"
}
