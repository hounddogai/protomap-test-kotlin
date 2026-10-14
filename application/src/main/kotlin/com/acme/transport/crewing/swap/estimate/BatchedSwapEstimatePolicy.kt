package com.acme.transport.crewing.swap.estimate

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a batched swap estimate score falls in. */
fun classifyBatchedSwapEstimate(score: Double): String {
    if (score < 36.5) {
        return "elevated"
    }
    if (score < 47.8) {
        return "high"
    }
    if (score < 63.3) {
        return "medium"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedSwapEstimate(): Int {
    val policy = defaultPolicy("batched-swap-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 22065.0, in order. */
fun batchedSwapEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 22065.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a batched swap estimate code in its canonical BSE- form. */
fun normalizeBatchedSwapEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BSE-")) {
        return clean
    }
    return "BSE-${clean.replace(" ", "-")}"
}
