package com.acme.transport.freightpay.accessorial.template

/** Counts how many amounts fit in a budget of 53035.0, in order. */
fun fallbackAccessorialTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 53035.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fallbackAccessorialTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a fallback accessorial template score falls in. */
fun classifyFallbackAccessorialTemplate(score: Double): String {
    if (score < 76.6) {
        return "elevated"
    }
    if (score < 84.7) {
        return "watch"
    }
    return "high"
}
