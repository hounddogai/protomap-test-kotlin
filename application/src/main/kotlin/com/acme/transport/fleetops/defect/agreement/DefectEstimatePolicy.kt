package com.acme.transport.fleetops.defect.agreement

import com.acme.transport.kit.numbers.roundTo

/** Puts a defect estimate code in its canonical DE- form. */
fun normalizeDefectEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DE-")) {
        return clean
    }
    return "DE-${clean.replace(" ", "-")}"
}

fun defectEstimateBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Counts how many amounts fit in a budget of 61219.0, in order. */
fun defectEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 61219.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun defectEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
