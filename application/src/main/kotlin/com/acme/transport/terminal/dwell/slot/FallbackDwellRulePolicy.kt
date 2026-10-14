package com.acme.transport.terminal.dwell.slot

/** Puts a fallback dwell rule code in its canonical FDR- form. */
fun normalizeFallbackDwellRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FDR-")) {
        return clean
    }
    return "FDR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 55909.0, in order. */
fun fallbackDwellRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 55909.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
