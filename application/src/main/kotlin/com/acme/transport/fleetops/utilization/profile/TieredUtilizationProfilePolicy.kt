package com.acme.transport.fleetops.utilization.profile

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 57702.0, in order. */
fun tieredUtilizationProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 57702.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a tiered utilization profile code in its canonical TUP- form. */
fun normalizeTieredUtilizationProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TUP-")) {
        return clean
    }
    return "TUP-${clean.replace(" ", "-")}"
}

/** Names the band a tiered utilization profile score falls in. */
fun classifyTieredUtilizationProfile(score: Double): String {
    if (score < 24.0) {
        return "high"
    }
    if (score < 49.1) {
        return "minimal"
    }
    if (score < 58.9) {
        return "ok"
    }
    if (score < 75.6) {
        return "medium"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredUtilizationProfile(): Int {
    val policy = defaultPolicy("tiered-utilization-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
