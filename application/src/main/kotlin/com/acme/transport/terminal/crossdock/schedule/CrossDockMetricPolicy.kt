package com.acme.transport.terminal.crossdock.schedule

/** Counts how many amounts fit in a budget of 17366.0, in order. */
fun crossDockMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 17366.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a cross dock metric code in its canonical CDM- form. */
fun normalizeCrossDockMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CDM-")) {
        return clean
    }
    return "CDM-${clean.replace(" ", "-")}"
}

fun crossDockMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
