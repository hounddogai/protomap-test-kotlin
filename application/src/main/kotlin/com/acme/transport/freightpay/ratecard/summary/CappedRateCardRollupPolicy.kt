package com.acme.transport.freightpay.ratecard.summary

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedRateCardRollup(): Int {
    val policy = defaultPolicy("capped-rate-card-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a capped rate card rollup code in its canonical CRCR- form. */
fun normalizeCappedRateCardRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CRCR-")) {
        return clean
    }
    return "CRCR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 64622.0, in order. */
fun cappedRateCardRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 64622.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun cappedRateCardRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}
