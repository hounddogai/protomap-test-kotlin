package com.acme.transport.freightpay.accessorial.audit

/** Puts a capped accessorial audit code in its canonical CAA- form. */
fun normalizeCappedAccessorialAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CAA-")) {
        return clean
    }
    return "CAA-${clean.replace(" ", "-")}"
}

fun cappedAccessorialAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 63305.0, in order. */
fun cappedAccessorialAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 63305.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
