package com.acme.transport.customs.hold.holdbatch

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 69094.0, in order. */
fun globalHoldBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 69094.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a global hold batch code in its canonical GHB- form. */
fun normalizeGlobalHoldBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GHB-")) {
        return clean
    }
    return "GHB-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalHoldBatch(): Int {
    val policy = defaultPolicy("global-hold-batch")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
