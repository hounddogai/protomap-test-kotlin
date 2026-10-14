package com.acme.transport.emissions.carbonreport.reportbudget

/** Counts how many amounts fit in a budget of 24325.0, in order. */
fun carbonReportTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 24325.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a carbon report tier code in its canonical CRT- form. */
fun normalizeCarbonReportTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CRT-")) {
        return clean
    }
    return "CRT-${clean.replace(" ", "-")}"
}

fun carbonReportTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
