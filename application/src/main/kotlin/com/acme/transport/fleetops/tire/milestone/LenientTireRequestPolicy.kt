package com.acme.transport.fleetops.tire.milestone

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 12511.0, in order. */
fun lenientTireRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 12511.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a lenient tire request code in its canonical LTR- form. */
fun normalizeLenientTireRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LTR-")) {
        return clean
    }
    return "LTR-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientTireRequest(): Int {
    val policy = defaultPolicy("lenient-tire-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
