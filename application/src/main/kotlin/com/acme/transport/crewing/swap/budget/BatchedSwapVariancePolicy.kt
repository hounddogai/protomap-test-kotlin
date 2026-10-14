package com.acme.transport.crewing.swap.budget

/** Names the band a batched swap variance score falls in. */
fun classifyBatchedSwapVariance(score: Double): String {
    if (score < 54.7) {
        return "ok"
    }
    if (score < 80.0) {
        return "critical"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 57328.0, in order. */
fun batchedSwapVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 57328.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a batched swap variance code in its canonical BSV- form. */
fun normalizeBatchedSwapVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BSV-")) {
        return clean
    }
    return "BSV-${clean.replace(" ", "-")}"
}
