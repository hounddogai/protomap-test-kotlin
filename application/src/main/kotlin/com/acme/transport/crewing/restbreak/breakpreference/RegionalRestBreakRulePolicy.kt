package com.acme.transport.crewing.restbreak.breakpreference

/** Counts how many amounts fit in a budget of 49602.0, in order. */
fun regionalRestBreakRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 49602.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a regional rest break rule code in its canonical RRBR- form. */
fun normalizeRegionalRestBreakRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RRBR-")) {
        return clean
    }
    return "RRBR-${clean.replace(" ", "-")}"
}
