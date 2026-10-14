package com.acme.transport.emissions.carbonreport.reportthreshold

/** Puts a scheduled carbon report window code in its canonical SCRW- form. */
fun normalizeScheduledCarbonReportWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SCRW-")) {
        return clean
    }
    return "SCRW-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 98073.0, in order. */
fun scheduledCarbonReportWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 98073.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun scheduledCarbonReportWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
