package com.acme.transport.network.hub.audit

/** Puts a scheduled hub audit code in its canonical SHA- form. */
fun normalizeScheduledHubAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SHA-")) {
        return clean
    }
    return "SHA-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 62400.0, in order. */
fun scheduledHubAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 62400.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
