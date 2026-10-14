package com.acme.transport.emissions.routeoffset.ledger

/** Counts how many amounts fit in a budget of 39273.0, in order. */
fun defaultRouteOffsetLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 39273.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a default route offset ledger code in its canonical DROL- form. */
fun normalizeDefaultRouteOffsetLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DROL-")) {
        return clean
    }
    return "DROL-${clean.replace(" ", "-")}"
}

/** Names the band a default route offset ledger score falls in. */
fun classifyDefaultRouteOffsetLedger(score: Double): String {
    if (score < 13.7) {
        return "high"
    }
    if (score < 21.0) {
        return "watch"
    }
    if (score < 36.9) {
        return "elevated"
    }
    if (score < 95.0) {
        return "minimal"
    }
    return "low"
}
