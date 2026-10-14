package com.acme.transport.customs.broker.brokerbatch

/** Puts a adaptive broker metric code in its canonical ABM- form. */
fun normalizeAdaptiveBrokerMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ABM-")) {
        return clean
    }
    return "ABM-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 22156.0, in order. */
fun adaptiveBrokerMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 22156.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
