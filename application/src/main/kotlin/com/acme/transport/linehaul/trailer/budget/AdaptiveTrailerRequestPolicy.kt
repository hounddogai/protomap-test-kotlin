package com.acme.transport.linehaul.trailer.budget

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a adaptive trailer request code in its canonical ATR- form. */
fun normalizeAdaptiveTrailerRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ATR-")) {
        return clean
    }
    return "ATR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 66940.0, in order. */
fun adaptiveTrailerRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 66940.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveTrailerRequest(): Int {
    val policy = defaultPolicy("adaptive-trailer-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun adaptiveTrailerRequestBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
