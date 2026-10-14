package com.acme.transport.safety.violation.violationledger

fun lenientViolationLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a lenient violation ledger score falls in. */
fun classifyLenientViolationLedger(score: Double): String {
    if (score < 60.4) {
        return "low"
    }
    if (score < 76.9) {
        return "watch"
    }
    if (score < 96.8) {
        return "critical"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 78773.0, in order. */
fun lenientViolationLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 78773.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a lenient violation ledger code in its canonical LVL- form. */
fun normalizeLenientViolationLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LVL-")) {
        return clean
    }
    return "LVL-${clean.replace(" ", "-")}"
}
