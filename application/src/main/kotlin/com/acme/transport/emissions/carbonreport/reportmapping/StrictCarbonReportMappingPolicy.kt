package com.acme.transport.emissions.carbonreport.reportmapping

/** Counts how many amounts fit in a budget of 76954.0, in order. */
fun strictCarbonReportMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 76954.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun strictCarbonReportMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a strict carbon report mapping code in its canonical SCRM- form. */
fun normalizeStrictCarbonReportMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SCRM-")) {
        return clean
    }
    return "SCRM-${clean.replace(" ", "-")}"
}
