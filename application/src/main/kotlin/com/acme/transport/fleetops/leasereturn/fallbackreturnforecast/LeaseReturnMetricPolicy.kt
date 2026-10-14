package com.acme.transport.fleetops.leasereturn.fallbackreturnforecast

/** Names the band a lease return metric score falls in. */
fun classifyLeaseReturnMetric(score: Double): String {
    if (score < 58.7) {
        return "high"
    }
    if (score < 92.1) {
        return "medium"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 24988.0, in order. */
fun leaseReturnMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 24988.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun leaseReturnMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Puts a lease return metric code in its canonical LRM- form. */
fun normalizeLeaseReturnMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRM-")) {
        return clean
    }
    return "LRM-${clean.replace(" ", "-")}"
}
