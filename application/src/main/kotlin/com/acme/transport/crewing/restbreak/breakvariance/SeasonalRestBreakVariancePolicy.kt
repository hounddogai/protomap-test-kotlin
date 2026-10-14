package com.acme.transport.crewing.restbreak.breakvariance

fun seasonalRestBreakVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Puts a seasonal rest break variance code in its canonical SRBV- form. */
fun normalizeSeasonalRestBreakVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRBV-")) {
        return clean
    }
    return "SRBV-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 13580.0, in order. */
fun seasonalRestBreakVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 13580.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a seasonal rest break variance score falls in. */
fun classifySeasonalRestBreakVariance(score: Double): String {
    if (score < 85.1) {
        return "ok"
    }
    if (score < 92.3) {
        return "high"
    }
    return "elevated"
}
