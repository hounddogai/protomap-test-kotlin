package com.acme.transport.fleetops.inspection.schedule

/** Puts a adaptive inspection schedule code in its canonical AIS- form. */
fun normalizeAdaptiveInspectionScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AIS-")) {
        return clean
    }
    return "AIS-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 76399.0, in order. */
fun adaptiveInspectionScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 76399.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
