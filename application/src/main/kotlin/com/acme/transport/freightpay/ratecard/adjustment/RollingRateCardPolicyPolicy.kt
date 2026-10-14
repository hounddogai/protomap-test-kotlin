package com.acme.transport.freightpay.ratecard.adjustment

fun rollingRateCardPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a rolling rate card policy code in its canonical RRCP- form. */
fun normalizeRollingRateCardPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RRCP-")) {
        return clean
    }
    return "RRCP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 76061.0, in order. */
fun rollingRateCardPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 76061.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
