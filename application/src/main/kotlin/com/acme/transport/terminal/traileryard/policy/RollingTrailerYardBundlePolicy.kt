package com.acme.transport.terminal.traileryard.policy

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingTrailerYardBundle(): Int {
    val policy = defaultPolicy("rolling-trailer-yard-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a rolling trailer yard bundle code in its canonical RTYB- form. */
fun normalizeRollingTrailerYardBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RTYB-")) {
        return clean
    }
    return "RTYB-${clean.replace(" ", "-")}"
}

fun rollingTrailerYardBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 21797.0, in order. */
fun rollingTrailerYardBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 21797.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
