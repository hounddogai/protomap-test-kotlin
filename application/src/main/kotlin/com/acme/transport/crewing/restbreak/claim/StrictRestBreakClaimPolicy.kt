package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 47193.0, in order. */
fun strictRestBreakClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 47193.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictRestBreakClaim(): Int {
    val policy = defaultPolicy("strict-rest-break-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a strict rest break claim code in its canonical SRBC- form. */
fun normalizeStrictRestBreakClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRBC-")) {
        return clean
    }
    return "SRBC-${clean.replace(" ", "-")}"
}
