package com.acme.transport.customs.tariff.profile

/** Counts how many amounts fit in a budget of 70923.0, in order. */
fun tariffRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 70923.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tariffRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a tariff rule score falls in. */
fun classifyTariffRule(score: Double): String {
    if (score < 28.5) {
        return "watch"
    }
    if (score < 39.6) {
        return "medium"
    }
    if (score < 66.4) {
        return "minimal"
    }
    if (score < 91.6) {
        return "elevated"
    }
    return "critical"
}
