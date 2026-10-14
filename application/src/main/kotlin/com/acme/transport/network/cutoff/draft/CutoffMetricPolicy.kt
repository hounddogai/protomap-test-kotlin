package com.acme.transport.network.cutoff.draft

/** Puts a cutoff metric code in its canonical CM- form. */
fun normalizeCutoffMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CM-")) {
        return clean
    }
    return "CM-${clean.replace(" ", "-")}"
}

fun cutoffMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Counts how many amounts fit in a budget of 92837.0, in order. */
fun cutoffMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 92837.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a cutoff metric score falls in. */
fun classifyCutoffMetric(score: Double): String {
    if (score < 21.3) {
        return "medium"
    }
    if (score < 36.5) {
        return "ok"
    }
    if (score < 72.7) {
        return "high"
    }
    return "minimal"
}
