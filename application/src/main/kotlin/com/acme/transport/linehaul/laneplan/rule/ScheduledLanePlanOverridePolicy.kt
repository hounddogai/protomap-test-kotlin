package com.acme.transport.linehaul.laneplan.rule

fun scheduledLanePlanOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Puts a scheduled lane plan override code in its canonical SLPO- form. */
fun normalizeScheduledLanePlanOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SLPO-")) {
        return clean
    }
    return "SLPO-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 15932.0, in order. */
fun scheduledLanePlanOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 15932.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
