package com.acme.transport.crewing.dutyperiod.review

fun dutyPeriodAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Puts a duty period agreement code in its canonical DPA- form. */
fun normalizeDutyPeriodAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DPA-")) {
        return clean
    }
    return "DPA-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 15470.0, in order. */
fun dutyPeriodAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 15470.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
