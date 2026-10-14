package com.acme.transport.customs.tariff.budget

/** Names the band a tariff template score falls in. */
fun classifyTariffTemplate(score: Double): String {
    if (score < 9.0) {
        return "elevated"
    }
    if (score < 44.9) {
        return "high"
    }
    if (score < 73.5) {
        return "low"
    }
    if (score < 83.5) {
        return "watch"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 18637.0, in order. */
fun tariffTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 18637.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tariffTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
