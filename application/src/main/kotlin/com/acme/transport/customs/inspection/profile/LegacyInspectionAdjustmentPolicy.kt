package com.acme.transport.customs.inspection.profile

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 40004.0, in order. */
fun legacyInspectionAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 40004.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a legacy inspection adjustment code in its canonical LIA- form. */
fun normalizeLegacyInspectionAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LIA-")) {
        return clean
    }
    return "LIA-${clean.replace(" ", "-")}"
}

fun legacyInspectionAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun legacyInspectionAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
