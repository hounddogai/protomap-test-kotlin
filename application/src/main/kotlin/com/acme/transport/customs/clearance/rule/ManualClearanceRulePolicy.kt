package com.acme.transport.customs.clearance.rule

/** Puts a manual clearance rule code in its canonical MCR- form. */
fun normalizeManualClearanceRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MCR-")) {
        return clean
    }
    return "MCR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 86440.0, in order. */
fun manualClearanceRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 86440.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
