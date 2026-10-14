package com.acme.transport.safety.violation.adjustment

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 87612.0, in order. */
fun rollingViolationBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 87612.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a rolling violation batch code in its canonical RVB- form. */
fun normalizeRollingViolationBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RVB-")) {
        return clean
    }
    return "RVB-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingViolationBatch(): Int {
    val policy = defaultPolicy("rolling-violation-batch")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
