package com.acme.transport.terminal.dwell.preference

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDwellSegment(): Int {
    val policy = defaultPolicy("dwell-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a dwell segment code in its canonical DS- form. */
fun normalizeDwellSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DS-")) {
        return clean
    }
    return "DS-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 86061.0, in order. */
fun dwellSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 86061.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
