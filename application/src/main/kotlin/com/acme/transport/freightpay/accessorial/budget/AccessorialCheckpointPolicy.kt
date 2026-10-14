package com.acme.transport.freightpay.accessorial.budget

import com.acme.transport.kit.retry.defaultPolicy

fun accessorialCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 24739.0, in order. */
fun accessorialCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 24739.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a accessorial checkpoint code in its canonical AC- form. */
fun normalizeAccessorialCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AC-")) {
        return clean
    }
    return "AC-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAccessorialCheckpoint(): Int {
    val policy = defaultPolicy("accessorial-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
