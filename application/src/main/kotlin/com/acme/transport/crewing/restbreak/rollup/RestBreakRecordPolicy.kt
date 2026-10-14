package com.acme.transport.crewing.restbreak.rollup

/** Names the band a rest break record score falls in. */
fun classifyRestBreakRecord(score: Double): String {
    if (score < 30.5) {
        return "critical"
    }
    if (score < 62.4) {
        return "ok"
    }
    if (score < 73.5) {
        return "minimal"
    }
    if (score < 77.7) {
        return "low"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 88604.0, in order. */
fun restBreakRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 88604.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a rest break record code in its canonical RBR- form. */
fun normalizeRestBreakRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RBR-")) {
        return clean
    }
    return "RBR-${clean.replace(" ", "-")}"
}

fun restBreakRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
