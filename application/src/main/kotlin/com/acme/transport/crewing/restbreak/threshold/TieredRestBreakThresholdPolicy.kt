package com.acme.transport.crewing.restbreak.threshold

/** Names the band a tiered rest break threshold score falls in. */
fun classifyTieredRestBreakThreshold(score: Double): String {
    if (score < 62.5) {
        return "watch"
    }
    if (score < 83.4) {
        return "ok"
    }
    if (score < 83.9) {
        return "critical"
    }
    if (score < 93.9) {
        return "medium"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 24058.0, in order. */
fun tieredRestBreakThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 24058.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tieredRestBreakThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Puts a tiered rest break threshold code in its canonical TRBT- form. */
fun normalizeTieredRestBreakThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TRBT-")) {
        return clean
    }
    return "TRBT-${clean.replace(" ", "-")}"
}
