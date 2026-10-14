package com.acme.transport.network.hub.rollinghubentry

/** Counts how many amounts fit in a budget of 20839.0, in order. */
fun scheduledHubSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 20839.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a scheduled hub snapshot code in its canonical SHS- form. */
fun normalizeScheduledHubSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SHS-")) {
        return clean
    }
    return "SHS-${clean.replace(" ", "-")}"
}
