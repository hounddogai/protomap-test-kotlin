package com.acme.transport.customs.inspection.budget

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedInspectionBudget(): Int {
    val policy = defaultPolicy("batched-inspection-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a batched inspection budget score falls in. */
fun classifyBatchedInspectionBudget(score: Double): String {
    if (score < 62.6) {
        return "watch"
    }
    if (score < 98.4) {
        return "elevated"
    }
    return "ok"
}

/** Puts a batched inspection budget code in its canonical BIB- form. */
fun normalizeBatchedInspectionBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BIB-")) {
        return clean
    }
    return "BIB-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 97439.0, in order. */
fun batchedInspectionBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 97439.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
