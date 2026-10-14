package com.acme.transport.emissions.fuelburn.adjustment

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a regional fuel burn target code in its canonical RFBT- form. */
fun normalizeRegionalFuelBurnTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RFBT-")) {
        return clean
    }
    return "RFBT-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalFuelBurnTarget(): Int {
    val policy = defaultPolicy("regional-fuel-burn-target")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a regional fuel burn target score falls in. */
fun classifyRegionalFuelBurnTarget(score: Double): String {
    if (score < 4.0) {
        return "elevated"
    }
    if (score < 79.2) {
        return "minimal"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 19656.0, in order. */
fun regionalFuelBurnTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 19656.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
