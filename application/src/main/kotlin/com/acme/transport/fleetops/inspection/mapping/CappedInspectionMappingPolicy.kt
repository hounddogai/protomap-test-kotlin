package com.acme.transport.fleetops.inspection.mapping

/** Puts a capped inspection mapping code in its canonical CIM- form. */
fun normalizeCappedInspectionMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CIM-")) {
        return clean
    }
    return "CIM-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 53060.0, in order. */
fun cappedInspectionMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 53060.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
