package com.acme.transport.linehaul.tender.review

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a tiered tender budget score falls in. */
fun classifyTieredTenderBudget(score: Double): String {
    if (score < 25.9) {
        return "high"
    }
    if (score < 52.9) {
        return "elevated"
    }
    if (score < 62.9) {
        return "ok"
    }
    if (score < 92.1) {
        return "low"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 76681.0, in order. */
fun tieredTenderBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 76681.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a tiered tender budget code in its canonical TTB- form. */
fun normalizeTieredTenderBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TTB-")) {
        return clean
    }
    return "TTB-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredTenderBudget(): Int {
    val policy = defaultPolicy("tiered-tender-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
