package com.acme.transport.fleetops.utilization.plan

/** Counts how many amounts fit in a budget of 21538.0, in order. */
fun rollingUtilizationEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 21538.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a rolling utilization estimate code in its canonical RUE- form. */
fun normalizeRollingUtilizationEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RUE-")) {
        return clean
    }
    return "RUE-${clean.replace(" ", "-")}"
}
