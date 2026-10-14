package com.acme.transport.linehaul.tender.tenderestimate

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a pending tender metric code in its canonical PTM- form. */
fun normalizePendingTenderMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PTM-")) {
        return clean
    }
    return "PTM-${clean.replace(" ", "-")}"
}

/** Names the band a pending tender metric score falls in. */
fun classifyPendingTenderMetric(score: Double): String {
    if (score < 3.2) {
        return "ok"
    }
    if (score < 24.6) {
        return "elevated"
    }
    if (score < 50.8) {
        return "high"
    }
    if (score < 72.4) {
        return "medium"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingTenderMetric(): Int {
    val policy = defaultPolicy("pending-tender-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 35957.0, in order. */
fun pendingTenderMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 35957.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
