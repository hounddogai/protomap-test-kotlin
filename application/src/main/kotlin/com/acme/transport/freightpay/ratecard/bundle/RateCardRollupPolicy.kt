package com.acme.transport.freightpay.ratecard.bundle

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 15110.0, in order. */
fun rateCardRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 15110.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRateCardRollup(): Int {
    val policy = defaultPolicy("rate-card-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a rate card rollup code in its canonical RCR- form. */
fun normalizeRateCardRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCR-")) {
        return clean
    }
    return "RCR-${clean.replace(" ", "-")}"
}
