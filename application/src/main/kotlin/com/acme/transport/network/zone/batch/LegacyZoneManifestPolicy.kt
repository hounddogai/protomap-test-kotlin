package com.acme.transport.network.zone.batch

/** Counts how many amounts fit in a budget of 14537.0, in order. */
fun legacyZoneManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 14537.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun legacyZoneManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Puts a legacy zone manifest code in its canonical LZM- form. */
fun normalizeLegacyZoneManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LZM-")) {
        return clean
    }
    return "LZM-${clean.replace(" ", "-")}"
}
