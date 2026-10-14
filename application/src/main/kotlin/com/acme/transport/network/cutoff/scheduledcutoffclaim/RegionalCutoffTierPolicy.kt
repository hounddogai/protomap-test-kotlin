package com.acme.transport.network.cutoff.scheduledcutoffclaim

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 65378.0, in order. */
fun regionalCutoffTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 65378.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a regional cutoff tier code in its canonical RCT- form. */
fun normalizeRegionalCutoffTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCT-")) {
        return clean
    }
    return "RCT-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalCutoffTier(): Int {
    val policy = defaultPolicy("regional-cutoff-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
