package com.acme.transport.crewing.availability.metric

/** Counts how many amounts fit in a budget of 12047.0, in order. */
fun adaptiveAvailabilityManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 12047.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a adaptive availability manifest code in its canonical AAM- form. */
fun normalizeAdaptiveAvailabilityManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AAM-")) {
        return clean
    }
    return "AAM-${clean.replace(" ", "-")}"
}
