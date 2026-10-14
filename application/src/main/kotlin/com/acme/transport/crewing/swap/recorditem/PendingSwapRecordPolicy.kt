package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a pending swap record code in its canonical PSR- form. */
fun normalizePendingSwapRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PSR-")) {
        return clean
    }
    return "PSR-${clean.replace(" ", "-")}"
}

/** Names the band a pending swap record score falls in. */
fun classifyPendingSwapRecord(score: Double): String {
    if (score < 27.0) {
        return "low"
    }
    if (score < 81.6) {
        return "medium"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingSwapRecord(): Int {
    val policy = defaultPolicy("pending-swap-record")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 7664.0, in order. */
fun pendingSwapRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 7664.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
