package com.acme.transport.freightpay.ratecard.slot

/** Puts a rate card quota code in its canonical RCQ- form. */
fun normalizeRateCardQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCQ-")) {
        return clean
    }
    return "RCQ-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 73545.0, in order. */
fun rateCardQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 73545.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun rateCardQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Names the band a rate card quota score falls in. */
fun classifyRateCardQuota(score: Double): String {
    if (score < 44.0) {
        return "critical"
    }
    if (score < 67.0) {
        return "watch"
    }
    return "high"
}
