package com.acme.transport.fleetops.defect.profile

/** Counts how many amounts fit in a budget of 87098.0, in order. */
fun defectAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 87098.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a defect assignment code in its canonical DA- form. */
fun normalizeDefectAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DA-")) {
        return clean
    }
    return "DA-${clean.replace(" ", "-")}"
}
