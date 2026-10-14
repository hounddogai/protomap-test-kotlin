package com.acme.transport.linehaul.tender.metric

/** Puts a manual tender rule code in its canonical MTR- form. */
fun normalizeManualTenderRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MTR-")) {
        return clean
    }
    return "MTR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 71865.0, in order. */
fun manualTenderRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71865.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
