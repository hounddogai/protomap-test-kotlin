package com.acme.transport.lastmile.drop.recorditem

/** Counts how many amounts fit in a budget of 27611.0, in order. */
fun globalDropExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 27611.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a global drop exception score falls in. */
fun classifyGlobalDropException(score: Double): String {
    if (score < 9.0) {
        return "watch"
    }
    if (score < 66.4) {
        return "ok"
    }
    if (score < 87.8) {
        return "critical"
    }
    if (score < 98.3) {
        return "medium"
    }
    return "elevated"
}

/** Puts a global drop exception code in its canonical GDE- form. */
fun normalizeGlobalDropExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GDE-")) {
        return clean
    }
    return "GDE-${clean.replace(" ", "-")}"
}

fun globalDropExceptionSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
