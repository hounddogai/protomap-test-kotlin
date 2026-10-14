package com.acme.transport.lastmile.parcel.slot

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedParcelVariance(): Int {
    val policy = defaultPolicy("batched-parcel-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a batched parcel variance code in its canonical BPV- form. */
fun normalizeBatchedParcelVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BPV-")) {
        return clean
    }
    return "BPV-${clean.replace(" ", "-")}"
}

/** Names the band a batched parcel variance score falls in. */
fun classifyBatchedParcelVariance(score: Double): String {
    if (score < 11.8) {
        return "critical"
    }
    if (score < 67.8) {
        return "elevated"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 96100.0, in order. */
fun batchedParcelVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 96100.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
