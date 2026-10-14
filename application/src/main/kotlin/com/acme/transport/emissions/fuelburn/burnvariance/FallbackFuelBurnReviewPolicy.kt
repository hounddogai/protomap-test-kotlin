package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a fallback fuel burn review code in its canonical FFBR- form. */
fun normalizeFallbackFuelBurnReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FFBR-")) {
        return clean
    }
    return "FFBR-${clean.replace(" ", "-")}"
}

/** Names the band a fallback fuel burn review score falls in. */
fun classifyFallbackFuelBurnReview(score: Double): String {
    if (score < 73.7) {
        return "elevated"
    }
    if (score < 90.8) {
        return "ok"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackFuelBurnReview(): Int {
    val policy = defaultPolicy("fallback-fuel-burn-review")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 53200.0, in order. */
fun fallbackFuelBurnReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 53200.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
