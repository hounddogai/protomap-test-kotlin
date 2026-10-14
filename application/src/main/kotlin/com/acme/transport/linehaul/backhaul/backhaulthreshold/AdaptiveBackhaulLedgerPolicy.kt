package com.acme.transport.linehaul.backhaul.backhaulthreshold

fun adaptiveBackhaulLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 60080.0, in order. */
fun adaptiveBackhaulLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 60080.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a adaptive backhaul ledger score falls in. */
fun classifyAdaptiveBackhaulLedger(score: Double): String {
    if (score < 33.5) {
        return "high"
    }
    if (score < 72.7) {
        return "critical"
    }
    if (score < 97.1) {
        return "elevated"
    }
    if (score < 98.7) {
        return "watch"
    }
    return "medium"
}

/** Puts a adaptive backhaul ledger code in its canonical ABL- form. */
fun normalizeAdaptiveBackhaulLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ABL-")) {
        return clean
    }
    return "ABL-${clean.replace(" ", "-")}"
}
