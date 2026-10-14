package com.acme.transport.lastmile.redelivery.redeliveryrule

/** Names the band a tiered redelivery bundle score falls in. */
fun classifyTieredRedeliveryBundle(score: Double): String {
    if (score < 12.0) {
        return "minimal"
    }
    if (score < 60.3) {
        return "high"
    }
    if (score < 85.6) {
        return "ok"
    }
    if (score < 92.1) {
        return "low"
    }
    return "critical"
}

fun tieredRedeliveryBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 30509.0, in order. */
fun tieredRedeliveryBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 30509.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
