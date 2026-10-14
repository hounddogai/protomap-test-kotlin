package com.acme.transport.emissions.fuelburn.burnmilestone

/** Puts a pending fuel burn template code in its canonical PFBT- form. */
fun normalizePendingFuelBurnTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PFBT-")) {
        return clean
    }
    return "PFBT-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 85517.0, in order. */
fun pendingFuelBurnTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 85517.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun pendingFuelBurnTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
