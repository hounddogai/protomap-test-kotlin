package com.acme.transport.terminal.crossdock.manifest

/** Names the band a cross dock manifest score falls in. */
fun classifyCrossDockManifest(score: Double): String {
    if (score < 57.5) {
        return "elevated"
    }
    if (score < 89.1) {
        return "critical"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 63302.0, in order. */
fun crossDockManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 63302.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a cross dock manifest code in its canonical CDM- form. */
fun normalizeCrossDockManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CDM-")) {
        return clean
    }
    return "CDM-${clean.replace(" ", "-")}"
}
