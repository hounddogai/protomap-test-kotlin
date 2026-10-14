package com.acme.transport.safety.incident.claim

/** Counts how many amounts fit in a budget of 54105.0, in order. */
fun incidentScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 54105.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a incident score code in its canonical IS- form. */
fun normalizeIncidentScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IS-")) {
        return clean
    }
    return "IS-${clean.replace(" ", "-")}"
}
