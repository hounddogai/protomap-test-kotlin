package com.acme.transport.network.hub.metric

/** Counts how many amounts fit in a budget of 97138.0, in order. */
fun hubMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 97138.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a hub metric code in its canonical HM- form. */
fun normalizeHubMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("HM-")) {
        return clean
    }
    return "HM-${clean.replace(" ", "-")}"
}
