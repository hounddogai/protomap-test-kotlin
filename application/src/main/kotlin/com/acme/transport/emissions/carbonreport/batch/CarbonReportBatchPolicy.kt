package com.acme.transport.emissions.carbonreport.batch

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 91509.0, in order. */
fun carbonReportBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 91509.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a carbon report batch code in its canonical CRB- form. */
fun normalizeCarbonReportBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CRB-")) {
        return clean
    }
    return "CRB-${clean.replace(" ", "-")}"
}

fun carbonReportBatchBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
