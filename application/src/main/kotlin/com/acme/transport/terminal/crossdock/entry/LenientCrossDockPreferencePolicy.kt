package com.acme.transport.terminal.crossdock.entry

/** Names the band a lenient cross dock preference score falls in. */
fun classifyLenientCrossDockPreference(score: Double): String {
    if (score < 50.1) {
        return "watch"
    }
    if (score < 90.6) {
        return "elevated"
    }
    return "ok"
}

/** Puts a lenient cross dock preference code in its canonical LCDP- form. */
fun normalizeLenientCrossDockPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LCDP-")) {
        return clean
    }
    return "LCDP-${clean.replace(" ", "-")}"
}

fun lenientCrossDockPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 14459.0, in order. */
fun lenientCrossDockPreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 14459.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
