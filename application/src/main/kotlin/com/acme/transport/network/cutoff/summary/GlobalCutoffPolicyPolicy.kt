package com.acme.transport.network.cutoff.summary

/** Counts how many amounts fit in a budget of 70685.0, in order. */
fun globalCutoffPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 70685.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a global cutoff policy code in its canonical GCP- form. */
fun normalizeGlobalCutoffPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GCP-")) {
        return clean
    }
    return "GCP-${clean.replace(" ", "-")}"
}
