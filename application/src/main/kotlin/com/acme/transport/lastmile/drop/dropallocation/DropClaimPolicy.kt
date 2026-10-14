package com.acme.transport.lastmile.drop.dropallocation

import com.acme.transport.kit.retry.defaultPolicy

fun dropClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Puts a drop claim code in its canonical DC- form. */
fun normalizeDropClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DC-")) {
        return clean
    }
    return "DC-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 55436.0, in order. */
fun dropClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 55436.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDropClaim(): Int {
    val policy = defaultPolicy("drop-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
