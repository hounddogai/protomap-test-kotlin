package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedParcelAdjustment(): Int {
    val policy = defaultPolicy("batched-parcel-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a batched parcel adjustment code in its canonical BPA- form. */
fun normalizeBatchedParcelAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BPA-")) {
        return clean
    }
    return "BPA-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 93950.0, in order. */
fun batchedParcelAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93950.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a batched parcel adjustment score falls in. */
fun classifyBatchedParcelAdjustment(score: Double): String {
    if (score < 10.3) {
        return "watch"
    }
    if (score < 48.8) {
        return "ok"
    }
    return "medium"
}
