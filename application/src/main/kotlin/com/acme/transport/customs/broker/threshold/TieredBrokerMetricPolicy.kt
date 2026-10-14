package com.acme.transport.customs.broker.threshold

import com.acme.transport.kit.numbers.roundTo

/** Puts a tiered broker metric code in its canonical TBM- form. */
fun normalizeTieredBrokerMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TBM-")) {
        return clean
    }
    return "TBM-${clean.replace(" ", "-")}"
}

fun tieredBrokerMetricBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a tiered broker metric score falls in. */
fun classifyTieredBrokerMetric(score: Double): String {
    if (score < 20.1) {
        return "elevated"
    }
    if (score < 25.7) {
        return "medium"
    }
    if (score < 93.9) {
        return "critical"
    }
    if (score < 98.3) {
        return "low"
    }
    return "minimal"
}

/** Counts how many amounts fit in a budget of 44983.0, in order. */
fun tieredBrokerMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 44983.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
