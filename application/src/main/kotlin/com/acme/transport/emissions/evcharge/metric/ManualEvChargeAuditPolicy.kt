package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 94710.0, in order. */
fun manualEvChargeAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 94710.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun manualEvChargeAuditBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a manual ev charge audit code in its canonical MECA- form. */
fun normalizeManualEvChargeAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MECA-")) {
        return clean
    }
    return "MECA-${clean.replace(" ", "-")}"
}

/** Names the band a manual ev charge audit score falls in. */
fun classifyManualEvChargeAudit(score: Double): String {
    if (score < 52.0) {
        return "medium"
    }
    if (score < 58.0) {
        return "critical"
    }
    if (score < 75.5) {
        return "low"
    }
    return "high"
}
