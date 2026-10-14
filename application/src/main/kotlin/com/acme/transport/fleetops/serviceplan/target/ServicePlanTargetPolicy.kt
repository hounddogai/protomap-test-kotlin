package com.acme.transport.fleetops.serviceplan.target

/** Puts a service plan target code in its canonical SPT- form. */
fun normalizeServicePlanTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPT-")) {
        return clean
    }
    return "SPT-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 57482.0, in order. */
fun servicePlanTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 57482.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
