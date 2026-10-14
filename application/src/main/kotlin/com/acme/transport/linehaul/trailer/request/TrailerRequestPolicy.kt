package com.acme.transport.linehaul.trailer.request

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 13938.0, in order. */
fun trailerRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 13938.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a trailer request code in its canonical TR- form. */
fun normalizeTrailerRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TR-")) {
        return clean
    }
    return "TR-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTrailerRequest(): Int {
    val policy = defaultPolicy("trailer-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
