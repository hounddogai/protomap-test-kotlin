package com.acme.transport.linehaul.trailer.rule

/** Puts a trailer variance code in its canonical TV- form. */
fun normalizeTrailerVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TV-")) {
        return clean
    }
    return "TV-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 23270.0, in order. */
fun trailerVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 23270.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a trailer variance score falls in. */
fun classifyTrailerVariance(score: Double): String {
    if (score < 1.1) {
        return "high"
    }
    if (score < 60.5) {
        return "medium"
    }
    return "ok"
}

fun trailerVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
