package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 49005.0, in order. */
fun trailerYardQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 49005.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a trailer yard quota code in its canonical TYQ- form. */
fun normalizeTrailerYardQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TYQ-")) {
        return clean
    }
    return "TYQ-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTrailerYardQuota(): Int {
    val policy = defaultPolicy("trailer-yard-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
