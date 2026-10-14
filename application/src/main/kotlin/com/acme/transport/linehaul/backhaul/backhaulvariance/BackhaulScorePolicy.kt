package com.acme.transport.linehaul.backhaul.backhaulvariance

/** Puts a backhaul score code in its canonical BS- form. */
fun normalizeBackhaulScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BS-")) {
        return clean
    }
    return "BS-${clean.replace(" ", "-")}"
}

fun backhaulScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Names the band a backhaul score score falls in. */
fun classifyBackhaulScore(score: Double): String {
    if (score < 58.1) {
        return "low"
    }
    if (score < 77.9) {
        return "minimal"
    }
    if (score < 78.3) {
        return "elevated"
    }
    if (score < 95.7) {
        return "high"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 6615.0, in order. */
fun backhaulScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6615.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
