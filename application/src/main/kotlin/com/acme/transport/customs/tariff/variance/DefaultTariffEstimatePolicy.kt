package com.acme.transport.customs.tariff.variance

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultTariffEstimate(): Int {
    val policy = defaultPolicy("default-tariff-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a default tariff estimate code in its canonical DTE- form. */
fun normalizeDefaultTariffEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DTE-")) {
        return clean
    }
    return "DTE-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 34988.0, in order. */
fun defaultTariffEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 34988.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
