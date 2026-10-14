package com.acme.transport.emissions.evcharge.review

import com.acme.transport.kit.retry.defaultPolicy

fun legacyEvChargeOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 24408.0, in order. */
fun legacyEvChargeOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 24408.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyEvChargeOverride(): Int {
    val policy = defaultPolicy("legacy-ev-charge-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a legacy ev charge override code in its canonical LECO- form. */
fun normalizeLegacyEvChargeOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LECO-")) {
        return clean
    }
    return "LECO-${clean.replace(" ", "-")}"
}
